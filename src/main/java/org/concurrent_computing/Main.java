package src.main.java.org.concurrent_computing;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int threadsNumber = 1;
        int iterationsNumber = 4;
        ValueMonitor value = new ValueMonitor();
        Thread[] threads = new Thread[threadsNumber * 2];

        for (int i = 0; i < threadsNumber * 2; i += 2) {
            ValueChanger increment = new ValueChanger(value, true, iterationsNumber);
            ValueChanger decrement = new ValueChanger(value, false, iterationsNumber);

            threads[i + 1] = new Thread(increment);
            threads[i] = new Thread(decrement);
        }

        for (int i = 0; i < threadsNumber * 2; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threadsNumber * 2; i++) {
            threads[i].join();
        }

        System.out.print(value.getValue());
    }
}