package ksh.tryptobackend.portfolio.application.service;

import ksh.tryptobackend.investmentround.application.port.in.FindActiveRoundsUseCase;
import ksh.tryptobackend.portfolio.application.port.in.FindSnapshotInputsUseCase;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotInputResult;
import ksh.tryptobackend.portfolio.domain.vo.ActiveRound;
import ksh.tryptobackend.portfolio.domain.vo.WalletSnapshot;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindSnapshotInputsService implements FindSnapshotInputsUseCase {

    private final FindActiveRoundsUseCase findActiveRoundsUseCase;
    private final FindWalletUseCase findWalletUseCase;

    @Override
    public List<SnapshotInputResult> findAllSnapshotInputs() {
        List<ActiveRound> activeRounds = findActiveRounds();
        List<Long> roundIds = activeRounds.stream().map(ActiveRound::roundId).toList();
        Map<Long, List<WalletSnapshot>> walletsByRoundId = findWalletsByRoundIds(roundIds);

        return activeRounds.stream()
            .flatMap(round -> walletsByRoundId.getOrDefault(round.roundId(), List.of()).stream()
                .map(wallet -> new SnapshotInputResult(
                    round.roundId(), round.userId(),
                    wallet.exchangeId(), wallet.walletId(), wallet.seedAmount())))
            .toList();
    }

    private List<ActiveRound> findActiveRounds() {
        return findActiveRoundsUseCase.findAllActiveRounds().stream()
            .map(r -> new ActiveRound(r.roundId(), r.userId(), r.startedAt()))
            .toList();
    }

    private Map<Long, List<WalletSnapshot>> findWalletsByRoundIds(List<Long> roundIds) {
        return findWalletUseCase.findByRoundIds(roundIds).stream()
            .map(result -> new WalletSnapshot(result.walletId(), result.roundId(), result.exchangeId(), result.seedAmount()))
            .collect(Collectors.groupingBy(WalletSnapshot::roundId));
    }
}
