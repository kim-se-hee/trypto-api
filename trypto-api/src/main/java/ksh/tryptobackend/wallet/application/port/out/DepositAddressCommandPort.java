package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.domain.model.DepositAddress;

public interface DepositAddressCommandPort {

    DepositAddress save(DepositAddress depositAddress);
}
