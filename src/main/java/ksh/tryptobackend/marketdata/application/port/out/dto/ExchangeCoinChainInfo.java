package ksh.tryptobackend.marketdata.application.port.out.dto;

public record ExchangeCoinChainInfo(
    Long exchangeCoinChainId,
    Long exchangeCoinId,
    String chain,
    boolean tagRequired
) {
}
