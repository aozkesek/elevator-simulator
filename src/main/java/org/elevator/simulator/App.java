/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.elevator.simulator;

import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class App {

    public static void main(String[] args) {

        int elevatorCount = 2;
        int maxFloor = 10;
        int capacity = 6;
        int pgInterval = 10;
        int simInterval = 1;

        switch (args.length) {
            case 5:
                simInterval = Integer.parseInt(args[4]);

            case 4:
                pgInterval = Integer.parseInt(args[3]);

            case 3:
                capacity = Integer.parseInt(args[2]);

            case 2:
                maxFloor = Integer.parseInt(args[1]);

            case 1:
                elevatorCount = Integer.parseInt(args[0]);

        }

        out.println("elevatorCount = " + elevatorCount +
                ", maxFloor = " + maxFloor +
                ", capacity = " + capacity +
                ", pgInterval = " + pgInterval +
                ", simInterval = " + simInterval);

        ExecutorService taskExecutor =
                Executors.newFixedThreadPool(elevatorCount + 1);

        for (int i = 0; i < elevatorCount; i++)
            taskExecutor.execute(new Elevator(maxFloor, capacity));

        PassengerGenerator passengerGenerator = new PassengerGenerator(pgInterval);
        taskExecutor.execute(passengerGenerator);

        try { Thread.sleep(simInterval * 1000 * 60); }
        catch(InterruptedException e) { }

        Elevator.setOutOfService();

        taskExecutor.shutdown();
        try { taskExecutor.awaitTermination(simInterval, TimeUnit.MINUTES); }
        catch (InterruptedException e) { }
        System.exit(0);

    }
}
