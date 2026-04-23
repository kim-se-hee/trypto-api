package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.BtcDailyPriceResult;

import java.time.LocalDate;
import java.util.List;

public interface FindBtcDailyPricesUseCase {

    List<BtcDailyPriceResult> findBtcDailyPrices(LocalDate startDate, LocalDate endDate, String currency);
}
