package ksh.tryptobackend.regretanalysis.application.port.out;

import java.util.Map;
import java.util.Set;

public interface CoinSymbolPort {

    Map<Long, String> findSymbolsByIds(Set<Long> coinIds);

    Map<Long, String> findSymbolsByExchangeCoinIds(Set<Long> exchangeCoinIds);

    Map<Long, Long> findCoinIdsByExchangeCoinIds(Set<Long> exchangeCoinIds);
}
