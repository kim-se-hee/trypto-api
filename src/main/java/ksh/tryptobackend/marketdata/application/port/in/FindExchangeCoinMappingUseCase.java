package ksh.tryptobackend.marketdata.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FindExchangeCoinMappingUseCase {

    Optional<Long> findExchangeCoinId(Long exchangeId, Long coinId);

    Map<Long, Long> findExchangeCoinIdMap(Long exchangeId, List<Long> coinIds);
}
