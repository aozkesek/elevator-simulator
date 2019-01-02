package org.elevator.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.out;

public class Elevator implements Runnable {

        private static Object lock = new Object();

        private static int floor;
        private static int capacity;

        private int speedLimit;
        private int currentFloor;
        private long elevatorId;
        private static volatile boolean outOfService;
        private Direction direction;
        private List<Passenger> passengers;
        private WaitingPassengerQueue waitingPassengerQueue;

        public Elevator(int maxFloor, int capacity,
                        WaitingPassengerQueue waitingPassengerQueue) {

                floor = maxFloor;
                this.capacity = capacity;
                this.waitingPassengerQueue = waitingPassengerQueue;

                passengers = new ArrayList<>();
                direction = Direction.IDLE;
                // ground floor
                currentFloor = 0;
                outOfService = false;
                // 2500 msec takes between one floor to other
                speedLimit = 1300;
        }

        public static boolean isOutOfService() {
                return outOfService;
        }

        public static void setOutOfService() {
                synchronized (lock) {
                        outOfService = true;
                }
        }

        public static int getFloor() {
                return floor;
        }

        public long getElevatorId() {
                return elevatorId;
        }

        public Direction getDirection() {
                return direction;
        }

        public int getCurrentFloor() {
                return currentFloor;
        }

        public boolean isAvailableRoom() {
                return passengers.size() < capacity;
        }

        private void limitSpeed() {

            try {
                Thread.sleep(speedLimit);
            } catch (InterruptedException e) {

            }

        }

        public void addPassenger(Passenger passenger) {
                out.println(this + " has total " + passengers.size());
                if (passengers.size() < capacity) {
                        out.println(passenger + " is getting into " + this);
                        passengers.add(passenger);
                }
        }

        private void goUp() {

                // no one, skip
                if (waitingPassengerQueue.isEmpty() && passengers.isEmpty()) {
                        direction = Direction.IDLE;
                        return;
                }

                if (currentFloor < floor) {
                    direction = Direction.UP;
                    limitSpeed();
                    currentFloor++;
                } else
                    direction = Direction.IDLE;
        }

        private void goDown() {

                // no one, skip
                if (waitingPassengerQueue.isEmpty() && passengers.isEmpty()) {
                        direction = Direction.IDLE;
                        return;
                }

                if (currentFloor > 0) {
                    direction = Direction.DOWN;
                    limitSpeed();
                    currentFloor--;
                } else
                    direction = Direction.IDLE;
        }

        private Direction getInitialDirectionFor(Passenger passenger) {

                if (passenger.getFloor() > currentFloor)
                        return Direction.UP;

                else if (passenger.getFloor() < currentFloor)
                        return Direction.DOWN;

                // passenger is at the same floor
                return Direction.IDLE;
        }

        private void dropPassengers() {
                List<Passenger> getOuts = new ArrayList<>();
                passengers
                        .forEach((p) -> {
                                // we reached the floor, now get out
                                if (p.getDestFloor() != currentFloor)
                                        return;

                                out.println(">>> " + p + " is getting out from "
                                        + this);
                                getOuts.add(p);
                        });

                if (getOuts.size() > 0)
                        passengers.removeAll(getOuts);
        }

        private void pickupPassengers(Direction destDirection) {

                // sorry, no available room for someone new
                if (passengers.size() == capacity) {
                        out.println(this + ": sorry, no room for new one.");
                        return;
                }

                // get in folks,
                waitingPassengerQueue.pickUp(this, destDirection);
        }

        private boolean isSomeoneNeedService() {

                if (!passengers.isEmpty() || !waitingPassengerQueue.isEmpty())
                        return true;

                return false;
        }

        private void goFor(Direction destDirection) {

                switch (destDirection) {
                        case UP:
                                goUp();
                                break;

                        case DOWN:
                                goDown();
                                break;
                }

        }

        private boolean isSomeoneExistFor(Direction destDirection) {

                return waitingPassengerQueue
                        .isPassengerWaitingFor(destDirection, currentFloor)
                        || !passengers.isEmpty();
        }

        private Direction swapDirection(Direction destDirection) {
                if (destDirection == Direction.DOWN)
                        return Direction.UP;
                else if (destDirection == Direction.UP)
                        return Direction.DOWN;
                return Direction.IDLE;
        }

        public void report() {
                out.print(this + " is going to " + direction
                        + ", currently at " + currentFloor);
                if (waitingPassengerQueue.size() > 0)
                        out.print(", total " + waitingPassengerQueue.size()
                                + " is/are waiting");
                if (passengers.size() > 0)
                        out.print(", total " + passengers.size()
                                + " is/are getting service now.");
                out.println();
        }

        private void doService(Direction destDirection) {

                while (!outOfService && isSomeoneNeedService()) {

                        // let them get out first
                        dropPassengers();

                        if (currentFloor == floor)
                                // we are at top floor, return down
                                destDirection = Direction.DOWN;
                        else if (currentFloor == 0)
                                // we are at ground/basement floor
                                destDirection = Direction.UP;

                        direction = destDirection;

                        report();

                        // now folks can get in
                        pickupPassengers(destDirection);

                        if (!isSomeoneExistFor(destDirection)) {
                                destDirection = swapDirection(destDirection);
                                // direction is changed, pick up again
                                pickupPassengers(destDirection);
                        }

                        goFor(destDirection);

                }

                // report IDLE
                report();

        }

        @Override
        public void run() {

                elevatorId = Thread.currentThread().getId();

                // check constantly is someone waiting for
                while (!outOfService) {

                        Thread.yield();

                        if (waitingPassengerQueue.isEmpty())
                                continue;

                        Passenger passenger = waitingPassengerQueue
                                .getFirst(this);

                        if (passenger == null
                                || passenger.getElevatorId() != elevatorId)
                                continue;

                        Direction initialDirection =
                                getInitialDirectionFor(passenger);
                        Direction destDirection = initialDirection;

                        if (destDirection == Direction.IDLE)
                                destDirection = passenger.getDirection();

                        doService(destDirection);

                }
        }

        @Override
        public String toString() {
                return "Elevator-" + elevatorId;
        }
}