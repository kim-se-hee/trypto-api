package ksh.tryptobackend.regretanalysis.application.port.out.dto;

public record ExchangeInfoRecord(
    Long exchangeId,
    String name,
    String currency
) {
}
