package org.elevator.simulator;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;

public class Elevator implements Runnable {

        private static int floors;
        private static int persons;
        private static AtomicBoolean outOfService;

        private int speed;
        private int floor;

        private AtomicReference<Direction> direction;
        private ConcurrentMap<Integer, Passenger> insiders;
        private ConcurrentMap<Integer, Passenger> outsiders;

        public Elevator(int floors, int persons) {

                Elevator.floors = floors;
                Elevator.persons = persons;

                insiders = new ConcurrentHashMap<>();
                outsiders = new ConcurrentHashMap<>();

                direction = new AtomicReference<>(Direction.IDLE);
                // ground floor
                floor = 0;
                outOfService = new AtomicBoolean(false);
                // 2500 msec takes between one floor to other
                speed = 1300;

        }

        public static boolean isOutOfService() {
                return outOfService.get();
        }

        public static void setOutOfService() {
                outOfService.set(true);
        }

        public static int getFloors() {
                return floors;
        }

        public Direction getDirection() {
                return direction.get();
        }

        public int getFloor() {
                return floor;
        }

        public boolean isRoomAvailable() {
                return insiders.size() + outsiders.size() < persons;
        }

        public int count() {
                return insiders.size() + outsiders.size();
        }

        private boolean isInService() {
                return !insiders.isEmpty();
        }

        private boolean isInServiceFor() {
                return insiders.values()
                    .stream()
                    .anyMatch(p -> p.getDirection() == direction.get());
        }

        private boolean isWaiting() {
                return !outsiders.isEmpty();
        }

        private boolean isWaitingFor() {
                return outsiders.values()
                    .stream()
                    .anyMatch(p -> direction.get() == Direction.DOWN ?
                        floor > p.getFloor() : floor < p.getFloor());
        }

        private void directFor(Passenger p) {
                if (p.getFloor() > floor)
                        direction.set(Direction.UP);
                else if (p.getFloor() < floor)
                        direction.set(Direction.DOWN);
                else
                        direction.set(p.getDirection());

        }

        private void getOff() {
                insiders.values()
                    .stream()
                    .filter(p -> p.getDestination() == floor)
                    .forEach(p -> {
                            insiders.remove(p.hashCode(), p);
                            if (!insiders.containsValue(p))
                                    out.println("-" + p + " has got out " + this);
                    });
        }

        private void getOn() {
                outsiders.values()
                    .stream()
                    .filter(p -> p.getFloor() == floor && p.getDirection() == direction.get())
                    .forEach(p -> {
                            outsiders.remove(p.hashCode(), p);
                            insiders.putIfAbsent(p.hashCode(), p);
                            if (insiders.containsValue(p))
                                    out.println("+" + p + " has got on " + this);
                    });

        }

        public void waitFor(Passenger p) {
                outsiders.putIfAbsent(p.hashCode(), p);
                if (outsiders.containsValue(p))
                        out.println("+" + p + " is waiting for " + this);
        }

        private void goFor() {

                try { Thread.sleep(speed); } catch (InterruptedException e) { }

                if (direction.get() == Direction.DOWN) {
                        if (floor > 0) {
                                floor--;
                        }
                } else if (direction.get() == Direction.UP) {
                        if (floor < floors) {
                                floor++;
                        }
                }

        }

        private void swapDirection() {
                if (direction.get() == Direction.DOWN)
                        direction.set(Direction.UP);
                else if (direction.get() == Direction.UP)
                        direction.set(Direction.DOWN);
        }

        public void report() {
                out.println("* " + this);

        }

        private void doService() {

                while (!isOutOfService() && (isInService() || isWaiting())) {

                        if (floor == floors)
                                // we are at top floor, return down
                                direction.set(Direction.DOWN);
                        else if (floor == 0)
                                // we are at ground/basement floor
                                direction.set(Direction.UP);

                        report();

                        getOff();

                        getOn();

                        if (!isInServiceFor() && !isWaitingFor()) {
                                swapDirection();
                                getOn();
                        }

                        goFor();

                }

                report();

        }

        private Passenger nearest() {
                Optional<Passenger> min = outsiders.values()
                    .stream()
                    .min(Comparator.comparingInt(a -> Math.abs(a.getFloor() - floor)));
                return min.orElse(null);
        }

        @Override
        public void run() {

                WaitingQueue.add(this);

                while (!isOutOfService()) {

                        Thread.yield();

                        if (!isWaiting())
                                continue;

                        Passenger passenger = nearest();

                        if (passenger == null)
                                continue;

                        directFor(passenger);

                        doService();

                }

                WaitingQueue.remove(this);
        }

        @Override
        public String toString() {
                return String.format("[E%1$d: %2$d|%3$s|%4$d/%5$d]", hashCode(),
                    floor, direction, insiders.size(), outsiders.size());
        }
}