package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public final class Main {
    public static void main(String[] args) {
        int buffersCount = 5;
        int producersCount = 5;
        int consumersCount = 5;

        Buffer[] buffers = new Buffer[producersCount];
        Producer[] producers = new Producer[producersCount];
        Consumer[] consumers = new Consumer[consumersCount];

        Middleman middleman = new Middleman(buffersCount, producersCount, consumersCount);

        for (int i = 0; i < buffersCount; i++) {
            Buffer buffer = new Buffer(i, producersCount, consumersCount);
            buffers[i] = buffer;
        }

        for (int i = 0; i < producersCount; i++) {
            Producer producer = new Producer(i, buffersCount, middleman);
            for (Buffer buffer : buffers) {
                producer.registerBuffer(buffer);
            }
            producers[i] = producer;
        }

        for (int i = 0; i < consumersCount; i++) {
            Consumer consumer = new Consumer(i, buffersCount, middleman);
            for (Buffer buffer : buffers) {
                consumer.registerBuffer(buffer);
            }
            consumers[i] = consumer;
        }

        CSProcess[] procList = Arrays.stream(new CSProcess[][]{{middleman}, buffers, producers, consumers})
                .flatMap(Arrays::stream)
                .toArray(CSProcess[]::new);

        System.out.println("startup");

        Parallel par = new Parallel(procList);
        par.run();
    }
}
