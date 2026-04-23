package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.query.CheckRuleViolationsQuery;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.RuleViolationResult;

import java.util.List;

public interface CheckRuleViolationsUseCase {

    List<RuleViolationResult> checkViolations(CheckRuleViolationsQuery query);
}
