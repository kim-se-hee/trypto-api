package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExchangePort {

    Optional<ExchangeData> findById(Long exchangeId);

    record ExchangeData(
        Long exchangeId,
        BigDecimal feeRate,
        Long baseCurrencyCoinId,
        String baseCurrencySymbol
    ) {
    }
}
