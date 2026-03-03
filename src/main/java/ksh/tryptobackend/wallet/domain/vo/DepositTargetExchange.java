package ksh.tryptobackend.wallet.domain.vo;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import lombok.Getter;

import java.util.Objects;

@Getter
public class DepositTargetExchange {

    private final Long baseCurrencyCoinId;
    private final String currency;

    private DepositTargetExchange(Long baseCurrencyCoinId, String currency) {
        this.baseCurrencyCoinId = baseCurrencyCoinId;
        this.currency = currency;
    }

    public static DepositTargetExchange of(Long baseCurrencyCoinId, String currency) {
        return new DepositTargetExchange(baseCurrencyCoinId, currency);
    }

    public void validateTransferable(Long coinId) {
        if (isFiatCurrency() && baseCurrencyCoinId.equals(coinId)) {
            throw new CustomException(ErrorCode.BASE_CURRENCY_NOT_TRANSFERABLE);
        }
    }

    private boolean isFiatCurrency() {
        return "KRW".equals(currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepositTargetExchange that = (DepositTargetExchange) o;
        return Objects.equals(baseCurrencyCoinId, that.baseCurrencyCoinId)
            && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseCurrencyCoinId, currency);
    }
}
