package ksh.tryptobackend.marketdata.application.port.in;

import java.util.Map;
import java.util.Set;

public interface FindCoinSymbolsUseCase {

    Map<Long, String> findSymbolsByIds(Set<Long> coinIds);
}
