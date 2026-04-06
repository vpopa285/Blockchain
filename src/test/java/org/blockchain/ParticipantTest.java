package org.blockchain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantTest {

    @Mock
    private Blockchain blockchainMock;

    @Test
    void getNameTest() {
        Participant p = new Participant("Test", blockchainMock);

        assertThat(p.getName()).isEqualTo("Test");
    }

    @Test
    void submittingTransactionToBlockchainTest() {
        when(blockchainMock.nextTransactionId()).thenReturn(1L);

        Participant alice = new Participant("Alice", blockchainMock);
        Participant bob   = new Participant("Bob",   blockchainMock);
        alice.send(bob, 50);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(blockchainMock).submitTransaction(captor.capture());

        Transaction submitted = captor.getValue();

        assertThat(submitted.from()).isEqualTo("Alice");
        assertThat(submitted.to()).isEqualTo("Bob");
        assertThat(submitted.amount()).isEqualTo(50);
        assertThat(submitted.id()).isEqualTo(1L);
    }

    @Test
    void validitySignatureTest() {
        when(blockchainMock.nextTransactionId()).thenReturn(42L);

        Participant sender   = new Participant("Nick", blockchainMock);
        Participant receiver = new Participant("CarShop", blockchainMock);
        sender.send(receiver, 15);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(blockchainMock).submitTransaction(captor.capture());

        assertThat(captor.getValue().isValid()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"Alice, Bob, 1, 10",
            "miner1, miner2, 5, 99",
            "miner1, CarShop, 7, 15"})
    void correctFieldsTest(String fromName, String toName, long txId, int amount) {
        when(blockchainMock.nextTransactionId()).thenReturn(txId);

        Participant from = new Participant(fromName, blockchainMock);
        Participant to   = new Participant(toName,   blockchainMock);
        from.send(to, amount);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(blockchainMock).submitTransaction(captor.capture());

        Transaction tx = captor.getValue();

        assertThat(tx.from()).isEqualTo(fromName);
        assertThat(tx.to()).isEqualTo(toName);
        assertThat(tx.amount()).isEqualTo(amount);
        assertThat(tx.id()).isEqualTo(txId);
        assertThat(tx.isValid()).isTrue();
    }

    @Test
    void callsNextTransactionTest() {
        when(blockchainMock.nextTransactionId()).thenReturn(1L);

        Participant a = new Participant("A", blockchainMock);
        Participant b = new Participant("B", blockchainMock);
        a.send(b, 10);

        verify(blockchainMock, times(1)).nextTransactionId();
    }

    @Test
    void calledMultipleTimesTest() {
        when(blockchainMock.nextTransactionId()).thenReturn(1L, 2L, 3L);

        Participant a = new Participant("A", blockchainMock);
        Participant b = new Participant("B", blockchainMock);

        a.send(b, 10);
        a.send(b, 20);
        a.send(b, 30);

        verify(blockchainMock, times(3)).submitTransaction(any());
    }

}
