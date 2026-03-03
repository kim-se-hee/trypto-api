package ksh.tryptobackend.batch.regretreport;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.application.port.out.InvestmentRulePort;
import ksh.tryptobackend.regretanalysis.application.port.out.LivePricePort;
import ksh.tryptobackend.regretanalysis.application.port.out.OrderHistoryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.regretanalysis.application.port.out.RuleViolationPort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RuleInfo;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RuleViolationRecord;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.TradeRecord;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.TradeSide;
import ksh.tryptobackend.regretanalysis.domain.model.AssetSnapshot;
import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import ksh.tryptobackend.regretanalysis.domain.model.RuleImpact;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import ksh.tryptobackend.regretanalysis.domain.vo.ImpactGap;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
public class RegretReportItemProcessor implements ItemProcessor<RegretReportInput, RegretReport> {

    private static final int RATE_SCALE = 4;

    private final InvestmentRulePort investmentRulePort;
    private final RuleViolationPort ruleViolationPort;
    private final OrderHistoryPort orderHistoryPort;
    private final LivePricePort livePricePort;
    private final PortfolioSnapshotPort portfolioSnapshotPort;

    @Override
    public RegretReport process(RegretReportInput input) {
        List<RuleInfo> rules = investmentRulePort.findByRoundId(input.roundId());
        if (rules.isEmpty()) {
            return null;
        }

        List<Long> ruleIds = rules.stream().map(RuleInfo::ruleId).toList();
        List<RuleViolationRecord> violations = ruleViolationPort.findByRuleIdsAndExchangeId(ruleIds, input.exchangeId());
        if (violations.isEmpty()) {
            return null;
        }

        Map<Long, RuleInfo> ruleMap = rules.stream()
            .collect(Collectors.toMap(RuleInfo::ruleId, r -> r));

        List<ViolationDetail> details = buildViolationDetails(violations, ruleMap, input);
        List<RuleImpact> impacts = buildRuleImpacts(details, ruleMap, input);

        AssetSnapshot snapshot = portfolioSnapshotPort
            .findLatestByRoundIdAndExchangeId(input.roundId(), input.exchangeId())
            .orElse(null);

        BigDecimal actualProfitRate = snapshot != null ? snapshot.getTotalProfitRate() : BigDecimal.ZERO;
        BigDecimal totalInvestment = snapshot != null ? snapshot.getTotalInvestment() : BigDecimal.ZERO;
        LocalDate analysisStart = input.startedAt().toLocalDate();
        LocalDate analysisEnd = LocalDate.now();

        return RegretReport.generate(
            input.userId(), input.roundId(), input.exchangeId(),
            actualProfitRate, totalInvestment,
            impacts, details,
            analysisStart, analysisEnd
        );
    }

    private List<ViolationDetail> buildViolationDetails(List<RuleViolationRecord> violations,
                                                        Map<Long, RuleInfo> ruleMap,
                                                        RegretReportInput input) {
        List<Long> orderIds = violations.stream()
            .map(RuleViolationRecord::orderId)
            .filter(id -> id != null)
            .toList();

        Map<Long, TradeRecord> tradeMap = orderHistoryPort.findByOrderIds(orderIds).stream()
            .collect(Collectors.toMap(TradeRecord::orderId, t -> t));

        List<ViolationDetail> details = new ArrayList<>();
        for (RuleViolationRecord violation : violations) {
            RuleInfo rule = ruleMap.get(violation.ruleId());
            if (rule == null || violation.orderId() == null) {
                continue;
            }

            TradeRecord trade = tradeMap.get(violation.orderId());
            if (trade == null) {
                continue;
            }

            BigDecimal lossAmount = calculateLoss(rule, trade, input);
            details.add(ViolationDetail.create(
                violation.orderId(), violation.ruleId(), null,
                lossAmount, lossAmount, violation.createdAt()
            ));
        }
        return details;
    }

