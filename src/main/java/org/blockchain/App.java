package org.blockchain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static final int MINER_COUNT = 10;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch finishLatch = new CountDownLatch(1);
        Blockchain blockchain = new Blockchain(finishLatch);

        Participant[] miners = new Participant[MINER_COUNT + 1];
        for (int i = 1; i <= MINER_COUNT; i++) {
            miners[i] = new Participant("miner" + i, blockchain);
        }
        Participant nick = new Participant("Nick", blockchain);
        Participant bob = new Participant("Bob", blockchain);
        Participant alice = new Participant("Alice", blockchain);
        Participant carShop = new Participant("CarShop", blockchain);
        Participant gaming = new Participant("GamingShop", blockchain);
        Participant beauty = new Participant("BeautyShop", blockchain);
        Participant shoesShop = new Participant("ShoesShop", blockchain);
        Participant fastFood = new Participant("FastFood", blockchain);
        Participant worker1 = new Participant("Worker1", blockchain);
        Participant worker2 = new Participant("Worker2", blockchain);
        Participant worker3 = new Participant("Worker3", blockchain);
        Participant director1 = new Participant("Director1", blockchain);
        Participant carPartsShop = new Participant("CarPartsShop", blockchain);
        Participant gamingShop = new Participant("GamingShop", blockchain);
        Participant beautyShop = new Participant("BeautyShop", blockchain);
        Participant gameDev = new Participant("GameDev", blockchain);
        Participant supplier = new Participant("Supplier", blockchain);

        for (int i = 1; i <= MINER_COUNT; i++) {
            Thread t = new Thread(new Miner(i, blockchain));
            t.setDaemon(true);
            t.start();
        }

        ScheduledExecutorService sender = Executors.newScheduledThreadPool(4);

        scheduleAfter(sender, 0, () -> miners[9].send(miners[1], 30));
        scheduleAfter(sender, 0, () -> miners[9].send(miners[2], 30));
        scheduleAfter(sender, 0, () -> miners[9].send(nick, 30));

        scheduleAfter(sender, 400, () -> miners[9].send(bob, 10));
        scheduleAfter(sender, 400, () -> miners[7].send(alice, 10));
        scheduleAfter(sender, 400, () -> nick.send(shoesShop, 1));
        scheduleAfter(sender, 400, () -> nick.send(fastFood, 2));
        scheduleAfter(sender, 400, () -> nick.send(carShop, 15));
        scheduleAfter(sender, 400, () -> miners[7].send(carShop, 90));

        scheduleAfter(sender, 700, () -> carShop.send(worker1, 10));
        scheduleAfter(sender, 700, () -> carShop.send(worker2, 10));
        scheduleAfter(sender, 700, () -> carShop.send(worker3, 10));
        scheduleAfter(sender, 700, () -> carShop.send(director1, 30));
        scheduleAfter(sender, 700, () -> carShop.send(carPartsShop, 45));
        scheduleAfter(sender, 700, () -> bob.send(gamingShop, 5));
        scheduleAfter(sender, 700, () -> alice.send(beautyShop, 5));

        scheduleAfter(sender, 1000, () -> gaming.send(gameDev, 20));
        scheduleAfter(sender, 1000, () -> beauty.send(supplier, 15));
        scheduleAfter(sender, 1000, () -> miners[1].send(miners[3], 50));
        scheduleAfter(sender, 1000, () -> miners[2].send(miners[4], 50));

        scheduleAfter(sender, 1300, () -> miners[3].send(nick, 20));
        scheduleAfter(sender, 1300, () -> miners[4].send(bob, 20));
        scheduleAfter(sender, 1300, () -> miners[5].send(alice, 20));

        scheduleAfter(sender, 1600, () -> nick.send(fastFood, 10));
        scheduleAfter(sender, 1600, () -> bob.send(shoesShop, 10));
        scheduleAfter(sender, 1600, () -> alice.send(beautyShop, 10));

        scheduleAfter(sender, 1900, () -> miners[6].send(miners[7], 40));
        scheduleAfter(sender, 1900, () -> miners[8].send(miners[9], 40));
        scheduleAfter(sender, 1900, () -> miners[10].send(miners[1], 40));

        finishLatch.await();
        sender.shutdown();
    }

    private static void scheduleAfter(ScheduledExecutorService exec, long delayMs, Runnable task) {
        exec.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }
}
