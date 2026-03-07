package ksh.tryptobackend.portfolio.application.port.in.dto.result;

import java.math.BigDecimal;

public record SnapshotDetailResult(
    String coinSymbol,
    String exchangeName,
    BigDecimal assetRatio,
    BigDecimal profitRate
) {
}
