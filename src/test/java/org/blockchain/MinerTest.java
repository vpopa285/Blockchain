package org.blockchain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinerTest {

    @Mock
    private Blockchain blockchainMock;

    @Test
    void finishedBlockchainNeverGetLastBlock() {
        when(blockchainMock.isFinished()).thenReturn(true);

        new Miner(1, blockchainMock).run();

        verify(blockchainMock, never()).getLastBlock();
    }

    @Test
    void finishedBlockchainNeverAddBlock() {
        when(blockchainMock.isFinished()).thenReturn(true);

        new Miner(1, blockchainMock).run();

        verify(blockchainMock, never()).addBlock(any(), anyInt(), anyString());
    }

    @Test
    void blockchainNotFinishedGetLastBlockTest() {
        when(blockchainMock.isFinished()).thenReturn(false, true);
        when(blockchainMock.getLastBlock()).thenReturn(mock(Block.class));
        when(blockchainMock.getZeroNumber()).thenReturn(0);
        when(blockchainMock.getPending()).thenReturn(java.util.Collections.emptyList());

        new Miner(1, blockchainMock).run();

        verify(blockchainMock, atLeastOnce()).getLastBlock();
    }

    @Test
    void blockchainNotFinishedAddBlockTest() {
        when(blockchainMock.isFinished()).thenReturn(false, true);

        Block last = mock(Block.class);
        when(last.id()).thenReturn(0L);
        when(last.hash()).thenReturn("0");

        when(blockchainMock.getLastBlock()).thenReturn(last);
        when(blockchainMock.getZeroNumber()).thenReturn(0);
        when(blockchainMock.getPending()).thenReturn(java.util.Collections.emptyList());

        new Miner(1, blockchainMock).run();

        verify(blockchainMock, atLeastOnce()).addBlock(any(), eq(1), eq("miner1"));
    }

    @Test
    void miningSetsCorrectNameTest() {
        when(blockchainMock.isFinished()).thenReturn(false, true);

        Block last = mock(Block.class);
        when(last.id()).thenReturn(0L);
        when(last.hash()).thenReturn("0");

        when(blockchainMock.getLastBlock()).thenReturn(last);
        when(blockchainMock.getZeroNumber()).thenReturn(0);
        when(blockchainMock.getPending()).thenReturn(java.util.Collections.emptyList());

        new Miner(5, blockchainMock).run();

        verify(blockchainMock).addBlock(any(), eq(5), eq("miner5"));
    }

}
