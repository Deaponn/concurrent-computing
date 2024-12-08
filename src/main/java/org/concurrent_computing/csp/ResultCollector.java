package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.exit;

public class ResultCollector implements CSProcess {
    private final Producer[] producers;
    private final Consumer[] consumers;
    private final Buffer[] buffers;
    private final int bufferCapacity;
    private final int itemSize;
    private final int timeSleep;

    public ResultCollector(Producer[] producers, Consumer[] consumers, Buffer[] buffers,
                           int bufferCapacity, int itemSize, int timeSleep) {
        this.producers = producers;
        this.consumers = consumers;
        this.buffers = buffers;
        this.bufferCapacity = bufferCapacity;
        this.itemSize = itemSize;
        this.timeSleep = timeSleep;
    }

    public void collectResults(int middlemanOpCount) {
        for (Buffer buffer : this.buffers) {
            buffer.deactivate();
        }

        ResultCollector.saveToCSV(middlemanOpCount,
                Arrays.stream(this.producers)
                        .mapToInt(Producer::getOpCount)
                        .toArray(),
                Arrays.stream(this.consumers)
                        .mapToInt(Consumer::getOpCount)
                        .toArray(),
                Arrays.stream(this.buffers)
                        .mapToInt(Buffer::getOpCount)
                        .toArray(),
                // p, c, b, bs, is - producers, consumers, buffers, buffer size, item size
                String.format("csp_test/p%dc%db%dbs%dis%d",
                        this.producers.length,
                        this.consumers.length,
                        this.buffers.length,
                        this.bufferCapacity,
                        this.itemSize
                )
        );

        exit(0);
    }

    private static void saveToCSV(int middleman, int[] producers, int[] consumers, int[] buffers, String testName) {
        try {
            Files.createDirectories(Paths.get(testName));
        } catch (IOException ignored) {
        }

        File csvOutputFile = new File(String.format("%s/results.csv", testName));
        if (!csvOutputFile.isFile()) {
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                pw.print("middleman,");
                pw.print(IntStream.range(0, producers.length)
                        .mapToObj(idx -> String.format("producer %d", idx))
                        .collect(Collectors.joining(",")));
                pw.print(",");
                pw.print(IntStream.range(0, consumers.length)
                        .mapToObj(idx -> String.format("consumer %d", idx))
                        .collect(Collectors.joining(",")));
                pw.print(",");
                pw.print(IntStream.range(0, buffers.length)
                        .mapToObj(idx -> String.format("buffer %d", idx))
                        .collect(Collectors.joining(",")));
                pw.println();
            } catch (FileNotFoundException ignored) {
            }
        }

        String dataString = String.format("%d,%s,%s,%s\n", middleman,
                IntStream.of(producers)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(",")),
                IntStream.of(consumers)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(",")),
                IntStream.of(buffers)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(",")));

        try {
            FileWriter fr = new FileWriter(csvOutputFile, true);
            fr.write(dataString);
            fr.close();
        } catch (IOException ignored) {
        }
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(this.timeSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Producer producer : this.producers) {
            producer.deactivate();
        }

        for (Consumer consumer : this.consumers) {
            consumer.deactivate();
        }
    }
}
