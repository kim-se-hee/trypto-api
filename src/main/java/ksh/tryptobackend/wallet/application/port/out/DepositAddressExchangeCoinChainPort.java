package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;

public interface DepositAddressExchangeCoinChainPort {

    DepositAddressChainInfo getExchangeCoinChain(Long exchangeId, Long coinId, String chain);
}
