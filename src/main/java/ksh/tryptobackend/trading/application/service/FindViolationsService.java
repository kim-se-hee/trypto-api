package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.trading.application.port.in.FindViolationsUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.result.ViolationResult;
import ksh.tryptobackend.trading.application.port.out.RecordedViolationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindViolationsService implements FindViolationsUseCase {

    private final RecordedViolationQueryPort recordedViolationQueryPort;

    @Override
    public List<ViolationResult> findByRuleIdsAndExchangeId(List<Long> ruleIds, Long exchangeId) {
        return recordedViolationQueryPort.findByRuleIdsAndExchangeId(ruleIds, exchangeId).stream()
            .map(v -> new ViolationResult(v.violationId(), v.orderId(), v.ruleId(), v.createdAt()))
            .toList();
    }
}
