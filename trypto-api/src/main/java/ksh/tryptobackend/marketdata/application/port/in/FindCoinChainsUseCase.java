package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinChainResult;

import java.util.List;

public interface FindCoinChainsUseCase {

    List<CoinChainResult> findCoinChains(Long exchangeId, Long coinId);
}
