package ksh.tryptobackend.marketdata.adapter.out.repository;

import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeJpaRepository extends JpaRepository<ExchangeJpaEntity, Long> {

    Optional<ExchangeJpaEntity> findByName(String name);
}
