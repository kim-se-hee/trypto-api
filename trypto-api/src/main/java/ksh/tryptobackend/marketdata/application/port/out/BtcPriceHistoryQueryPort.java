package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.vo.DailyClosePrice;

import java.time.LocalDate;
import java.util.List;

public interface BtcPriceHistoryQueryPort {

    List<DailyClosePrice> findBtcDailyPrices(LocalDate startDate, LocalDate endDate, String currency);
}
