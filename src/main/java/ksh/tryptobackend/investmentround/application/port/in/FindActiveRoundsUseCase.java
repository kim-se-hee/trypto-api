package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.result.ActiveRoundResult;

import java.util.List;

public interface FindActiveRoundsUseCase {

    List<ActiveRoundResult> findAllActiveRounds();
}
