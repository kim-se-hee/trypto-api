package ksh.tryptobackend.portfolio.application.port.in;

import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotSummaryResult;

import java.time.LocalDate;
import java.util.List;

public interface FindSnapshotSummariesUseCase {

    List<SnapshotSummaryResult> findLatestSummaries(LocalDate snapshotDate);
}
