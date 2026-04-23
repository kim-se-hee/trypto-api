package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.WithdrawalFeeResult;

import java.util.Optional;

public interface FindWithdrawalFeeUseCase {

    Optional<WithdrawalFeeResult> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
