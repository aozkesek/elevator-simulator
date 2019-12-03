package org.elevator.simulator;

class Passenger {

    private int floor;
    private int destination;

    Passenger() {
        int floors = Elevator.getFloors();
        floor = RandomGenerator.nextInt(floors);
        destination = RandomGenerator.nextInt(floors);

        while (floor == destination)
            destination = RandomGenerator.nextInt(floors);
    }

    int getDestination() {
        return destination;
    }

    int getFloor() {
        return floor;
    }

    Direction getDirection() {
        return (destination > floor) ? Direction.UP : Direction.DOWN;
    }

    @Override
    public String toString() {
        return String.format("[P%1$d: %2$d|%3$d|%4$s]",
            hashCode(), floor, destination, getDirection());
    }
}
