package ksh.tryptobackend.trading.domain.vo;

import java.util.Objects;

public record ExchangeSymbolKey(
    String exchange,
    String symbol
) {

    public ExchangeSymbolKey {
        Objects.requireNonNull(exchange);
        Objects.requireNonNull(symbol);
    }
}
