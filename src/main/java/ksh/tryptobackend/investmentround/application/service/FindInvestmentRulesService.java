package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.investmentround.application.port.in.FindInvestmentRulesUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.InvestmentRuleResult;
import ksh.tryptobackend.investmentround.application.port.out.InvestmentRuleQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindInvestmentRulesService implements FindInvestmentRulesUseCase {

    private final InvestmentRuleQueryPort investmentRuleQueryPort;

    @Override
    public List<InvestmentRuleResult> findByRoundId(Long roundId) {
        return investmentRuleQueryPort.findByRoundId(roundId).stream()
            .map(info -> new InvestmentRuleResult(info.ruleId(), info.ruleType(), info.thresholdValue()))
            .toList();
    }
}
