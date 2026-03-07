package ksh.tryptobackend.trading.domain.vo;

import java.time.LocalDateTime;

public record RecordedViolation(
    Long violationId,
    Long orderId,
    Long ruleId,
    LocalDateTime createdAt
) {
}
