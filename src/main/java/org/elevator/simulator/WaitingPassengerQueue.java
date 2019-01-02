package org.elevator.simulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static java.lang.System.out;

public class WaitingPassengerQueue {

        private static int cpuCount = 2;

        private Object lock;
        private ConcurrentHashMap<Integer, Passenger> waitingPassengers;

        public WaitingPassengerQueue() {
                lock = new Object();
                waitingPassengers = new ConcurrentHashMap<>();
        }

        public boolean add(Passenger passenger) {
                out.println("! " + passenger + " is queued. Total " + size());
                return passenger.equals(
                        waitingPassengers.put(passenger.hashCode(), passenger));
        }

        public boolean isEmpty() {
                return waitingPassengers.isEmpty();
        }

        public int size() {
                return waitingPassengers.size();
        }

        public Passenger getFirst(Elevator elevator) {
                synchronized (lock) {
                        Passenger p = waitingPassengers
                                .search(cpuCount, (k,v) -> {
                                        if (v.getElevatorId() == -1)
                                                return v;
                                        return null;
                                });
                        if (p == null)
                                return null;
                        out.println("! " + p + " will be in " + elevator);
                        p.setElevatorId(elevator.getElevatorId());
                        return p;
                }
        }

        private boolean isWaitingFor(Elevator elevator, Passenger passenger) {
                return (passenger.getFloor() > elevator.getCurrentFloor() &&
                                passenger.getDirection() == Direction.UP) ||
                        (passenger.getFloor() < elevator.getCurrentFloor() &&
                                passenger.getDirection() == Direction.DOWN);
        }

        public boolean isPassengerWaitingFor(Direction destDirection, int floor) {
                Map.Entry<Integer, Passenger> entry = waitingPassengers
                        .searchEntries(4, e -> {
                        if ((e.getValue().getFloor() < floor && destDirection == Direction.DOWN)
                        || (e.getValue().getFloor() > floor && destDirection == Direction.UP))
                                return e;
                        else
                                return null;
                });

                return entry != null;
        }

        public void bind(Elevator elevator) {
                synchronized (lock) {
                        waitingPassengers.forEach((k,v) -> {
                                if (v.getElevatorId() == -1 &&
                                    v.getDirection() == elevator.getDirection() &&
                                    isWaitingFor(elevator, v))
                                {
                                        out.println("! " + v + " will be in " + elevator);
                                        v.setElevatorId(elevator.getElevatorId());
                                }
                        });
                }

        }

        public void pickUp(Elevator elevator, Direction destDirection) {
                synchronized (lock) {
                        waitingPassengers.forEach((k,v) -> {
                                if (v.getFloor() == elevator.getCurrentFloor()
                                        && v.getDirection() == destDirection
                                        && elevator.isAvailableRoom())
                                {
                                        waitingPassengers.remove(k, v);
                                        elevator.addPassenger(v);
                                }
                        });
                }
        }
}
