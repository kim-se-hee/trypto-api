package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.application.port.out.dto.WithdrawalFeeInfo;

import java.util.Optional;

public interface WithdrawalFeeQueryPort {

    Optional<WithdrawalFeeInfo> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
