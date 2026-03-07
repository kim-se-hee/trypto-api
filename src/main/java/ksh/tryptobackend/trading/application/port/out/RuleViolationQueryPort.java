package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.vo.RuleViolationRef;

import java.util.List;

public interface RuleViolationQueryPort {

    List<RuleViolationRef> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId);
}
