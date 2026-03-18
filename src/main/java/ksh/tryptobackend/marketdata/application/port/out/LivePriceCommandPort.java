package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.vo.LiveTicker;

public interface LivePriceCommandPort {

    void send(Long exchangeId, LiveTicker liveTicker);
}
