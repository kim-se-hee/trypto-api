package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.model.PortfolioSnapshot;

import java.util.List;

public interface PortfolioSnapshotCommandPort {

    PortfolioSnapshot save(PortfolioSnapshot snapshot);

    List<PortfolioSnapshot> saveAll(List<PortfolioSnapshot> snapshots);
}
