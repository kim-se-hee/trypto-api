package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.WalletSnapshotPort;
import ksh.tryptobackend.ranking.application.port.out.dto.WalletSnapshotInfo;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("rankingWalletSnapshotAdapter")
@RequiredArgsConstructor
public class WalletSnapshotAdapter implements WalletSnapshotPort {

    private final FindWalletUseCase findWalletUseCase;

    @Override
    public List<WalletSnapshotInfo> findByRoundId(Long roundId) {
        return findWalletUseCase.findByRoundId(roundId).stream()
            .map(this::toWalletSnapshotInfo)
            .toList();
    }

    @Override
    public List<WalletSnapshotInfo> findByRoundIds(List<Long> roundIds) {
        return findWalletUseCase.findByRoundIds(roundIds).stream()
            .map(this::toWalletSnapshotInfo)
            .toList();
    }

    private WalletSnapshotInfo toWalletSnapshotInfo(WalletResult result) {
        return new WalletSnapshotInfo(result.walletId(), result.roundId(), result.exchangeId(), result.seedAmount());
    }
}
