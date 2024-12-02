package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.io.*;
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
                // es, bs, b, p, k - element size, buffer size, buffers, producers, konsumers,
                String.format("csp_test/results%des%dbs%db%dp%dk.csv",
                        this.itemSize,
                        this.bufferCapacity,
                        this.buffers.length,
                        this.producers.length,
                        this.consumers.length
                )
        );

        exit(0);
    }

    private static void saveToCSV(int middleman, int[] producers, int[] consumers, int[] buffers, String filename) {
        File csvOutputFile = new File(filename);
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
            TimeUnit.SECONDS.sleep(this.timeSleep - 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Producer producer : this.producers) {
            producer.deactivate();
        }

        for (Consumer consumer : this.consumers) {
            consumer.deactivate();
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.collectResults(0);
    }
}
