package ksh.tryptobackend.batch.snapshot;

import ksh.tryptobackend.ranking.domain.model.PortfolioSnapshot;
import ksh.tryptobackend.ranking.domain.model.SnapshotDetail;

import java.util.List;

public record SnapshotOutput(PortfolioSnapshot snapshot, List<SnapshotDetail> details) {
}
