package ksh.tryptobackend.investmentround.adapter.in.dto.request;

import jakarta.validation.constraints.NotNull;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.EndRoundCommand;

public record EndRoundRequest(@NotNull Long userId) {

    public EndRoundCommand toCommand(Long roundId) {
        return new EndRoundCommand(roundId, userId);
    }
}
