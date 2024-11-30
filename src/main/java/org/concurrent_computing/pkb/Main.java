package src.main.java.org.concurrent_computing.pkb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int minQuantity = 1;
        int maxQuantity = 50;
        int maxBuffer = 100;
        int threads = 8;
        Supplier<Integer> randomQuantity = () -> (int) (Math.random() * (maxQuantity - minQuantity + 1) + minQuantity);

        System.out.printf("Current parameters are:\nminQuantity=%d\nmaxQuantity=%d\nmaxBuffer=%d\nthreads=%d%n",
                minQuantity, maxQuantity, maxBuffer, threads);

        Buffer buffer2cond = new Buffer2Cond(maxBuffer);
        Buffer buffer4cond = new Buffer4Cond(maxBuffer);
        Buffer buffer3lock = new Buffer3Lock(maxBuffer);

        int testSeconds = 20;

        int[] results2Cond = benchmark(threads, testSeconds, randomQuantity, buffer2cond, "2 Cond");
        int[] results4Cond = benchmark(threads, testSeconds, randomQuantity, buffer4cond, "4 Cond");
        int[] results3Lock = benchmark(threads, testSeconds, randomQuantity, buffer3lock, "3 Lock");

        String filename = String.format("pkb_test/results%dto%db%dt%d.csv", minQuantity, maxQuantity, maxBuffer, threads * 2);

        saveToCSV(new int[][]{results2Cond, results4Cond, results3Lock}, filename);

        exit(0);
    }

    private static void saveToCSV(int[][] results, String filename) {
        File csvOutputFile = new File(filename);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            IntStream.range(0, results[0].length)
                    .mapToObj((idx) -> (idx + 1) + "," + results[0][idx] + "," + results[1][idx] + "," + results[2][idx])
                    .forEach(pw::println);
        } catch (FileNotFoundException ignored) {
        }
    }

    private static int[] benchmark(int threads, int testSeconds, Supplier<Integer> randomQuantity, Buffer buffer, String testName) throws InterruptedException {
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
            producersAndConsumers[2 * i].interrupt();
            producersAndConsumers[2 * i + 1].interrupt();
        }

        // aby dac chwile czasu na wykonanie instrukcji z producersAndConsumers[i].interrupt();
        // i upewnic sie ze watki z poprzedniego wywolania benchmark
        // nie wplywaja negatywnie na wydajnosc kolejnych benchmarkow
        TimeUnit.SECONDS.sleep(1);

        return results;
    }
}