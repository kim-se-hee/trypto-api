package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindInvestmentRulesUseCase;
import ksh.tryptobackend.regretanalysis.application.port.out.InvestmentRulePort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RuleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("regretInvestmentRuleAdapter")
@RequiredArgsConstructor
public class InvestmentRuleAdapter implements InvestmentRulePort {

    private final FindInvestmentRulesUseCase findInvestmentRulesUseCase;

    @Override
    public List<RuleInfo> findByRoundId(Long roundId) {
        return findInvestmentRulesUseCase.findByRoundId(roundId).stream()
            .map(result -> new RuleInfo(result.ruleId(), result.ruleType(), result.thresholdValue()))
            .toList();
    }
}
