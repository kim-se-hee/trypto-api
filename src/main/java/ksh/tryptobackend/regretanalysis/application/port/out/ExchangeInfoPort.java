package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeInfoRecord;

public interface ExchangeInfoPort {

    ExchangeInfoRecord getExchangeInfo(Long exchangeId);
}
