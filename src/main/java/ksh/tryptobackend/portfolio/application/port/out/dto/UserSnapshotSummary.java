package ksh.tryptobackend.portfolio.application.port.out.dto;

import java.math.BigDecimal;

public record UserSnapshotSummary(
    Long userId,
    Long roundId,
    BigDecimal totalAssetKrw,
    BigDecimal totalInvestmentKrw
) {
}
