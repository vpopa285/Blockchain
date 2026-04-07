package org.blockchain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AppTest {

    @Test
    void mainRunsWithoutExceptionTest() {
        assertThatCode(() -> {
            Thread t = new Thread(() -> {
                try {
                    App.main(new String[]{});
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            t.setDaemon(true);
            t.start();

            Thread.sleep(1000);
        }).doesNotThrowAnyException();
    }

}
