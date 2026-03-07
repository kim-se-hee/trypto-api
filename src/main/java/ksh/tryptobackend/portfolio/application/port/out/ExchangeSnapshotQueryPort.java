package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.vo.ExchangeSnapshot;

public interface ExchangeSnapshotQueryPort {

    ExchangeSnapshot getExchangeInfo(Long exchangeId);
}
