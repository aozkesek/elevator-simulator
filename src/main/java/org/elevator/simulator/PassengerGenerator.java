package org.elevator.simulator;

import static java.lang.System.out;

final class PassengerGenerator implements Runnable {

    private int interval;

    public PassengerGenerator(int interval) {
        this.interval = interval;
    }

    @Override
    public void run() {

        while (!Elevator.isOutOfService()) {
            try {
                Thread.sleep(RandomGenerator.nextInt(interval) * 1000);
                if (!Elevator.isOutOfService())
                    WaitingQueue.add(new Passenger());
            } catch (InterruptedException e) {
                out.println(e.getMessage());
            }

        }

    }

}
