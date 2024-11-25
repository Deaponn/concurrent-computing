package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public class Middleman implements CSProcess {
    private final int buffersCount;
    private final int producersCount;
    private int buffersTaken = 0;
    private int nextProducerBuffer = 0;
    private int nextConsumerBuffer = 0;
    private final AltingChannelInputInt[] producersRequests;
    private final ChannelOutputInt[] producersResponses;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutputInt[] consumersResponses;

    public Middleman(int buffersCount, int producersCount, int consumersCount) {
        this.buffersCount = buffersCount;
        this.producersCount = producersCount;

        this.producersRequests = new AltingChannelInputInt[producersCount];
        this.producersResponses = new ChannelOutputInt[producersCount];

        this.consumersRequests = new AltingChannelInputInt[consumersCount];
        this.consumersResponses = new ChannelOutputInt[consumersCount];
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

    @Override
    public void run() {
        Guard[] guards = Arrays.stream(new Guard[][]{this.producersRequests, this.consumersRequests})
                .flatMap(Arrays::stream)
                .toArray(Guard[]::new);

        Alternative alternativeAll = new Alternative(guards);
        // these other alternatives are used when there is no possibility of the other
        // class of processes to receive attention
        // for example when all buffers are full no Producer should receive attention
        Alternative alternativeProducers = new Alternative(this.producersRequests);
        Alternative alternativeConsumers = new Alternative(this.consumersRequests);

        while (true) {
            int index = buffersTaken == 0 ? alternativeProducers.select() :
                    buffersTaken == this.buffersCount ? alternativeConsumers.select() : alternativeAll.select();

            if (buffersTaken == this.buffersCount) index += this.producersCount;

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
        }
    }
}

enum AlternativeOutput {
    PRODUCER,
    CONSUMER
}
