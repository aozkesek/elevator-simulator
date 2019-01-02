package org.elevator.simulator;

import java.util.concurrent.ThreadLocalRandom;

public class Passenger {

    private Object lock;
    private int floor;
    private int destFloor;
    private volatile long elevatorId;

    public Passenger() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        lock = new Object();

        elevatorId = -1;
        floor = random.nextInt(Elevator.getFloor());
        destFloor = random.nextInt(Elevator.getFloor());

        while (floor == destFloor)
            destFloor = random.nextInt(Elevator.getFloor());
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

    public void setElevatorId(long elevatorId) {
        synchronized (lock) {
            this.elevatorId = elevatorId;
        }
    }

    public long getElevatorId() {
        return elevatorId;
    }

    @Override
    public String toString() {
        return String.format("<Passenger: %1$d, %2$d, %3$s>",
                floor, destFloor, getDirection());
    }
}
