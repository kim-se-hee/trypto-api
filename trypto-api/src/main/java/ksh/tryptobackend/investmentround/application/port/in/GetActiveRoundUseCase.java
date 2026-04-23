package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.query.GetActiveRoundQuery;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.GetActiveRoundResult;

public interface GetActiveRoundUseCase {

    GetActiveRoundResult getActiveRound(GetActiveRoundQuery query);
}
