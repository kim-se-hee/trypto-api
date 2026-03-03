package ksh.tryptobackend.wallet.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Getter
@Builder
public class DepositAddress {

    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final Long depositAddressId;
    private final Long walletId;
    private final String chain;
    private final String address;
    private final String tag;

    public static DepositAddress create(Long walletId, String chain, boolean tagRequired) {
        String seed = walletId + ":" + chain;
        String address = generateHash(seed);
        String tag = tagRequired ? generateHash(seed + ":tag") : null;

        return DepositAddress.builder()
            .walletId(walletId)
            .chain(chain)
            .address(address)
            .tag(tag)
            .build();
    }

    private static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
