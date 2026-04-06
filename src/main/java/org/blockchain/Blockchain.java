package org.blockchain;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Blockchain {

    private static final int TARGET_BLOCKS = 15;
    private static final int MINER_REWARD = 100;

    private final List<Block> chain = new ArrayList<>();
    private final AtomicInteger zeroNumber = new AtomicInteger(0);
    private final CountDownLatch finishLatch;

    private final List<Transaction> pending = new ArrayList<>();
    private long maxConfirmedTxId = 0L;
    private long maxPendingTxId = 0L;

    private final AtomicLong txIdCounter = new AtomicLong(1);

    private final Map<String, Integer> balances = new HashMap<>();

    public Blockchain(CountDownLatch finishLatch) {
        this.finishLatch = finishLatch;
    }

    public synchronized List<Transaction> getPending() {
        return List.copyOf(pending);
    }

    public synchronized void addBlock(Block candidate, int minerId, String minerName) {
        if (isFinished()) {
            return;
        }

        Block last = getLastBlock();

        if (!candidate.previousHash().equals(last.hash())) {
            return;
        }

        if (!candidate.hash().startsWith("0".repeat(zeroNumber.get()))) {
            return;
        }

        List<Transaction> minerTxList = candidate.transactions();
        String expected = Block.contentToHash(
                candidate.id(), candidate.timestamp(),
                candidate.previousHash(), candidate.magic(), minerTxList);

        if (!candidate.hash().equals(expected)) {
            return;
        }

        if (!new HashSet<>(pending).containsAll(minerTxList)) {
            return;
        }

        Block finalBlock = new Block(
                candidate.id(), candidate.timestamp(),
                candidate.previousHash(), candidate.magic(),
                candidate.hash(), candidate.generationSeconds(),
                minerId, minerName,
                Collections.unmodifiableList(minerTxList));

        chain.add(finalBlock);

        balances.merge(minerName, MINER_REWARD, Integer::sum);

        for (Transaction tx : minerTxList) {
            if (tx.id() > maxConfirmedTxId) {
                maxConfirmedTxId = tx.id();
            }
        }

        pending.removeAll(minerTxList);
        if (maxPendingTxId < maxConfirmedTxId) {
            maxPendingTxId = maxConfirmedTxId;
        }

        String nMsg = adjustDifficulty(finalBlock.generationSeconds());
        System.out.println(finalBlock);
        System.out.println(nMsg);
        System.out.println();

        if (chain.size() >= TARGET_BLOCKS) {
            finishLatch.countDown();
        }
    }

    public synchronized Block getLastBlock() {
        if (chain.isEmpty()) {
            return new Block(0L, 0L, "0", 0L, "0", 0L, 0, "", new ArrayList<>());
        }
        return chain.get(chain.size() - 1);
    }

    public boolean isFinished() {
        return finishLatch.getCount() == 0;
    }

    public int getZeroNumber() {
        return zeroNumber.get();
    }

    public long nextTransactionId() {
        return txIdCounter.getAndIncrement();
    }

    public synchronized void submitTransaction(Transaction tx) {
        if (isFinished()) {
            return;
        }

        if (!tx.isValid()) {
            return;
        }

        if (tx.id() <= maxPendingTxId) {
            return;
        }

        int senderBal = balances.getOrDefault(tx.from(), 100);
        if (senderBal < tx.amount()) {
            return;
        }

        balances.put(tx.from(), senderBal - tx.amount());
        pending.add(tx);
        maxPendingTxId = tx.id();
    }

    private String adjustDifficulty(long seconds) {
        if (seconds < 1) {
            return "N was increased to " + zeroNumber.incrementAndGet();
        } else if (seconds > 60) {
            zeroNumber.updateAndGet(n -> Math.max(0, n - 1));
            return "N was decreased by 1";
        } else {
            return "N stays the same";
        }
    }
}
