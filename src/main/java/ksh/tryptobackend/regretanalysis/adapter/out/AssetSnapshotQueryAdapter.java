package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.portfolio.application.port.in.FindSnapshotsUseCase;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotInfoResult;
import ksh.tryptobackend.regretanalysis.application.port.out.AssetSnapshotQueryPort;
import ksh.tryptobackend.regretanalysis.domain.model.AssetSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetSnapshotQueryAdapter implements AssetSnapshotQueryPort {

    private final FindSnapshotsUseCase findSnapshotsUseCase;

    @Override
    public AssetSnapshot getLatestByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        return findSnapshotsUseCase.findLatestByRoundIdAndExchangeId(roundId, exchangeId)
            .map(this::toAssetSnapshot)
            .orElseThrow(() -> new CustomException(ErrorCode.SNAPSHOT_NOT_FOUND));
    }

    @Override
    public List<AssetSnapshot> findAllByRoundIdAndExchangeId(Long roundId, Long exchangeId) {
        return findSnapshotsUseCase.findAllByRoundIdAndExchangeId(roundId, exchangeId).stream()
            .map(this::toAssetSnapshot)
            .toList();
    }

    private AssetSnapshot toAssetSnapshot(SnapshotInfoResult result) {
        return AssetSnapshot.reconstitute(
            result.snapshotId(), result.roundId(), result.exchangeId(),
            result.totalAsset(), result.totalInvestment(),
            result.totalProfitRate(), result.snapshotDate()
        );
    }
}
