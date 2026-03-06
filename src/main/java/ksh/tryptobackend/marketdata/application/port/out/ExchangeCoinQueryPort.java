package ksh.tryptobackend.marketdata.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExchangeCoinQueryPort {

    Optional<Long> findExchangeCoinId(Long exchangeId, Long coinId);

    Map<Long, Long> findExchangeCoinIdMap(Long exchangeId, List<Long> coinIds);
}
