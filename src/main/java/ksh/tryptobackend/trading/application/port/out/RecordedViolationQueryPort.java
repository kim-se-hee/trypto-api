package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.vo.RecordedViolation;

import java.util.List;

public interface RecordedViolationQueryPort {

    List<RecordedViolation> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId);
}
