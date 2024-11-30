package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;
import java.util.function.Consumer;

public class Middleman implements CSProcess {
    private final int buffersCount;
    private final int bufferCapacity;
    private final int producersCount;
    private int buffersTaken = 0;
    private int nextProducerBuffer = 0;
    private int nextConsumerBuffer = 0;
    private final AltingChannelInputInt[] producersRequests;
    private final ChannelOutputInt[] producersResponses;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutputInt[] consumersResponses;
    private int opCount = 0;
    private boolean isActive = true;
    private int deactivatedCount = 0;
    private final ResultCollector resultCollector;

    public Middleman(int buffersCount, int bufferCapacity, int producersCount, int consumersCount, ResultCollector resultCollector) {
        this.buffersCount = buffersCount;
        this.bufferCapacity = bufferCapacity;
        this.producersCount = producersCount;

        this.producersRequests = new AltingChannelInputInt[producersCount];
        this.producersResponses = new ChannelOutputInt[producersCount];

        this.consumersRequests = new AltingChannelInputInt[consumersCount];
        this.consumersResponses = new ChannelOutputInt[consumersCount];

        this.resultCollector = resultCollector;
    }

    public AltingChannelInputInt registerProducer(int producerIndex, AltingChannelInputInt request) {
        One2OneChannelInt responseChannel = Channel.one2oneInt();

        this.producersRequests[producerIndex] = request;
        this.producersResponses[producerIndex] = responseChannel.out();

        return responseChannel.in();
    }

    public AltingChannelInputInt registerConsumer(int consumerIndex, AltingChannelInputInt request) {
        One2OneChannelInt responseChannel = Channel.one2oneInt();

        this.consumersRequests[consumerIndex] = request;
        this.consumersResponses[consumerIndex] = responseChannel.out();

        return responseChannel.in();
    }

    void deactivate() {
        this.deactivatedCount++;
        if (this.deactivatedCount == this.producersRequests.length + this.consumersRequests.length) {
            this.isActive = false;
            this.resultCollector.collectResults(this.opCount);
        }
    }

    @Override
    public void run() {
        Guard[] guards = Arrays.stream(new Guard[][]{this.producersRequests, this.consumersRequests})
                .flatMap(Arrays::stream)
                .toArray(Guard[]::new);

        Alternative alternativeAll = new Alternative(guards);
        // these other alternatives are used when there is no possibility for the other
        // class of processes to receive attention
        // for example when all buffers are full no Producer should receive attention
        Alternative alternativeProducers = new Alternative(this.producersRequests);
        Alternative alternativeConsumers = new Alternative(this.consumersRequests);

        while (this.isActive) {
            boolean producersOnly = buffersTaken == 0;
            boolean consumersOnly = buffersTaken == this.buffersCount * this.bufferCapacity;

            int index = producersOnly ? alternativeProducers.select() :
                    consumersOnly ? alternativeConsumers.select() :
                            alternativeAll.select();

            if (consumersOnly) index += this.producersCount;

            AlternativeOutput action = index < producersCount ? AlternativeOutput.PRODUCER : AlternativeOutput.CONSUMER;

            switch (action) {
                case PRODUCER -> {
                    int producerIndex = this.producersRequests[index].read();
                    this.producersResponses[index].write(this.nextProducerBuffer);
                    this.nextProducerBuffer = (this.nextProducerBuffer + 1) % this.buffersCount;
                    this.buffersTaken++;
                }
                case CONSUMER -> {
                    int consumerIndex = this.consumersRequests[index - this.producersCount].read();
                    this.consumersResponses[index - this.producersCount].write(this.nextConsumerBuffer);
                    this.nextConsumerBuffer = (this.nextConsumerBuffer + 1) % this.buffersCount;
                    this.buffersTaken--;
                }
            }
            this.opCount++;
        }
    }
}

enum AlternativeOutput {
    PRODUCER,
    CONSUMER
}
