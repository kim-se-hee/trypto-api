package ksh.tryptobackend.wallet.adapter.out.repository;

import ksh.tryptobackend.wallet.adapter.out.entity.WalletBalanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalanceJpaEntity, Long> {

    List<WalletBalanceJpaEntity> findByWalletId(Long walletId);

    Optional<WalletBalanceJpaEntity> findByWalletIdAndCoinId(Long walletId, Long coinId);
}
