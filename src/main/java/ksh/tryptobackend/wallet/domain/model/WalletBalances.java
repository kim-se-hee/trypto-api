package ksh.tryptobackend.wallet.domain.model;

import java.util.List;
import java.util.Optional;

public class WalletBalances {

    private final List<WalletBalance> values;

    public WalletBalances(List<WalletBalance> values) {
        this.values = List.copyOf(values);
    }

    public Optional<WalletBalance> findBaseCurrency(Long baseCurrencyCoinId) {
        return values.stream()
            .filter(balance -> balance.getCoinId().equals(baseCurrencyCoinId))
            .findFirst();
    }

    public List<WalletBalance> findCoinBalances(Long baseCurrencyCoinId) {
        return values.stream()
            .filter(balance -> !balance.getCoinId().equals(baseCurrencyCoinId))
            .toList();
    }
}
