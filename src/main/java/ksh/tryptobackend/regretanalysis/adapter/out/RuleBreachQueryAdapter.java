package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.trading.application.port.in.FindViolationsUseCase;
import ksh.tryptobackend.regretanalysis.application.port.out.RuleBreachQueryPort;
import ksh.tryptobackend.regretanalysis.domain.vo.RuleBreach;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleBreachQueryAdapter implements RuleBreachQueryPort {

    private final FindViolationsUseCase findViolationsUseCase;

    @Override
    public List<RuleBreach> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId) {
        return findViolationsUseCase.findByRuleIdsAndExchangeId(ruleIds, exchangeId).stream()
            .map(result -> new RuleBreach(result.violationId(), result.orderId(), result.ruleId(), result.createdAt()))
            .toList();
    }
}
