package ksh.tryptobackend.batch.regretreport;

import ksh.tryptobackend.regretanalysis.application.port.out.LivePricePort;
import ksh.tryptobackend.regretanalysis.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.regretanalysis.application.port.out.TradeViolationQueryPort;
import ksh.tryptobackend.regretanalysis.domain.model.AssetSnapshot;
import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import ksh.tryptobackend.regretanalysis.domain.model.RuleImpact;
import ksh.tryptobackend.regretanalysis.domain.model.TradeViolation;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
public class RegretReportItemProcessor implements ItemProcessor<RegretReportInput, RegretReport> {

    private final TradeViolationQueryPort tradeViolationQueryPort;
    private final LivePricePort livePricePort;
    private final PortfolioSnapshotPort portfolioSnapshotPort;

    @Override
    public RegretReport process(RegretReportInput input) {
        List<TradeViolation> violations = tradeViolationQueryPort
            .findByRoundIdAndExchangeId(input.roundId(), input.exchangeId());
        if (violations.isEmpty()) {
            return null;
        }

        List<ViolationDetail> details = calculateViolationDetails(violations);
        AssetSnapshot snapshot = getLatestSnapshot(input);
        List<RuleImpact> impacts = new ViolationDetails(details)
            .toRuleImpacts(snapshot.getTotalInvestment());

        return RegretReport.generate(
            input.userId(), input.roundId(), input.exchangeId(),
            snapshot.getTotalProfitRate(), snapshot.getTotalInvestment(),
            impacts, details,
            input.startedAt().toLocalDate(), LocalDate.now()
        );
    }

    private List<ViolationDetail> calculateViolationDetails(List<TradeViolation> violations) {
        Map<Long, BigDecimal> currentPrices = resolveCurrentPrices(violations);
        return violations.stream()
            .map(v -> {
                BigDecimal lossAmount = v.calculateLoss(currentPrices.get(v.getExchangeCoinId()));
                return ViolationDetail.create(
                    v.getOrderId(), v.getRuleId(), v.getExchangeCoinId(),
                    lossAmount, lossAmount, v.getViolatedAt());
            })
            .toList();
    }

    private Map<Long, BigDecimal> resolveCurrentPrices(List<TradeViolation> violations) {
        return violations.stream()
            .map(TradeViolation::getExchangeCoinId)
            .distinct()
            .collect(Collectors.toMap(id -> id, livePricePort::getCurrentPrice));
    }

    private AssetSnapshot getLatestSnapshot(RegretReportInput input) {
        return portfolioSnapshotPort
            .getLatestByRoundIdAndExchangeId(input.roundId(), input.exchangeId());
    }
}
