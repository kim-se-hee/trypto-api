package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.domain.vo.RoundOverview;

import java.util.List;
import java.util.Optional;

public interface InvestmentRoundQueryPort {

    Optional<RoundOverview> findActiveRoundByUserId(Long userId);

    Optional<RoundOverview> findRoundInfoById(Long roundId);

    List<RoundOverview> findAllActiveRounds();
}
