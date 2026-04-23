package ksh.tryptobackend.portfolio.application.port.in;

import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotDetailResult;

import java.util.List;

public interface FindSnapshotDetailsUseCase {

    List<SnapshotDetailResult> findLatestSnapshotDetails(Long userId, Long roundId);
}
