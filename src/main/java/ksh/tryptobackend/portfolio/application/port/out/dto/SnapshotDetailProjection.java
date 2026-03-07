package ksh.tryptobackend.portfolio.application.port.out.dto;

import java.math.BigDecimal;

public record SnapshotDetailProjection(
    Long coinId,
    Long exchangeId,
    BigDecimal assetRatio,
    BigDecimal profitRate
) {
}
