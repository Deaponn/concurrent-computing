package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int minQuantity = 1;
        int maxQuantity = 50;
        int maxBuffer = 2 * maxQuantity;
        int threads = 8;
        Supplier<Integer> randomQuantity = () -> (int) (Math.random() * maxQuantity + minQuantity);

        System.out.printf("Current parameters are:\nminQuantity=%d\nmaxQuantity=%d\nmaxBuffer=%d\nthreads=%d%n",
                minQuantity, maxQuantity, maxBuffer, threads);

        Buffer buffer2cond = new Buffer2Cond(maxBuffer);
        Buffer buffer4cond = new Buffer4Cond(maxBuffer);
        Buffer buffer3lock = new Buffer3Lock(maxBuffer);

        benchmark(threads, randomQuantity, buffer2cond, "2 Cond");
        benchmark(threads, randomQuantity, buffer4cond, "4 Cond");
        benchmark(threads, randomQuantity, buffer3lock, "3 Lock");

        exit(0);
    }

    private static void benchmark(int threads, Supplier<Integer> randomQuantity, Buffer buffer, String testName) throws InterruptedException {
        int testSeconds = 20;

        System.out.println("\nBenchmarking " + testName);

        Thread[] producersAndConsumers = new Thread[threads * 2];
        int[] results = new int[testSeconds];

        for (int i = 0; i < threads; i++) {
            Producer producer = new Producer(buffer, randomQuantity);
            Consumer consumer = new Consumer(buffer, randomQuantity);

            producersAndConsumers[2 * i] = new Thread(producer);
            producersAndConsumers[2 * i + 1] = new Thread(consumer);

            producersAndConsumers[2 * i].start();
            producersAndConsumers[2 * i + 1].start();
        }

        for (int i = 0; i < testSeconds; i++) {
            TimeUnit.SECONDS.sleep(1);
            results[i] = buffer.getOperationCount();
        }

        for (int i = 0; i < threads; i++) {
            producersAndConsumers[i].interrupt();
        }

        System.out.println(
                IntStream.range(0, testSeconds)
                        .mapToObj((idx) -> (idx + 1) + ";" + results[idx])
                        .collect(Collectors.joining("\n"))
        );

        // aby dac chwile czasu na wykonanie instrukcji z producersAndConsumers[i].interrupt();
        // i upewnic sie ze watki z poprzedniego wywolania benchmark
        // nie wplywaja negatywnie na wydajnosc kolejnych benchmarkow
        TimeUnit.SECONDS.sleep(1);
    }
}