package ksh.tryptobackend.marketdata.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindCoinInfoUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinInfoResult;
import ksh.tryptobackend.marketdata.application.port.out.CoinQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindCoinInfoService implements FindCoinInfoUseCase {

    private final CoinQueryPort coinQueryPort;

    @Override
    public Map<Long, CoinInfoResult> findByIds(Set<Long> coinIds) {
        return coinQueryPort.findInfoByIds(coinIds).stream()
            .collect(Collectors.toMap(
                info -> info.coinId(),
                info -> new CoinInfoResult(info.symbol(), info.name())
            ));
    }
}
