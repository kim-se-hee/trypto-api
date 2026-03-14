package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.WithdrawalFee;

import java.util.Optional;

public interface WithdrawalFeeQueryPort {

    Optional<WithdrawalFee> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
