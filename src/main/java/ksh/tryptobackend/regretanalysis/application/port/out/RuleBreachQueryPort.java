package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.RuleBreach;

import java.util.List;

public interface RuleBreachQueryPort {

    List<RuleBreach> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId);
}
