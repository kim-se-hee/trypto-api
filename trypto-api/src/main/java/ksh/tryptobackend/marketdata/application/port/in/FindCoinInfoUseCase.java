package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinInfoResult;

import java.util.Map;
import java.util.Set;

public interface FindCoinInfoUseCase {

    Map<Long, CoinInfoResult> findByIds(Set<Long> coinIds);
}
