package org.blockchain;

import lombok.Getter;

import java.security.*;

public class Participant {

    @Getter
    private final String name;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final Blockchain blockchain;

    public Participant(String name, Blockchain blockchain) {
        this.name = name;
        this.blockchain = blockchain;

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair pair = kpg.generateKeyPair();

            this.publicKey = pair.getPublic();
            this.privateKey = pair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Participant to, int amount) {
        long txId = blockchain.nextTransactionId();
        String payload = txId + "|" + name + "|" + to.getName() + "|" + amount;
        byte[] signature = StringUtil.sign(payload, privateKey);

        Transaction tx = new Transaction(txId, name, to.getName(), amount, signature, publicKey);

        blockchain.submitTransaction(tx);
    }
}
