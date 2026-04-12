package ksh.tryptobackend.trading.adapter.out.repository;

import jakarta.persistence.LockModeType;
import ksh.tryptobackend.trading.adapter.out.entity.HoldingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface HoldingJpaRepository extends JpaRepository<HoldingJpaEntity, Long> {

    Optional<HoldingJpaEntity> findByWalletIdAndCoinId(Long walletId, Long coinId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<HoldingJpaEntity> findForUpdateByWalletIdAndCoinId(Long walletId, Long coinId);

    List<HoldingJpaEntity> findByWalletId(Long walletId);
}
