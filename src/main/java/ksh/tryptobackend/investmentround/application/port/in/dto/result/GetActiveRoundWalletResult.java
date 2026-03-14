package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;

public record GetActiveRoundWalletResult(Long walletId, Long exchangeId) {

    public static GetActiveRoundWalletResult from(WalletResult walletResult) {
        return new GetActiveRoundWalletResult(walletResult.walletId(), walletResult.exchangeId());
    }
}
