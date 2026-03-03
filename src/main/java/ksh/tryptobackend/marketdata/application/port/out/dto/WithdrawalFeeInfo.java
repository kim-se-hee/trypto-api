package ksh.tryptobackend.marketdata.application.port.out.dto;

import java.math.BigDecimal;

public record WithdrawalFeeInfo(BigDecimal fee, BigDecimal minWithdrawal) {
}
