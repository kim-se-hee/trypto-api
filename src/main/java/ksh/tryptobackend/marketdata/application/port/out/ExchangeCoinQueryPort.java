package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.ExchangeCoin;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeCoinIdMap;

import java.util.List;
import java.util.Optional;

public interface ExchangeCoinQueryPort {

    Optional<ExchangeCoin> findById(Long exchangeCoinId);

    ExchangeCoinIdMap findExchangeCoinIdMap(Long exchangeId, List<Long> coinIds);
}
