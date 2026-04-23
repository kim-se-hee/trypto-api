package ksh.tryptobackend.investmentround.adapter.in.dto.response;

import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChargeEmergencyFundingResponse(
    Long roundId,
    Long exchangeId,
    BigDecimal chargedAmount,
    int remainingChargeCount,
    LocalDateTime chargedAt
) {

    public static ChargeEmergencyFundingResponse from(ChargeEmergencyFundingResult result) {
        return new ChargeEmergencyFundingResponse(
            result.roundId(),
            result.exchangeId(),
            result.chargedAmount(),
            result.remainingChargeCount(),
            result.chargedAt()
        );
    }
}
