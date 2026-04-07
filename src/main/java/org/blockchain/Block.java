package org.blockchain;

import java.util.List;

public record Block(long id, long timestamp, String previousHash, long magic, String hash,
                    long generationSeconds, int minerId, String minerName, List<Transaction> transactions) {

    public static String contentToHash(long id, long timestamp, String previousHash,
                                       long magic, List<Transaction> transactions) {

        StringBuilder sb = new StringBuilder();
        sb.append(id).append(timestamp).append(previousHash).append(magic);

        for (Transaction t : transactions) {
            sb.append(t.id()).append(t.from()).append(t.to()).append(t.amount())
                    .append(StringUtil.toBase64(t.signature()));
        }

        return StringUtil.applySha256(sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Block:\n");

        sb.append("Created by: ").append(minerName).append('\n');
        sb.append(minerName).append(" gets 100 VC\n");

        sb.append("Id: ").append(id).append('\n');
        sb.append("Timestamp: ").append(timestamp).append('\n');
        sb.append("Magic number: ").append(magic).append('\n');
        sb.append("Hash of the previous block:\n").append(previousHash).append('\n');
        sb.append("Hash of the block:\n").append(hash).append('\n');

        sb.append("Block data:\n");
        if (transactions.isEmpty()) {
            sb.append("No transactions\n");
        } else {
            for (Transaction t : transactions) {
                sb.append(t).append('\n');
            }
        }

        sb.append("Block was generating for ").append(generationSeconds).append(" seconds");

        return sb.toString();
    }
}