    private BigDecimal calculateLoss(RuleInfo rule, TradeRecord trade, RegretReportInput input) {
        return switch (rule.ruleType()) {
            case CHASE_BUY_BAN, AVERAGING_DOWN_LIMIT ->
                calculateBuyViolationLoss(trade, input);
            case OVERTRADING_LIMIT ->
                trade.side() == TradeSide.BUY
                    ? calculateBuyViolationLoss(trade, input)
                    : calculateSellViolationLoss(trade);
            case LOSS_CUT ->
                calculateLossCutLoss(trade);
            case PROFIT_TAKE ->
                calculateProfitTakeLoss(trade);
        };
    }

    private BigDecimal calculateBuyViolationLoss(TradeRecord trade, RegretReportInput input) {
        List<TradeRecord> sellOrders = orderHistoryPort.findSellOrdersAfter(
            trade.walletId(), trade.exchangeCoinId(), trade.filledAt());

        BigDecimal remainingQty = trade.quantity();
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (TradeRecord sell : sellOrders) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal matchedQty = sell.quantity().min(remainingQty);
            BigDecimal loss = trade.filledPrice().subtract(sell.filledPrice()).multiply(matchedQty);
            totalLoss = totalLoss.add(loss);
            remainingQty = remainingQty.subtract(matchedQty);
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentPrice = livePricePort.getCurrentPrice(trade.exchangeCoinId());
            BigDecimal unrealizedLoss = trade.filledPrice().subtract(currentPrice).multiply(remainingQty);
            totalLoss = totalLoss.add(unrealizedLoss);
        }

        return totalLoss;
    }

    private BigDecimal calculateSellViolationLoss(TradeRecord trade) {
        BigDecimal currentPrice = livePricePort.getCurrentPrice(trade.exchangeCoinId());
        return currentPrice.subtract(trade.filledPrice()).multiply(trade.quantity());
    }

    private BigDecimal calculateLossCutLoss(TradeRecord trade) {
        BigDecimal currentPrice = livePricePort.getCurrentPrice(trade.exchangeCoinId());
        BigDecimal actualAmount = currentPrice.multiply(trade.quantity());
        return actualAmount.subtract(trade.amount());
    }

    private BigDecimal calculateProfitTakeLoss(TradeRecord trade) {
        BigDecimal currentPrice = livePricePort.getCurrentPrice(trade.exchangeCoinId());
        BigDecimal actualAmount = currentPrice.multiply(trade.quantity());
        return trade.amount().subtract(actualAmount);
    }

    private List<RuleImpact> buildRuleImpacts(List<ViolationDetail> details,
                                              Map<Long, RuleInfo> ruleMap,
                                              RegretReportInput input) {
        AssetSnapshot snapshot = portfolioSnapshotPort
            .findLatestByRoundIdAndExchangeId(input.roundId(), input.exchangeId())
            .orElse(null);
        BigDecimal totalInvestment = snapshot != null ? snapshot.getTotalInvestment() : BigDecimal.ZERO;

        Map<Long, List<ViolationDetail>> grouped = details.stream()
            .collect(Collectors.groupingBy(ViolationDetail::getRuleId));

        List<RuleImpact> impacts = new ArrayList<>();
        for (Map.Entry<Long, List<ViolationDetail>> entry : grouped.entrySet()) {
            Long ruleId = entry.getKey();
            List<ViolationDetail> ruleDetails = entry.getValue();

            BigDecimal totalLoss = ruleDetails.stream()
                .map(ViolationDetail::getLossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            ImpactGap gap = calculateImpactGap(totalLoss, totalInvestment);

            impacts.add(RuleImpact.create(ruleId, ruleDetails.size(), totalLoss, gap));
        }
        return impacts;
    }

    private ImpactGap calculateImpactGap(BigDecimal totalLoss, BigDecimal totalInvestment) {
        if (totalInvestment.compareTo(BigDecimal.ZERO) == 0) {
            return ImpactGap.of(BigDecimal.ZERO);
        }
        BigDecimal gap = totalLoss
            .divide(totalInvestment, RATE_SCALE, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        return ImpactGap.of(gap);
    }
}
