package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.vo.ExchangeConfig;

import java.util.List;

public interface ExchangeConfigQueryPort {

    List<ExchangeConfig> findAll();
}
