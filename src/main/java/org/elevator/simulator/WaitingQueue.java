package org.elevator.simulator;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.out;

final class WaitingQueue {

        private static ConcurrentMap<Integer, Elevator> elevators =
            new ConcurrentHashMap<>();

        private WaitingQueue() {}

        static boolean add(Elevator e) {
                if (null == e)
                        throw new IllegalArgumentException("elevator is NULL.");
                elevators.putIfAbsent(e.hashCode(), e);
                if (elevators.containsValue(e)) {
                        out.println("+" + e + " is IN SERVICE.");
                        return true;
                } else
                        return false;
        }

        static boolean remove(Elevator e) {
                if (null == e)
                        throw new IllegalArgumentException("elevator is NULL.");
                elevators.remove(e.hashCode(), e);
                if (elevators.containsValue(e))
                        return false;

                out.println("+" + e + " is OUT OF SERVICE.");
                return true;
        }

        private static Elevator nearest(Passenger p) {
                Optional<Elevator> min = elevators.values()
                    .stream()
                    .filter(e -> e.getDirection() == p.getDirection())
                    // at least two floor difference between them
                    .min(Comparator.comparingInt(e -> Math.abs(e.getFloor() - p.getFloor() - 2)));
                if (!min.isEmpty())
                        return min.get();

                // no elevator to same direction, ok check idle one
                min = elevators.values()
                    .stream()
                    .filter(e -> e.getDirection() == Direction.IDLE)
                    .min(Comparator.comparingInt(e -> Math.abs(e.getFloor() - p.getFloor())));
                if (!min.isEmpty())
                        return min.get();
                // uuu, even no IDLE, ok, pick one has least passengers
                min = elevators.values()
                    .stream()
                    .min(Comparator.comparingInt(e -> e.count()));
                return min.get();
        }

        static void add(Passenger p) {
                out.println("+" + p + " is in queue.");
                if (elevators.isEmpty())
                        out.println("Elevators are OUT OF SERVICE.");
                else
                        nearest(p).waitFor(p);
        }


}
