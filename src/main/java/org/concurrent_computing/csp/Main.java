package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        int buffersCount = 150;
        int producersCount = 250;
        int consumersCount = 250;

        int bufferCapacity = 10;
        int itemSize = 1024 * 1024; // in bytes
        int testTime = 10;

        Buffer[] buffers = new Buffer[buffersCount];
        Producer[] producers = new Producer[producersCount];
        Consumer[] consumers = new Consumer[consumersCount];

        ResultCollector resultCollector = new ResultCollector(producers, consumers, buffers, bufferCapacity, itemSize, testTime);

        Middleman middleman = new Middleman(buffersCount, bufferCapacity, producersCount, consumersCount, resultCollector);

        for (int i = 0; i < buffersCount; i++) {
            Buffer buffer = new Buffer(i, bufferCapacity, itemSize, producersCount, consumersCount);
            buffers[i] = buffer;
        }

        for (int i = 0; i < producersCount; i++) {
            Producer producer = new Producer(i, itemSize, buffersCount, middleman);
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

        CSProcess[] procList = Arrays.stream(new CSProcess[][]{
                        {middleman}, buffers, producers, consumers, {resultCollector}
                })
                .flatMap(Arrays::stream)
                .toArray(CSProcess[]::new);

        Parallel par = new Parallel(procList);
        par.run();
    }
}
