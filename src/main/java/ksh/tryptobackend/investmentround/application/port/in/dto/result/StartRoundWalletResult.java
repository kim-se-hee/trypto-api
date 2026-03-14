package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;

public record StartRoundWalletResult(Long walletId, Long exchangeId) {

    public static StartRoundWalletResult from(WalletResult walletResult) {
        return new StartRoundWalletResult(walletResult.walletId(), walletResult.exchangeId());
    }
}
