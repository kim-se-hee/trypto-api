package ksh.tryptobackend.wallet.adapter.out.repository;

import ksh.tryptobackend.wallet.adapter.out.entity.WalletBalanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalanceJpaEntity, Long> {

    Optional<WalletBalanceJpaEntity> findByWalletIdAndCoinId(Long walletId, Long coinId);
}
