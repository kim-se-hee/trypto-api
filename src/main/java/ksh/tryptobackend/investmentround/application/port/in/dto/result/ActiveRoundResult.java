package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import java.time.LocalDateTime;

public record ActiveRoundResult(Long roundId, Long userId, LocalDateTime startedAt) {
}
