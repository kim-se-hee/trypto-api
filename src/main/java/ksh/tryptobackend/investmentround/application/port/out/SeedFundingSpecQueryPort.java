package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.domain.vo.SeedFundingSpec;

import java.util.Optional;

public interface SeedFundingSpecQueryPort {

    Optional<SeedFundingSpec> findById(Long exchangeId);
}
