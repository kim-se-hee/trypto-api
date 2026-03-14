package ksh.tryptobackend.wallet.application.port.out;

import ksh.tryptobackend.wallet.domain.model.DepositAddress;

import java.util.Optional;

public interface DepositAddressQueryPort {

    Optional<DepositAddress> findByWalletIdAndChain(Long walletId, String chain);

    Optional<DepositAddress> findByRoundIdAndChainAndAddress(Long roundId, String chain, String address);
}
