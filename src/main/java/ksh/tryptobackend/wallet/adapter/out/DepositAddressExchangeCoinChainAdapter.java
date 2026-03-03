package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeCoinChainQueryPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainPort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositAddressExchangeCoinChainAdapter implements DepositAddressExchangeCoinChainPort {

    private final ExchangeCoinChainQueryPort exchangeCoinChainQueryPort;

    @Override
    public DepositAddressChainInfo getExchangeCoinChain(Long exchangeId, Long coinId, String chain) {
        return exchangeCoinChainQueryPort.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(info -> new DepositAddressChainInfo(info.tagRequired()))
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_CHAIN));
    }
}
