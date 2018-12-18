package org.elevator.simulator;

import java.util.concurrent.ThreadLocalRandom;

public class PassengerGenerator implements Runnable {

    private Elevator elevator;


    public PassengerGenerator(Elevator elevator) {
        this.elevator = elevator;
    }

    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (true) {

            try {
                Thread.sleep(random.nextInt(15) * 1000);
                new Passenger(elevator);
            } catch (InterruptedException e) {

            }

        }


    }

}
