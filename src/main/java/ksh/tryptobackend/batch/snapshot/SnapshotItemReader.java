package ksh.tryptobackend.batch.snapshot;

import ksh.tryptobackend.portfolio.application.port.out.ActiveRoundQueryPort;
import ksh.tryptobackend.portfolio.application.port.out.WalletSnapshotQueryPort;
import ksh.tryptobackend.portfolio.domain.vo.ActiveRound;
import ksh.tryptobackend.portfolio.domain.vo.WalletSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
public class SnapshotItemReader implements ItemReader<SnapshotInput> {

    private final ActiveRoundQueryPort activeRoundQueryPort;
    private final WalletSnapshotQueryPort walletSnapshotPort;

    private Iterator<SnapshotInput> iterator;

    @Override
    public SnapshotInput read() {
        if (iterator == null) {
            iterator = buildInputList().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<SnapshotInput> buildInputList() {
        List<ActiveRound> activeRounds = activeRoundQueryPort.findAllActiveRounds();
        List<Long> roundIds = activeRounds.stream().map(ActiveRound::roundId).toList();
        Map<Long, List<WalletSnapshot>> walletsByRoundId = walletSnapshotPort.findByRoundIds(roundIds).stream()
            .collect(Collectors.groupingBy(WalletSnapshot::roundId));

        return activeRounds.stream()
            .flatMap(round -> walletsByRoundId.getOrDefault(round.roundId(), List.of()).stream()
                .map(wallet -> toSnapshotInput(round, wallet)))
            .toList();
    }

    private SnapshotInput toSnapshotInput(ActiveRound round, WalletSnapshot wallet) {
        return new SnapshotInput(
            round.roundId(), round.userId(), wallet.exchangeId(),
            wallet.walletId(), wallet.seedAmount()
        );
    }
}
