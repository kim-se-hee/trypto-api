package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.result.InvestmentRuleResult;

import java.util.List;

public interface FindInvestmentRulesUseCase {

    List<InvestmentRuleResult> findByRoundId(Long roundId);
}
