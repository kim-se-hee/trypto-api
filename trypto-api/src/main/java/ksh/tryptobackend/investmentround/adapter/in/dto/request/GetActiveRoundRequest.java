package ksh.tryptobackend.investmentround.adapter.in.dto.request;

import jakarta.validation.constraints.NotNull;
import ksh.tryptobackend.investmentround.application.port.in.dto.query.GetActiveRoundQuery;

public record GetActiveRoundRequest(@NotNull Long userId) {

    public GetActiveRoundQuery toQuery() {
        return new GetActiveRoundQuery(userId);
    }
}
