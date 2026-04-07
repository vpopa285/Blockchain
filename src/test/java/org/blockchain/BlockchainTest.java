package org.blockchain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

class BlockchainTest {

    private Blockchain blockchain;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        blockchain = new Blockchain(latch);
    }

    private static Transaction makeTx(long id, String from, String to, int amount) {
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

    private Block mineNext(Blockchain bc, List<Transaction> txs) {
        Block last = bc.getLastBlock();
        String prefix = "0".repeat(bc.getZeroNumber());
        long nextId = last.id() + 1;
        long ts = System.currentTimeMillis();
        String prevHash = last.hash();

        long magic = 0;
        String hash;
        do {
            magic++;
            hash = Block.contentToHash(nextId, ts, prevHash, magic, txs);
        } while (!hash.startsWith(prefix));

        return new Block(nextId, ts, prevHash, magic, hash, 0L, 1, "miner1", txs);
    }

    @Test
    void getLastBlockTest() {
        Block genesis = blockchain.getLastBlock();

        assertThat(genesis.id()).isZero();
        assertThat(genesis.hash()).isEqualTo("0");
        assertThat(genesis.transactions()).isEmpty();
    }

    @Test
    void isFinishedInitiallyTest() {
        assertThat(blockchain.isFinished()).isFalse();
    }

    @Test
    void isFinishedCountdownTest() {
        latch.countDown();
        assertThat(blockchain.isFinished()).isTrue();
    }

    @Test
    void transactionIdFirstCallTest() {
        assertThat(blockchain.nextTransactionId()).isEqualTo(1L);
    }

    @Test
    void transactionIdSequentialCallsTest() {
        long prev = blockchain.nextTransactionId();

        for (int i = 0; i < 9; i++) {
            long next = blockchain.nextTransactionId();
            assertThat(next).isGreaterThan(prev);
            prev = next;
        }
    }

    @Test
    void submitTransactionValidTest() {
        Transaction tx = makeTx(1, "Alice", "Bob", 10);
        blockchain.submitTransaction(tx);

        assertThat(blockchain.getPending()).contains(tx);
    }

    @Test
    void submitTransactionInvalidTest() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        byte[] sig = StringUtil.sign("garbage", kp.getPrivate());
        KeyPair otherKp = kpg.generateKeyPair();

        Transaction invalidTx = new Transaction(1, "Alice", "Bob", 10, sig, otherKp.getPublic());
        blockchain.submitTransaction(invalidTx);

        assertThat(blockchain.getPending()).doesNotContain(invalidTx);
    }

    @Test
    void submitTransactionInsufficientBalanceTest() {
        Transaction tx = makeTx(1, "Alice", "Bob", 999);
        blockchain.submitTransaction(tx);

        assertThat(blockchain.getPending()).doesNotContain(tx);
    }

    @Test
    void submitTransactionFinishedTest() {
        latch.countDown();
        Transaction tx = makeTx(1, "Alice", "Bob", 10);
        blockchain.submitTransaction(tx);

        assertThat(blockchain.getPending()).doesNotContain(tx);
    }

    @Test
    void submitTransactionDuplicateIdTest() {
        Transaction tx1 = makeTx(1, "Alice", "Bob", 10);
        Transaction tx2 = makeTx(1, "Bob", "Carol", 5);

        blockchain.submitTransaction(tx1);
        blockchain.submitTransaction(tx2);

        assertThat(blockchain.getPending())
                .contains(tx1)
                .doesNotContain(tx2);
    }

    @Test
    void submitTransactionAcceptedTest() {
        Transaction tx = makeTx(1, "Alice", "Bob", 99);
        blockchain.submitTransaction(tx);

        assertThat(blockchain.getPending()).contains(tx);
    }

    @Test
    void submitTransactionExceedsBalanceTest() {
        Transaction tx = makeTx(1, "Alice", "Bob", 101);
        blockchain.submitTransaction(tx);

        assertThat(blockchain.getPending()).doesNotContain(tx);
    }

    @Test
    void validBlockTest() {
        Block candidate = mineNext(blockchain, new ArrayList<>());
        blockchain.addBlock(candidate, 1, "miner1");

        assertThat(blockchain.getLastBlock().id()).isEqualTo(1L);
    }

    @Test
    void validBlockWithTransactionsTest() {
        Transaction tx = makeTx(blockchain.nextTransactionId(), "Alice", "Bob", 10);
        blockchain.submitTransaction(tx);

        Block candidate = mineNext(blockchain, List.of(tx));
        blockchain.addBlock(candidate, 1, "miner1");

        assertThat(blockchain.getPending()).doesNotContain(tx);
    }

    @Test
    void wrongPreviousHashTest() {
        String wrongPrev = "0000000000000000000000000000000000000000000000000000000000000000";
        String hash = Block.contentToHash(1L, System.currentTimeMillis(), wrongPrev, 1L, new ArrayList<>());
        Block bad = new Block(1L, System.currentTimeMillis(), wrongPrev, 1L, hash, 0L, 1, "miner1", new ArrayList<>());

        blockchain.addBlock(bad, 1, "miner1");

        assertThat(blockchain.getLastBlock().id()).isEqualTo(0L);
    }

    @Test
    void hashDoesNotMatchContentTest() {
        Block last = blockchain.getLastBlock();
        Block bad = new Block(1L, System.currentTimeMillis(), last.hash(), 99L,
                "fake_hash_value", 0L, 1, "miner1", new ArrayList<>());

        blockchain.addBlock(bad, 1, "miner1");

        assertThat(blockchain.getLastBlock().id()).isEqualTo(0L);
    }

    @Test
    void transactionNotInPendingTest() {
        Transaction tx = makeTx(1, "Alice", "Bob", 10);
        Block candidate = mineNext(blockchain, List.of(tx));

        blockchain.addBlock(candidate, 1, "miner1");

        assertThat(blockchain.getLastBlock().id()).isEqualTo(0L);
    }

    @Test
    void afterFinishedTest() {
        latch.countDown();

        Block candidate = mineNext(blockchain, new ArrayList<>());
        blockchain.addBlock(candidate, 1, "miner1");

        assertThat(blockchain.getLastBlock().id()).isEqualTo(0L);
    }

    @Test
    void getZeroNumberTest() {
        assertThat(blockchain.getZeroNumber()).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 0",
            "59, 0",
            "61, -1"
    })
    void increasingDifficultyTest(int generationSeconds, int expectedDiff) {
        int before = blockchain.getZeroNumber();

        Block last = blockchain.getLastBlock();

        String hash = Block.contentToHash(1L, last.timestamp(), last.hash(), 1L, new ArrayList<>());

        Block block = new Block(1L, last.timestamp(), last.hash(), 1L,
                hash, generationSeconds, 1, "miner1", new ArrayList<>());

        blockchain.addBlock(block, 1, "miner1");

        int after = blockchain.getZeroNumber();
        int expected = Math.max(0, before + expectedDiff);

        assertThat(after).isEqualTo(expected);
    }

}
