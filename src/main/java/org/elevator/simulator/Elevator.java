package org.elevator.simulator;

import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.in;
import static java.lang.System.out;

public class Elevator implements Runnable {

        private Object lock = new Object();

        private int floor;
        private int speedLimit;
        private int capacity;
        private int currentFloor;
        private volatile boolean outOfService;
        private Direction direction;
        private ConcurrentHashMap<Integer, Passenger> passengers;
        private ConcurrentHashMap<Integer, Passenger> waitingPassengers;


        public Elevator(int maxFloor, int maxCapacity) {
                passengers = new ConcurrentHashMap<>();
                waitingPassengers = new ConcurrentHashMap<>();
                floor = maxFloor;
                capacity = maxCapacity;
                direction = Direction.IDLE;
                // ground floor
                currentFloor = 0;
                outOfService = false;
                // 2500 msec takes between one floor to other
                speedLimit = 1300;
        }

        public void setOutOfService() {
                synchronized (lock) {
                        outOfService = true;
                }
        }

        public int getFloor() {
                return floor;
        }

        public void queuePassenger(Passenger passenger) {
                out.println(passenger);
                waitingPassengers.put(passenger.hashCode(), passenger);
        }

        private void limitSpeed() {

            try {
                Thread.sleep(speedLimit);
            } catch (InterruptedException e) {

            }

        }

        private void goUp() {

                // no one, skip
                if (waitingPassengers.isEmpty() && passengers.isEmpty()) {
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
                if (waitingPassengers.isEmpty() && passengers.isEmpty()) {
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

                if (passengers.isEmpty())
                        return;

                passengers.forEach((h, p) -> {
                        // we reached the floor, now get out
                        if (p.getDestFloor() == currentFloor) {
                                out.println(">>> getting out " + p);
                                passengers.remove(p.hashCode());
                        } });

        }

        private void pickupPassengers(Direction destDirection) {

                // no one! Ok, im out
                if (waitingPassengers.isEmpty())
                        return;

                // sorry, no available room for someone new
                if (passengers.size() == capacity)
                        return;

                // get in folks,
                waitingPassengers.forEach((h, p) -> {
                        // collect only ones who wants to go same direction
                        // till enough room
                        if (passengers.size() < capacity &&
                                p.getFloor() == currentFloor &&
                                p.getDirection() == destDirection)
                        {
                                out.println("<<< getting in " + p);
                                passengers.put(p.hashCode(), p);
                                waitingPassengers.remove(p.hashCode());
                        } });
        }

        private boolean someoneNeededService() {

                if (!passengers.isEmpty() || !waitingPassengers.isEmpty())
                        return true;

                return false;
        }

        private boolean isPersonExistFor(Direction destDirection, int passFloor) {
                return destDirection == Direction.DOWN && passFloor < currentFloor
                        || destDirection == Direction.UP && passFloor > currentFloor;
        }

        private boolean isSomeoneWaitingFor(Direction destDirection) {

                boolean outside = !waitingPassengers.isEmpty()
                        && waitingPassengers.search(2,
                        (h,p) -> isPersonExistFor(destDirection, p.getFloor()));

                if (outside)
                        return true;

                boolean inside = !passengers.isEmpty()
                        && passengers.search(2,
                        (h,p) -> isPersonExistFor(destDirection, p.getDestFloor()));

                return outside || inside;

        }

        private void goFor(Direction destDirection) {

                switch (destDirection) {
                        case UP:
                                if (isSomeoneWaitingFor(destDirection))
                                        goUp();
                                else
                                        goDown();
                                break;

                        case DOWN:
                                if (isSomeoneWaitingFor(destDirection))
                                        goDown();
                                else
                                        goUp();
                                break;
                }

        }

        private void doService(Direction destDirection) {

                while (!outOfService && someoneNeededService()) {
                        // let them get out first
                        dropPassengers();

                        if (currentFloor == floor) {
                                // we are at top floor, return down
                                destDirection = Direction.DOWN;
                                // our service is up here, get out, catch a rocket
                                passengers.clear();
                        } else if (currentFloor == 0) {
                                // we are at ground/basement floor
                                destDirection = Direction.UP;
                                // what do you wait for, get out folks
                                passengers.clear();
                        }

                        // now folks can get in
                        pickupPassengers(destDirection);
                        goFor(destDirection);

                        report();
                }

        }

        public void report() {
                out.println("Elevator is going " + direction
                        + " | currently at " + currentFloor
                        + " | total persons waiting " + waitingPassengers.size()
                        + " | total persons getting " + passengers.size()
                );
        }

        @Override
        public void run() {
                // check constantly is someone waiting for
                while (!outOfService) {

                        Thread.yield();

                        if (waitingPassengers.isEmpty())
                                continue;

                        Passenger passenger = waitingPassengers
                                .elements()
                                .nextElement();

                        Direction initialDirection =
                                getInitialDirectionFor(passenger);
                        Direction destDirection = initialDirection;

                        // ok someone is waiting,
                        switch (initialDirection) {
                                case UP:
                                        goUp();
                                        break;

                                case DOWN:
                                        goDown();
                                        break;

                                case IDLE:
                                        destDirection = passenger.getDirection();
                                        break;
                        }


                        doService(destDirection);

                        // ooo dear, no one!, get some rest


                }
        }
}