package ksh.tryptobackend.ranking.domain.vo;

import java.math.BigDecimal;

public record RankerHolding(
    String coinSymbol,
    String exchangeName,
    BigDecimal assetRatio,
    BigDecimal profitRate
) {
}
