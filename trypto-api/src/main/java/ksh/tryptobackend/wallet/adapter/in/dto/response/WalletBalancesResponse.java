package ksh.tryptobackend.wallet.adapter.in.dto.response;

import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletBalancesResult;

import java.math.BigDecimal;
import java.util.List;

public record WalletBalancesResponse(
    Long exchangeId,
    String baseCurrencySymbol,
    BigDecimal baseCurrencyAvailable,
    BigDecimal baseCurrencyLocked,
    List<CoinBalanceResponse> balances
) {

    public record CoinBalanceResponse(Long coinId, BigDecimal available, BigDecimal locked) {
    }

    public static WalletBalancesResponse from(WalletBalancesResult result) {
        List<CoinBalanceResponse> balances = result.balances().stream()
            .map(b -> new CoinBalanceResponse(b.coinId(), b.available(), b.locked()))
            .toList();

        return new WalletBalancesResponse(
            result.exchangeId(),
            result.baseCurrencySymbol(),
            result.baseCurrencyAvailable(),
            result.baseCurrencyLocked(),
            balances
        );
    }
}
