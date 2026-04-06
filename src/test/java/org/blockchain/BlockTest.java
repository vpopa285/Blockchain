package org.blockchain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BlockTest {

    private static Transaction makeValidTx(long id, String from, String to, int amount) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();

            String payload = id + "|" + from + "|" + to + "|" + amount;
            byte[] sig = StringUtil.sign(payload, kp.getPrivate());

            return new Transaction(id, from, to, amount, sig, kp.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static Block makeBlock(long id, String prevHash, long magic, List<Transaction> txs) {
        String hash = Block.contentToHash(id, System.currentTimeMillis(), prevHash, magic, txs);

        return new Block(id, System.currentTimeMillis(), prevHash, magic, hash, 1L, 1, "miner1", txs);
    }

    @Test
    void sameInputsTest() {
        String h1 = Block.contentToHash(1L, 1000L, "prevHash", 42L, new ArrayList<>());
        String h2 = Block.contentToHash(1L, 1000L, "prevHash", 42L, new ArrayList<>());

        assertThat(h1).isEqualTo(h2).hasSize(64);
    }

    @ParameterizedTest
    @CsvSource({
            "id, 2, 1000, prevHash, 42",
            "timestamp, 1, 9999, prevHash, 42",
            "magic, 1, 1000, prevHash, 99"
    })
    void changingAnyFieldTest(String changedField, long id, long ts, String prev, long magic) {
        String base = Block.contentToHash(1L, 1000L, "prevHash", 42L, new ArrayList<>());
        String variant = Block.contentToHash(id, ts, prev, magic, new ArrayList<>());

        assertThat(variant).isNotEqualTo(base);
    }

    @Test
    void changingPreviousHashTest() {
        String h1 = Block.contentToHash(1L, 1000L, "hashA", 42L, new ArrayList<>());
        String h2 = Block.contentToHash(1L, 1000L, "hashB", 42L, new ArrayList<>());

        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void contentToHashWithTransactionsTest() {
        Transaction tx = makeValidTx(1, "Alice", "Bob", 10);
        String withTx = Block.contentToHash(1L, 1000L, "prev", 42L, List.of(tx));
        String withoutTx = Block.contentToHash(1L, 1000L, "prev", 42L, new ArrayList<>());

        assertThat(withTx).isNotEqualTo(withoutTx);
    }

    @Test
    void resultIs64CharTest() {
        String hash = Block.contentToHash(1L, 1000L, "prev", 42L, new ArrayList<>());

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void noTransactionsTest() {
        assertThat(makeBlock(1L, "0", 42L, new ArrayList<>()).toString())
                .contains("No transactions");
    }

    @Test
    void allTransactionsTest() {
        Transaction tx1 = makeValidTx(1, "Alice", "Bob", 10);
        Transaction tx2 = makeValidTx(2, "Bob", "CarShop", 5);
        String str = makeBlock(1L, "0", 42L, List.of(tx1, tx2)).toString();

        assertThat(str).contains("Alice").contains("Bob").contains("CarShop").doesNotContain("No transactions");
    }

    @Test
    void structuralTest() {
        String str = makeBlock(7L, "somePrevHash", 12345L, new ArrayList<>()).toString();

        assertThat(str)
                .contains("Block:")
                .contains("Id: 7")
                .contains("somePrevHash")
                .contains("miner1")
                .contains("100 VC")
                .contains("Block data:")
                .contains("Magic number: 12345");
    }

    @Test
    void showsCorrectBlockIdTest() {
        Block block = new Block(1, 0L, "0", 0L, "hash", 0L, 1, "miner1", new ArrayList<>());

        assertThat(block.toString()).contains("Id: " + 1);
    }
}
