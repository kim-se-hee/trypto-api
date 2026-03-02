package ksh.tryptobackend.regretanalysis.application.port.in.dto.result;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeMetadata;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RuleInfo;
import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import ksh.tryptobackend.regretanalysis.domain.model.RuleImpact;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetails;
import ksh.tryptobackend.regretanalysis.domain.vo.ThresholdUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record GetRegretReportResult(
    Long reportId,
    Long roundId,
    Long exchangeId,
    String exchangeName,
    String currency,
    int totalViolations,
    LocalDate analysisStart,
    LocalDate analysisEnd,
    BigDecimal missedProfit,
    BigDecimal actualProfitRate,
    BigDecimal ruleFollowedProfitRate,
    List<RuleImpactResult> ruleImpacts,
    List<ViolationDetailResult> violationDetails
) {

    public static GetRegretReportResult from(RegretReport report,
                                             ExchangeMetadata exchange,
                                             Map<Long, RuleInfo> ruleMap,
                                             Map<Long, String> coinSymbols) {
        List<RuleImpactResult> ruleImpactResults = report.getRuleImpacts().stream()
            .map(ri -> toRuleImpactResult(ri, ruleMap))
            .toList();

        List<ViolationDetailResult> violationDetailResults = toViolationDetailResults(
            report.getViolationDetails(), ruleMap, coinSymbols);

        return new GetRegretReportResult(
            report.getReportId(),
            report.getRoundId(),
            report.getExchangeId(),
            exchange.name(),
            exchange.currency(),
            report.getTotalViolations(),
            report.getAnalysisStart(),
            report.getAnalysisEnd(),
            report.getMissedProfit(),
            report.getActualProfitRate(),
            report.getRuleFollowedProfitRate(),
            ruleImpactResults,
            violationDetailResults
        );
    }

    private static RuleImpactResult toRuleImpactResult(RuleImpact ruleImpact,
                                                        Map<Long, RuleInfo> ruleMap) {
        RuleInfo rule = ruleMap.get(ruleImpact.getRuleId());

        return new RuleImpactResult(
            ruleImpact.getRuleImpactId(),
            ruleImpact.getRuleId(),
            rule.ruleType(),
            rule.thresholdValue(),
            ThresholdUnit.from(rule.ruleType()).symbol(),
            ruleImpact.getViolationCount(),
            ruleImpact.getTotalLossAmount(),
            ruleImpact.getImpactGap().value()
        );
    }

    private static List<ViolationDetailResult> toViolationDetailResults(
            ViolationDetails violationDetails,
            Map<Long, RuleInfo> ruleMap,
            Map<Long, String> coinSymbols) {
        List<ViolationDetailResult> results = new ArrayList<>();

        for (Map.Entry<Long, List<ViolationDetail>> entry : violationDetails.groupByOrder().entrySet()) {
            List<ViolationDetail> grouped = entry.getValue();
            ViolationDetail first = grouped.getFirst();

            List<String> violatedRules = grouped.stream()
                .map(d -> ruleMap.get(d.getRuleId()).ruleType().name())
                .distinct()
                .toList();

            String coinSymbol = coinSymbols.getOrDefault(first.getCoinId(), "");

            results.add(new ViolationDetailResult(
                first.getViolationDetailId(), first.getOrderId(), coinSymbol,
                violatedRules, first.getProfitLoss(), first.getOccurredAt()));
        }

        for (ViolationDetail detail : violationDetails.findMonitoringViolations()) {
            RuleInfo rule = ruleMap.get(detail.getRuleId());
            String coinSymbol = coinSymbols.getOrDefault(detail.getCoinId(), "");

            results.add(new ViolationDetailResult(
                detail.getViolationDetailId(), null, coinSymbol,
                List.of(rule.ruleType().name()), detail.getProfitLoss(), detail.getOccurredAt()));
        }

        return results;
    }

    public record RuleImpactResult(
        Long ruleImpactId,
        Long ruleId,
        RuleType ruleType,
        BigDecimal thresholdValue,
        String thresholdUnit,
        int violationCount,
        BigDecimal totalLossAmount,
        BigDecimal impactGap
    ) {
    }

    public record ViolationDetailResult(
        Long violationDetailId,
        Long orderId,
        String coinSymbol,
        List<String> violatedRules,
        BigDecimal profitLoss,
        LocalDateTime occurredAt
    ) {
    }
}
