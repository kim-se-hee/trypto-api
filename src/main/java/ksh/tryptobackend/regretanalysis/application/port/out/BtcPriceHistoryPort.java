package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.application.port.out.dto.BtcDailyPrice;

import java.time.LocalDate;
import java.util.List;

public interface BtcPriceHistoryPort {

    List<BtcDailyPrice> findBtcDailyPrices(LocalDate startDate, LocalDate endDate, String currency);
}
