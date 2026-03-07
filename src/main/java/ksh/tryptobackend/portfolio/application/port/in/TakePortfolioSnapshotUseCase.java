package ksh.tryptobackend.portfolio.application.port.in;

import ksh.tryptobackend.portfolio.application.port.in.dto.command.TakeSnapshotCommand;
import ksh.tryptobackend.portfolio.application.port.in.dto.result.SnapshotResult;

public interface TakePortfolioSnapshotUseCase {

    SnapshotResult takeSnapshot(TakeSnapshotCommand command);
}
