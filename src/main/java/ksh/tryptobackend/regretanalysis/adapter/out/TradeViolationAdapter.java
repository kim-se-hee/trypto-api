package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.investmentround.application.port.out.InvestmentRuleQueryPort;
import ksh.tryptobackend.investmentround.application.port.out.dto.InvestmentRuleInfo;
import ksh.tryptobackend.regretanalysis.application.port.out.TradeViolationQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.TradeSide;
import ksh.tryptobackend.regretanalysis.domain.model.TradeViolation;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext.SoldPortion;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import ksh.tryptobackend.trading.application.port.out.ViolationQueryPort;
import ksh.tryptobackend.trading.application.port.out.dto.OrderInfo;
import ksh.tryptobackend.trading.application.port.out.dto.ViolationInfo;
import ksh.tryptobackend.trading.domain.vo.Side;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TradeViolationAdapter implements TradeViolationQueryPort {

    private final InvestmentRuleQueryPort investmentRuleQueryPort;
    private final ViolationQueryPort violationQueryPort;
    private final OrderQueryPort orderQueryPort;

    @Override
    public List<TradeViolation> findByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        Map<Long, InvestmentRuleInfo> ruleMap = loadRuleMap(roundId);
        if (ruleMap.isEmpty()) {
            return List.of();
        }

        List<ViolationInfo> violations = violationQueryPort
            .findByRuleIdsAndExchangeId(new ArrayList<>(ruleMap.keySet()), exchangeId);
        if (violations.isEmpty()) {
            return List.of();
        }

        Map<Long, OrderInfo> orderMap = loadOrderMap(violations);
        Map<SellOrderKey, List<OrderInfo>> sellOrderCache = loadSellOrders(orderMap);

        return violations.stream()
            .filter(v -> v.orderId() != null)
            .map(v -> toTradeViolation(v, ruleMap, orderMap, sellOrderCache))
            .filter(Objects::nonNull)
            .toList();
    }

    private Map<Long, InvestmentRuleInfo> loadRuleMap(Long roundId) {
        return investmentRuleQueryPort.findByRoundId(roundId).stream()
            .collect(Collectors.toMap(InvestmentRuleInfo::ruleId, r -> r));
    }

    private Map<Long, OrderInfo> loadOrderMap(List<ViolationInfo> violations) {
        List<Long> orderIds = violations.stream()
            .map(ViolationInfo::orderId)
            .filter(Objects::nonNull)
            .toList();
        return orderQueryPort.findFilledByOrderIds(orderIds).stream()
            .collect(Collectors.toMap(OrderInfo::orderId, o -> o));
    }

    private Map<SellOrderKey, List<OrderInfo>> loadSellOrders(Map<Long, OrderInfo> orderMap) {
        Map<SellOrderKey, List<OrderInfo>> buyOrderGroups = orderMap.values().stream()
            .filter(o -> o.side() == Side.BUY)
            .collect(Collectors.groupingBy(o -> new SellOrderKey(o.walletId(), o.exchangeCoinId())));

        return buyOrderGroups.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    LocalDateTime earliestFilledAt = entry.getValue().stream()
                        .map(OrderInfo::filledAt)
                        .min(LocalDateTime::compareTo)
                        .orElseThrow();
                    return orderQueryPort.findFilledSellOrders(
                        entry.getKey().walletId(), entry.getKey().exchangeCoinId(), earliestFilledAt);
                }
            ));
    }

    private TradeViolation toTradeViolation(ViolationInfo violation,
                                             Map<Long, InvestmentRuleInfo> ruleMap,
                                             Map<Long, OrderInfo> orderMap,
                                             Map<SellOrderKey, List<OrderInfo>> sellOrderCache) {
        InvestmentRuleInfo rule = ruleMap.get(violation.ruleId());
        OrderInfo order = orderMap.get(violation.orderId());
        if (rule == null || order == null) {
            return null;
        }

        List<SoldPortion> soldPortions = resolveSoldPortions(order, sellOrderCache);

        return TradeViolation.create(
            order.orderId(), rule.ruleId(), rule.ruleType(),
            toTradeSide(order.side()),
            order.filledPrice(), order.quantity(), order.amount(),
            order.exchangeCoinId(), violation.createdAt(),
            soldPortions
        );
    }

    private List<SoldPortion> resolveSoldPortions(OrderInfo order,
                                                    Map<SellOrderKey, List<OrderInfo>> sellOrderCache) {
        if (order.side() != Side.BUY) {
            return List.of();
        }

        SellOrderKey key = new SellOrderKey(order.walletId(), order.exchangeCoinId());
        List<OrderInfo> sellOrders = sellOrderCache.getOrDefault(key, List.of());

        return sellOrders.stream()
            .filter(s -> s.filledAt().isAfter(order.filledAt()))
            .map(s -> new SoldPortion(s.filledPrice(), s.quantity()))
            .toList();
    }

    private TradeSide toTradeSide(Side side) {
        return switch (side) {
            case BUY -> TradeSide.BUY;
            case SELL -> TradeSide.SELL;
        };
    }

    private record SellOrderKey(Long walletId, Long exchangeCoinId) {
    }
}
