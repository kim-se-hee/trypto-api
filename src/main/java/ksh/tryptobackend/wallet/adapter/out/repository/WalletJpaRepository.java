package ksh.tryptobackend.wallet.adapter.out.repository;

import ksh.tryptobackend.wallet.adapter.out.entity.WalletJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<WalletJpaEntity, Long> {
}
