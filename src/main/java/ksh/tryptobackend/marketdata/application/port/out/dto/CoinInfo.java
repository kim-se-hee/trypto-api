package ksh.tryptobackend.marketdata.application.port.out.dto;

public record CoinInfo(
    Long coinId,
    String symbol,
    String name
) {
}
