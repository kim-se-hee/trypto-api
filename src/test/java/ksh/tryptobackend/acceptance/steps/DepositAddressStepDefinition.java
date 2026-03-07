package ksh.tryptobackend.acceptance.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ksh.tryptobackend.acceptance.testclient.CommonApiClient;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeJpaRepository;
import ksh.tryptobackend.marketdata.domain.model.ExchangeMarketType;
import ksh.tryptobackend.wallet.adapter.out.entity.WalletJpaEntity;
import ksh.tryptobackend.wallet.adapter.out.repository.DepositAddressJpaRepository;
import ksh.tryptobackend.wallet.adapter.out.repository.WalletJpaRepository;
import ksh.tryptobackend.wallet.domain.model.Wallet;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositAddressStepDefinition {

    private static final Long EXCHANGE_ID = 1L;
    private static final Long ROUND_ID = 1L;
    private static final Long KRW_COIN_ID = 1L;
    private static final Long BTC_COIN_ID = 2L;
    private static final Long XRP_COIN_ID = 3L;

    private final CommonApiClient apiClient;
    private final ExchangeJpaRepository exchangeJpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final DepositAddressJpaRepository depositAddressJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    private Long walletId;
    private String previousAddress;

    public DepositAddressStepDefinition(
        CommonApiClient apiClient,
        ExchangeJpaRepository exchangeJpaRepository,
        WalletJpaRepository walletJpaRepository,
        DepositAddressJpaRepository depositAddressJpaRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.apiClient = apiClient;
        this.exchangeJpaRepository = exchangeJpaRepository;
        this.walletJpaRepository = walletJpaRepository;
        this.depositAddressJpaRepository = depositAddressJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Before("@deposit-address")
    public void setUp() {
        depositAddressJpaRepository.deleteAllInBatch();
        walletJpaRepository.deleteAllInBatch();
        jdbcTemplate.update("DELETE FROM exchange_coin_chain");
        jdbcTemplate.update("DELETE FROM exchange_coin");
        exchangeJpaRepository.deleteAllInBatch();
        walletId = null;
        previousAddress = null;
    }

    @Given("입금 주소용 거래소와 지갑이 준비되어 있다")
    public void 입금_주소용_거래소와_지갑이_준비되어_있다() {
        exchangeJpaRepository.save(new ExchangeJpaEntity(
            EXCHANGE_ID, "Upbit", ExchangeMarketType.DOMESTIC,
            KRW_COIN_ID, new BigDecimal("0.0005")));

        Wallet wallet = Wallet.create(ROUND_ID, EXCHANGE_ID, BigDecimal.ZERO, LocalDateTime.now());
        WalletJpaEntity walletEntity = WalletJpaEntity.fromDomain(wallet);
        walletId = walletJpaRepository.save(walletEntity).getId();
    }

    @Given("업비트에서 BTC를 ERC-20 체인으로 지원한다")
    public void 업비트에서_BTC를_ERC20_체인으로_지원한다() {
        insertExchangeCoinChain(EXCHANGE_ID, BTC_COIN_ID, "ERC-20", false);
    }

    @Given("업비트에서 XRP를 Ripple 체인으로 지원하고 태그가 필요하다")
    public void 업비트에서_XRP를_Ripple_체인으로_지원하고_태그가_필요하다() {
        insertExchangeCoinChain(EXCHANGE_ID, XRP_COIN_ID, "Ripple", true);
    }

    @When("지갑의 BTC 입금 주소를 ERC-20 체인으로 조회한다")
    public void 지갑의_BTC_입금_주소를_ERC20_체인으로_조회한다() {
        savePreviousAddress();
        apiClient.get("/api/wallets/" + walletId + "/deposit-address?coinId=" + BTC_COIN_ID + "&chain=ERC-20");
    }

    @When("지갑의 XRP 입금 주소를 Ripple 체인으로 조회한다")
    public void 지갑의_XRP_입금_주소를_Ripple_체인으로_조회한다() {
        apiClient.get("/api/wallets/" + walletId + "/deposit-address?coinId=" + XRP_COIN_ID + "&chain=Ripple");
    }

    @When("지갑의 BTC 입금 주소를 Solana 체인으로 조회한다")
    public void 지갑의_BTC_입금_주소를_Solana_체인으로_조회한다() {
        apiClient.get("/api/wallets/" + walletId + "/deposit-address?coinId=" + BTC_COIN_ID + "&chain=Solana");
    }

    @When("지갑의 KRW 입금 주소를 ERC-20 체인으로 조회한다")
    public void 지갑의_KRW_입금_주소를_ERC20_체인으로_조회한다() {
        apiClient.get("/api/wallets/" + walletId + "/deposit-address?coinId=" + KRW_COIN_ID + "&chain=ERC-20");
    }

    @When("존재하지 않는 지갑의 입금 주소를 조회한다")
    public void 존재하지_않는_지갑의_입금_주소를_조회한다() {
        apiClient.get("/api/wallets/999999/deposit-address?coinId=" + BTC_COIN_ID + "&chain=ERC-20");
    }

    @Then("입금 주소의 chain은 {string}이다")
    public void 입금_주소의_chain은_이다(String chain) {
        apiClient.getLastResponse()
            .expectBody()
            .jsonPath("$.data.chain").isEqualTo(chain);
    }

    @Then("입금 주소의 address가 존재한다")
    public void 입금_주소의_address가_존재한다() {
        apiClient.getLastResponse()
            .expectBody()
            .jsonPath("$.data.address").isNotEmpty();
    }

    @Then("입금 주소의 tag는 존재하지 않는다")
    public void 입금_주소의_tag는_존재하지_않는다() {
        byte[] body = apiClient.getLastResponse()
            .expectBody().returnResult().getResponseBody();
        Object tag = com.jayway.jsonpath.JsonPath.read(new String(body), "$.data.tag");
        assertThat(tag).isNull();
    }

    @Then("입금 주소의 tag가 존재한다")
    public void 입금_주소의_tag가_존재한다() {
        apiClient.getLastResponse()
            .expectBody()
            .jsonPath("$.data.tag").isNotEmpty();
    }

    @Then("이전과 동일한 입금 주소가 반환된다")
    public void 이전과_동일한_입금_주소가_반환된다() {
        byte[] body = apiClient.getLastResponse()
            .expectBody().returnResult().getResponseBody();
        String currentAddress = com.jayway.jsonpath.JsonPath.read(new String(body), "$.data.address");
        assertThat(currentAddress).isEqualTo(previousAddress);
    }

    private void insertExchangeCoinChain(Long exchangeId, Long coinId, String chain, boolean tagRequired) {
        long exchangeCoinId = exchangeId * 100 + coinId;
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM exchange_coin WHERE exchange_coin_id = ?", Integer.class, exchangeCoinId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                "INSERT INTO exchange_coin (exchange_coin_id, exchange_id, coin_id) VALUES (?, ?, ?)",
                exchangeCoinId, exchangeId, coinId);
        }
        long chainId = exchangeCoinId * 100 + Math.abs(chain.hashCode() % 100);
        jdbcTemplate.update(
            "INSERT INTO exchange_coin_chain (exchange_coin_chain_id, exchange_coin_id, chain, tag_required) VALUES (?, ?, ?, ?)",
            chainId, exchangeCoinId, chain, tagRequired);
    }

    @SuppressWarnings("unchecked")
    private void savePreviousAddress() {
        if (previousAddress != null) {
            return;
        }
        try {
            byte[] body = apiClient.getLastResponse()
                .expectBody().returnResult().getResponseBody();
            if (body != null) {
                previousAddress = com.jayway.jsonpath.JsonPath.read(new String(body), "$.data.address");
            }
        } catch (Exception ignored) {
            // No previous response
        }
    }
}
