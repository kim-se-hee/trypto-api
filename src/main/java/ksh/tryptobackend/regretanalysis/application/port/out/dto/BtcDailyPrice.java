package ksh.tryptobackend.regretanalysis.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BtcDailyPrice(
    LocalDate date,
    BigDecimal closePrice
) {
}
