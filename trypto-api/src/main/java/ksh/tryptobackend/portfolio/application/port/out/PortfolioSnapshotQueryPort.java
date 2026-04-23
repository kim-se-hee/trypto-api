package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.vo.HoldingSummary;
import ksh.tryptobackend.portfolio.domain.vo.SnapshotOverview;
import ksh.tryptobackend.portfolio.domain.vo.UserSnapshotSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSnapshotQueryPort {

    List<HoldingSummary> findLatestSnapshotDetails(Long userId, Long roundId);

    Optional<SnapshotOverview> findLatestByRoundIdAndExchangeId(Long roundId, Long exchangeId);

    List<SnapshotOverview> findAllByRoundIdAndExchangeId(Long roundId, Long exchangeId);

    List<UserSnapshotSummary> findLatestSummaries(LocalDate snapshotDate);
}
