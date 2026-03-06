package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindInvestmentRulesUseCase;
import ksh.tryptobackend.trading.application.port.out.InvestmentRulePort;
import ksh.tryptobackend.trading.domain.model.ViolationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvestmentRuleAdapter implements InvestmentRulePort {

    private final FindInvestmentRulesUseCase findInvestmentRulesUseCase;

    @Override
    public List<ViolationRule> findByRoundId(Long roundId) {
        return findInvestmentRulesUseCase.findByRoundId(roundId).stream()
            .map(result -> ViolationRule.of(result.ruleId(), result.ruleType(), result.thresholdValue()))
            .toList();
    }
}
