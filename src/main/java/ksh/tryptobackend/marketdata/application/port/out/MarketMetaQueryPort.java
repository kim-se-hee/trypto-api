package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.vo.MarketMetaEntry;

import java.util.List;
import java.util.Map;

public interface MarketMetaQueryPort {

    Map<String, List<MarketMetaEntry>> findAll();
}
