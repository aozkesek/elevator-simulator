package org.elevator.simulator;

import java.util.concurrent.ThreadLocalRandom;

final class RandomGenerator {
    static ThreadLocalRandom random = ThreadLocalRandom.current();

    static int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
