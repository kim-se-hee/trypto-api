package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.regretanalysis.application.port.out.AnalysisRuleQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.OrderExecutionQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.RuleBreachQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.ViolatedOrderQueryPort;
import ksh.tryptobackend.regretanalysis.domain.model.ViolatedOrder;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRule;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRules;
import ksh.tryptobackend.regretanalysis.domain.vo.RuleBreach;
import ksh.tryptobackend.regretanalysis.domain.vo.OrderExecution;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext.SoldPortion;
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
public class ViolatedOrderQueryAdapter implements ViolatedOrderQueryPort {

    private final AnalysisRuleQueryPort analysisRuleQueryPort;
    private final RuleBreachQueryPort ruleBreachQueryPort;
    private final OrderExecutionQueryPort orderExecutionQueryPort;

    @Override
    public List<ViolatedOrder> findByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        AnalysisRules rules = analysisRuleQueryPort.findByRoundId(roundId);
        if (rules.isEmpty()) {
            return List.of();
        }

        Map<Long, AnalysisRule> ruleMap = rules.toMap();

        List<RuleBreach> breaches = ruleBreachQueryPort
            .findByRuleIdsAndExchangeId(new ArrayList<>(ruleMap.keySet()), exchangeId);
        if (breaches.isEmpty()) {
            return List.of();
        }

        Map<Long, OrderExecution> orderMap = loadOrderMap(breaches);
        Map<SellOrderKey, List<OrderExecution>> sellOrderCache = loadSellOrders(orderMap);

        return breaches.stream()
            .filter(b -> b.orderId() != null)
            .filter(b -> ruleMap.containsKey(b.ruleId()) && orderMap.containsKey(b.orderId()))
            .map(b -> toViolatedOrder(b, ruleMap, orderMap, sellOrderCache))
            .toList();
    }

    private Map<Long, OrderExecution> loadOrderMap(List<RuleBreach> breaches) {
        List<Long> orderIds = breaches.stream()
            .map(RuleBreach::orderId)
            .filter(Objects::nonNull)
            .toList();
        return orderExecutionQueryPort.findByOrderIds(orderIds).stream()
            .collect(Collectors.toMap(OrderExecution::orderId, t -> t));
    }

    private Map<SellOrderKey, List<OrderExecution>> loadSellOrders(Map<Long, OrderExecution> orderMap) {
        Map<SellOrderKey, List<OrderExecution>> buyOrderGroups = orderMap.values().stream()
            .filter(OrderExecution::isBuy)
            .collect(Collectors.groupingBy(o -> new SellOrderKey(o.walletId(), o.exchangeCoinId())));

        if (buyOrderGroups.isEmpty()) {
            return Map.of();
        }

        return buyOrderGroups.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    LocalDateTime earliestFilledAt = entry.getValue().stream()
                        .map(OrderExecution::filledAt)
                        .min(LocalDateTime::compareTo)
                        .orElseThrow(() -> new IllegalStateException(
                            "BUY 주문 그룹에 filledAt이 없습니다: key=" + entry.getKey()));
                    return orderExecutionQueryPort.findSellOrdersAfter(
                        entry.getKey().walletId(), entry.getKey().exchangeCoinId(), earliestFilledAt);
                }
            ));
    }

    private ViolatedOrder toViolatedOrder(RuleBreach breach,
                                           Map<Long, AnalysisRule> ruleMap,
                                           Map<Long, OrderExecution> orderMap,
                                           Map<SellOrderKey, List<OrderExecution>> sellOrderCache) {
        AnalysisRule rule = ruleMap.get(breach.ruleId());
        OrderExecution order = orderMap.get(breach.orderId());
        List<SoldPortion> soldPortions = resolveSoldPortions(order, sellOrderCache);

        return ViolatedOrder.create(
            order.orderId(), rule.ruleId(), rule.ruleType(),
            order.side(),
            order.filledPrice(), order.quantity(), order.amount(),
            order.exchangeCoinId(), breach.createdAt(),
            soldPortions
        );
    }

    private List<SoldPortion> resolveSoldPortions(OrderExecution order,
                                                    Map<SellOrderKey, List<OrderExecution>> sellOrderCache) {
        if (!order.isBuy()) {
            return List.of();
        }

        SellOrderKey key = new SellOrderKey(order.walletId(), order.exchangeCoinId());
        List<OrderExecution> sellOrders = sellOrderCache.getOrDefault(key, List.of());

        return sellOrders.stream()
            .filter(s -> s.filledAt().isAfter(order.filledAt()))
            .map(s -> new SoldPortion(s.filledPrice(), s.quantity()))
            .toList();
    }

    private record SellOrderKey(Long walletId, Long exchangeCoinId) {
    }
}
