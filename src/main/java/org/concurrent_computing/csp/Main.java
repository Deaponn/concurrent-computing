package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        int buffersCount = 3;
        int producersCount = 4;
        int consumersCount = 5;

        Buffer[] buffers = new Buffer[buffersCount];
        Producer[] producers = new Producer[producersCount];
        Consumer[] consumers = new Consumer[consumersCount];

        ResultCollector resultCollector = new ResultCollector(producers, consumers, buffers, 1);

        Middleman middleman = new Middleman(buffersCount, producersCount, consumersCount, resultCollector);

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

        CSProcess[] procList = Arrays.stream(new CSProcess[][]{
                        {middleman}, buffers, producers, consumers, {resultCollector}
                })
                .flatMap(Arrays::stream)
                .toArray(CSProcess[]::new);

        Parallel par = new Parallel(procList);
        par.run();
    }
}
