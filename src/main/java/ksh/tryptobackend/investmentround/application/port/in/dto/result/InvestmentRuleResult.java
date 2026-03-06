package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.common.domain.vo.RuleType;

import java.math.BigDecimal;

public record InvestmentRuleResult(Long ruleId, RuleType ruleType, BigDecimal thresholdValue) {
}
