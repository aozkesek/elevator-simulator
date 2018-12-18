package org.elevator.simulator;

import java.util.concurrent.ThreadLocalRandom;

public class Passenger {

    private int floor;
    private int destFloor;

    public Passenger(Elevator elevator) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        floor = random.nextInt(elevator.getFloor());
        destFloor = random.nextInt(elevator.getFloor());
        while (floor == destFloor)
            destFloor = random.nextInt(elevator.getFloor());
         elevator.queuePassenger(this);
    }

    public int getDestFloor() {
        return destFloor;
    }

    public Direction getDirection() {
        if (destFloor > floor)
            return Direction.UP;
        else
            return Direction.DOWN;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public String toString() {
        return String.format("<Passenger: %1$d, %2$d, %3$s>", floor, destFloor, getDirection());
    }
}
