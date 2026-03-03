package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressExchangeInfo;

public interface DepositAddressExchangePort {

    DepositAddressExchangeInfo getExchangeDetail(Long exchangeId);
}
