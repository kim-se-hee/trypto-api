package ksh.tryptobackend.investmentround.adapter.out.repository;

import ksh.tryptobackend.investmentround.adapter.out.entity.InvestmentRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRuleJpaRepository extends JpaRepository<InvestmentRuleJpaEntity, Long> {

    List<InvestmentRuleJpaEntity> findByRoundId(Long roundId);
}
