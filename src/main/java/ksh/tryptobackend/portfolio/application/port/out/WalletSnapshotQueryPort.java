package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.vo.WalletSnapshot;

import java.util.List;

public interface WalletSnapshotQueryPort {

    List<WalletSnapshot> findByRoundId(Long roundId);

    List<WalletSnapshot> findByRoundIds(List<Long> roundIds);
}
