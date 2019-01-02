package org.elevator.simulator;

import java.util.concurrent.ThreadLocalRandom;

public class PassengerGenerator implements Runnable {

    private WaitingPassengerQueue waitingPassengerQueue;
    private int interval;

    public PassengerGenerator(WaitingPassengerQueue waitingPassengerQueue,
                              int interval) {
        this.waitingPassengerQueue = waitingPassengerQueue;
        this.interval = interval;
    }

    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (!Elevator.isOutOfService()) {

            try {
                Thread.sleep(random.nextInt(interval) * 1000);
                if (!Elevator.isOutOfService())
                    waitingPassengerQueue.add(new Passenger());
            } catch (InterruptedException e) {

            }

        }

    }

}
