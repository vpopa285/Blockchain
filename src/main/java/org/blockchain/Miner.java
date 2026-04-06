package org.blockchain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Miner implements Runnable {

    private final int id;
    private final String name;
    private final Blockchain blockchain;

    public Miner(int id, Blockchain blockchain) {
        this.id = id;
        this.name = "miner" + id;
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        while (!blockchain.isFinished()) {
            Block last = blockchain.getLastBlock();

            int difficulty = blockchain.getZeroNumber();

            List<Transaction> txSnapshot = blockchain.getPending();

            Block candidate = mine(last, difficulty, txSnapshot);
            blockchain.addBlock(candidate, id, name);
        }
    }

    private Block mine(Block last, int difficulty, List<Transaction> transactions) {
        String prefix = "0".repeat(difficulty);
        long nextId = last.id() + 1;
        long timestamp = System.currentTimeMillis();
        String prevHash = last.hash();

        Instant start = Instant.now();

        while (true) {
            long magic = ThreadLocalRandom.current().nextLong();
            String hash = Block.contentToHash(nextId, timestamp, prevHash, magic, transactions);

            if (hash.startsWith(prefix)) {
                long duration = Duration.between(start, Instant.now()).toSeconds();

                return new Block(nextId, timestamp, prevHash, magic, hash, duration, id, name, transactions);
            }
        }
    }
}
