package ksh.tryptobackend.marketdata.adapter.out.repository;

import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeJpaRepository extends JpaRepository<ExchangeJpaEntity, Long> {
}
