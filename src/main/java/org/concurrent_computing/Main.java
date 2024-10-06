package org.concurrent_computing;

public class Main {
    public static void main(String[] args) throws InterruptedException {
//        int i = 0;
//        for (int j = 0; j < 10; j++) {
//            i += j;
//        }
//        System.out.println(i);
//          /usr/lib/jvm/java-21-jdk/bin/javap -v Main.class

        int[] iterationsList = {1, 2, 3, 5, 10, 15, 30, 50, 100, 200, 500, 1000, 2000, 3000, 4000, 5000, 10000};
        int[] threadsList = {1, 2, 3, 5, 10, 15, 30, 50, 100, 200, 500, 1000, 2000, 3000, 4000, 5000, 10000};
        for (int iterationsNumber: iterationsList) {
            for (int threadsNumber: threadsList) {
                Value value = new Value();
                Thread[] threads = new Thread[threadsNumber * 2];

                for (int i = 0; i < threadsNumber * 2; i += 2) {
                    ValueChanger increment = new ValueChanger(value, 1, iterationsNumber);
                    ValueChanger decrement = new ValueChanger(value, -1, iterationsNumber);

                    threads[i] = new Thread(increment);
                    threads[i + 1] = new Thread(decrement);
                }

                for (int i = 0; i < threadsNumber * 2; i++) {
                    threads[i].start();
                }

                for (int i = 0; i < threadsNumber * 2; i++) {
                    threads[i].join();
                }

                System.out.print(value.getValue() + " ");
            }
            System.out.println();
        }
    }
}