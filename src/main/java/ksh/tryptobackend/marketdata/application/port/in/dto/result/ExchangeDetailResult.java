package ksh.tryptobackend.marketdata.application.port.in.dto.result;

public record ExchangeDetailResult(String name, Long baseCurrencyCoinId, boolean domestic) {
}
