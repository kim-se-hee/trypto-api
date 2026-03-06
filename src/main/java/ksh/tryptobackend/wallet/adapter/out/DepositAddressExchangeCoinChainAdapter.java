package ksh.tryptobackend.wallet.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinChainUseCase;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainPort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositAddressExchangeCoinChainAdapter implements DepositAddressExchangeCoinChainPort {

    private final FindExchangeCoinChainUseCase findExchangeCoinChainUseCase;

    @Override
    public DepositAddressChainInfo getExchangeCoinChain(Long exchangeId, Long coinId, String chain) {
        return findExchangeCoinChainUseCase.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(result -> new DepositAddressChainInfo(result.tagRequired()))
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_CHAIN));
    }
}
