package ksh.tryptobackend.investmentround.adapter.in.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.ChargeEmergencyFundingCommand;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargeEmergencyFundingRequest(
    @NotNull Long userId,
    @NotNull Long exchangeId,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
    @NotNull UUID idempotencyKey
) {

    public ChargeEmergencyFundingCommand toCommand(Long roundId) {
        return new ChargeEmergencyFundingCommand(roundId, userId, exchangeId, amount, idempotencyKey);
    }
}
