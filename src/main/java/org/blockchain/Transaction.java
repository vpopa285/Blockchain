package org.blockchain;

import java.security.PublicKey;

public record Transaction(long id, String from, String to, int amount, byte[] signature, PublicKey publicKey) {

    public String signedPayload() {
        return id + "|" + from + "|" + to + "|" + amount;
    }

    public boolean isValid() {
        return StringUtil.verify(signedPayload(), signature, publicKey);
    }

    @Override
    public String toString() {
        return from + " sent " + amount + " VC to " + to;
    }
}
