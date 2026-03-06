package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindActiveRoundsUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ActiveRoundResult;
import ksh.tryptobackend.ranking.application.port.out.EligibleRoundQueryPort;
import ksh.tryptobackend.ranking.domain.vo.EligibleRound;
import ksh.tryptobackend.trading.application.port.in.CountFilledOrdersUseCase;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EligibleRoundAdapter implements EligibleRoundQueryPort {

    private final FindActiveRoundsUseCase findActiveRoundsUseCase;
    private final FindWalletUseCase findWalletUseCase;
    private final CountFilledOrdersUseCase countFilledOrdersUseCase;

    @Override
    public List<EligibleRound> findAll() {
        List<ActiveRoundResult> rounds = findActiveRoundsUseCase.findAllActiveRounds();
        List<Long> roundIds = rounds.stream().map(ActiveRoundResult::roundId).toList();

        Map<Long, List<WalletResult>> walletsByRound = findWalletUseCase.findByRoundIds(roundIds).stream()
            .collect(Collectors.groupingBy(WalletResult::roundId));

        List<Long> walletIds = walletsByRound.values().stream()
            .flatMap(List::stream).map(WalletResult::walletId).toList();
        Map<Long, Integer> orderCounts = countFilledOrdersUseCase.countGroupByWalletIds(walletIds);

        return rounds.stream()
            .map(round -> toEligibleRound(round, walletsByRound, orderCounts))
            .toList();
    }

    private EligibleRound toEligibleRound(ActiveRoundResult round,
                                           Map<Long, List<WalletResult>> walletsByRound,
                                           Map<Long, Integer> orderCounts) {
        int tradeCount = walletsByRound.getOrDefault(round.roundId(), List.of()).stream()
            .mapToInt(w -> orderCounts.getOrDefault(w.walletId(), 0))
            .sum();
        return new EligibleRound(round.userId(), round.roundId(), tradeCount, round.startedAt());
    }
}
