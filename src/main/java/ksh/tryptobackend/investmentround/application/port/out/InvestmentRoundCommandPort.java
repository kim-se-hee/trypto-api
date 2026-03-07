package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.domain.model.InvestmentRound;

import java.util.Optional;

public interface InvestmentRoundCommandPort {

    boolean existsActiveRoundByUserId(Long userId);

    long countByUserId(Long userId);

    Optional<InvestmentRound> findById(Long roundId);

    InvestmentRound save(InvestmentRound round);
}
