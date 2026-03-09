package ksh.tryptobackend.ranking.application.service;

import ksh.tryptobackend.investmentround.application.port.in.FindActiveRoundsUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ActiveRoundResult;
import ksh.tryptobackend.portfolio.application.port.in.FindSnapshotSummariesUseCase;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotSummaryResult;
import ksh.tryptobackend.ranking.application.port.in.CalculateRankingUseCase;
import ksh.tryptobackend.ranking.application.port.in.dto.command.CalculateRankingCommand;
import ksh.tryptobackend.ranking.application.port.out.RankingCommandPort;
import ksh.tryptobackend.ranking.domain.model.Ranking;
import ksh.tryptobackend.ranking.domain.vo.EligibleRound;
import ksh.tryptobackend.ranking.domain.vo.EligibleRounds;
import ksh.tryptobackend.ranking.domain.vo.RankingCandidates;
import ksh.tryptobackend.ranking.domain.vo.RankingPeriod;
import ksh.tryptobackend.ranking.domain.vo.RoundKey;
import ksh.tryptobackend.ranking.domain.vo.SnapshotSummaries;
import ksh.tryptobackend.ranking.domain.vo.SnapshotSummary;
import ksh.tryptobackend.trading.application.port.in.CountFilledOrdersUseCase;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculateRankingService implements CalculateRankingUseCase {

    private final FindActiveRoundsUseCase findActiveRoundsUseCase;
    private final FindWalletUseCase findWalletUseCase;
    private final CountFilledOrdersUseCase countFilledOrdersUseCase;
    private final FindSnapshotSummariesUseCase findSnapshotSummariesUseCase;
    private final RankingCommandPort rankingCommandPort;
    private final Clock clock;

    @Override
    public void calculateRanking(CalculateRankingCommand command) {
        LocalDate snapshotDate = command.snapshotDate();

        EligibleRounds eligibleRounds = findEligibleRounds(snapshotDate);
        if (eligibleRounds.isEmpty()) {
            return;
        }

        SnapshotSummaries todaySummaries = loadSummariesOf(snapshotDate);

        for (RankingPeriod period : RankingPeriod.values()) {
            SnapshotSummaries comparison = loadSummariesOf(snapshotDate.minusDays(period.getWindowDays()));
            RankingCandidates candidates = eligibleRounds.toCandidates(todaySummaries, comparison);
            List<Ranking> rankings = candidates.toRankings(period, snapshotDate, LocalDateTime.now(clock));
            rankingCommandPort.replaceByPeriodAndDate(rankings, period, snapshotDate);
        }
    }

    private EligibleRounds findEligibleRounds(LocalDate snapshotDate) {
        List<ActiveRoundResult> activeRounds = findActiveRoundsUseCase.findAllActiveRounds();
        List<Long> roundIds = activeRounds.stream()
            .map(ActiveRoundResult::roundId)
            .toList();

        List<WalletResult> wallets = findWalletUseCase.findByRoundIds(roundIds);
        List<Long> walletIds = wallets.stream()
            .map(WalletResult::walletId)
            .toList();

        Map<Long, Integer> tradeCountByWalletId = countFilledOrdersUseCase.countGroupByWalletIds(walletIds);

        Map<Long, Integer> tradeCountByRoundId = wallets.stream()
            .collect(Collectors.groupingBy(
                WalletResult::roundId,
                Collectors.summingInt(w -> tradeCountByWalletId.getOrDefault(w.walletId(), 0))
            ));

        List<EligibleRound> eligibleRoundList = activeRounds.stream()
            .map(round -> new EligibleRound(
                round.userId(),
                round.roundId(),
                tradeCountByRoundId.getOrDefault(round.roundId(), 0),
                round.startedAt()
            ))
            .toList();

        return EligibleRounds.of(eligibleRoundList, snapshotDate);
    }

    private SnapshotSummaries loadSummariesOf(LocalDate date) {
        List<SnapshotSummary> summaries = findSnapshotSummariesUseCase.findLatestSummaries(date).stream()
            .map(r -> new SnapshotSummary(r.userId(), r.roundId(), r.totalAssetKrw(), r.totalInvestmentKrw()))
            .toList();

        Map<RoundKey, BigDecimal> totalAssetMap = summaries.stream()
            .collect(Collectors.toMap(SnapshotSummary::roundKey, SnapshotSummary::totalAssetKrw));

        return new SnapshotSummaries(totalAssetMap);
    }
}
