package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.result.RoundInfoResult;

import java.util.Optional;

public interface FindRoundInfoUseCase {

    Optional<RoundInfoResult> findById(Long roundId);

    Optional<RoundInfoResult> findActiveByUserId(Long userId);
}
