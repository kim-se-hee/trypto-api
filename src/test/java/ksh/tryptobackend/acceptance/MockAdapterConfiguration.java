package ksh.tryptobackend.acceptance;

import ksh.tryptobackend.acceptance.mock.MockExchangeAdapter;
import ksh.tryptobackend.acceptance.mock.MockExchangeCoinAdapter;
import ksh.tryptobackend.acceptance.mock.MockLivePriceAdapter;
import ksh.tryptobackend.acceptance.mock.MockWalletBalanceAdapter;
import ksh.tryptobackend.trading.application.port.out.ExchangeCoinPort;
import ksh.tryptobackend.trading.application.port.out.ExchangePort;
import ksh.tryptobackend.trading.application.port.out.LivePricePort;
import ksh.tryptobackend.trading.application.port.out.WalletBalancePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockAdapterConfiguration {

    @Bean
    @Primary
    public WalletBalancePort walletBalancePort() {
        return new MockWalletBalanceAdapter();
    }

    @Bean
    @Primary
    public LivePricePort livePricePort() {
        return new MockLivePriceAdapter();
    }

    @Bean
    @Primary
    public ExchangePort exchangePort() {
        return new MockExchangeAdapter();
    }

    @Bean
    @Primary
    public ExchangeCoinPort exchangeCoinPort() {
        return new MockExchangeCoinAdapter();
    }
}
