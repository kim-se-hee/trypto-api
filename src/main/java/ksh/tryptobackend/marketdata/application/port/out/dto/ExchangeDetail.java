package ksh.tryptobackend.marketdata.application.port.out.dto;

import java.math.BigDecimal;

public record ExchangeDetail(String name, Long baseCurrencyCoinId, boolean domestic, BigDecimal feeRate) {
}
