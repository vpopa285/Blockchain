package org.blockchain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.*;

import static org.assertj.core.api.Assertions.*;

class StringUtilTest {

    @Test
    void returnExpectedHashTest() {
        assertThat(StringUtil.applySha256("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                .hasSize(64)
                .matches("[0-9a-f]+");
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "0", "input with spaces", "1234567890"})
    void rightSizeReturnTest(String input) {
        assertThat(StringUtil.applySha256(input))
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void sameInputSameHashTest() {
        assertThat(StringUtil.applySha256("a"))
                .isEqualTo(StringUtil.applySha256("a"));
    }

    @Test
    void differentInputDifferentHashTest() {
        assertThat(StringUtil.applySha256("a"))
                .isNotEqualTo(StringUtil.applySha256("b"));
    }

    @Test
    void emptyStringValidHashTest() {
        assertThat(StringUtil.applySha256(""))
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);

        return kpg.generateKeyPair();
    }

    @Test
    void validSignatureTest() throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] signature = StringUtil.sign("hello", kp.getPrivate());

        assertThat(StringUtil.verify("hello", signature, kp.getPublic())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1|Alice|Bob|50", "99|miner1|miner2|100", "0|x|y|1"})
    void transactionPayloadsTest(String payload) throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] sig = StringUtil.sign(payload, kp.getPrivate());

        assertThat(StringUtil.verify(payload, sig, kp.getPublic())).isTrue();
    }

    @Test
    void tamperedDataTest() throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] sig = StringUtil.sign("original", kp.getPrivate());

        assertThat(StringUtil.verify("tampered", sig, kp.getPublic())).isFalse();
    }

    @Test
    void wrongPublicKeyTest() throws Exception {
        KeyPair kp1 = generateKeyPair();
        KeyPair kp2 = generateKeyPair();
        byte[] sig = StringUtil.sign("data", kp1.getPrivate());

        assertThat(StringUtil.verify("data", sig, kp2.getPublic())).isFalse();
    }

    @Test
    void emptyArrayTest() {
        assertThat(StringUtil.toBase64(new byte[0])).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 16, 64, 128})
    void variousLengthsTest(int length) {
        byte[] data = new byte[length];

        for (int i = 0; i < length; i++) {
            data[i] = (byte) i;
        }

        assertThat(StringUtil.toBase64(data)).isNotBlank().matches("[A-Za-z0-9+/=]+");
    }

    @Test
    void nullPublicKeyTest() {
        boolean result = StringUtil.verify("data", new byte[]{1,2,3}, null);

        assertThat(result).isFalse();
    }

}
