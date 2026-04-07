package org.blockchain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.*;

import static org.assertj.core.api.Assertions.*;

class TransactionTest {

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        keyPair = kpg.generateKeyPair();
    }

    private Transaction buildTransaction(long id, String from, String to, int amount) {
        String payload = id + "|" + from + "|" + to + "|" + amount;
        byte[] sig = StringUtil.sign(payload, keyPair.getPrivate());

        return new Transaction(id, from, to, amount, sig, keyPair.getPublic());
    }

    @Test
    void correctlySignedTransactionTest() {
        assertThat(buildTransaction(1, "Alice", "Bob", 50).isValid()).isTrue();
    }

    @Test
    void tamperedAmountTest() {
        String payload = 1 + "|Alice|Bob|" + 50;
        byte[] sig = StringUtil.sign(payload, keyPair.getPrivate());
        Transaction tampered = new Transaction(1, "Alice", "Bob", 999, sig, keyPair.getPublic());

        assertThat(tampered.isValid()).isFalse();
    }

    @Test
    void tamperedSenderTest() {
        byte[] sig = StringUtil.sign("1|Alice|Bob|50", keyPair.getPrivate());
        Transaction tampered = new Transaction(1, "Mallory", "Bob", 50, sig, keyPair.getPublic());

        assertThat(tampered.isValid()).isFalse();
    }

    @Test
    void tamperedRecipientTest() {
        byte[] sig = StringUtil.sign("1|Alice|Bob|50", keyPair.getPrivate());
        Transaction tampered = new Transaction(1, "Alice", "Mallory", 50, sig, keyPair.getPublic());

        assertThat(tampered.isValid()).isFalse();
    }

    @Test
    void wrongPublicKeyTest() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair otherPair = kpg.generateKeyPair();

        Transaction tx = buildTransaction(1, "Alice", "Bob", 50);
        Transaction wrongKey = new Transaction(
                tx.id(), tx.from(), tx.to(), tx.amount(), tx.signature(), otherPair.getPublic());

        assertThat(wrongKey.isValid()).isFalse();
    }

    @Test
    void signedPayloadTest() {
        assertThat(buildTransaction(1, "Alice", "Bob", 50).signedPayload()).isEqualTo("1|Alice|Bob|50");
    }

    @Test
    void containsSenderAmountAndRecipientTest() {
        assertThat(buildTransaction(1, "Alice", "Bob", 50).toString())
                .contains("Alice").contains("Bob").contains(String.valueOf(50)).contains("VC");
    }
}
