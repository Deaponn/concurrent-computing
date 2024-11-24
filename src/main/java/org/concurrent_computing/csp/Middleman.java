package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;
import java.util.stream.Stream;

public class Middleman implements CSProcess {
    private final int buffersCount;
    private final int producersCount;
    private final int consumersCount;
    private int nextProducerBuffer = 0;
    private int nextConsumerBuffer = 0;
    private final AltingChannelInputInt[] producersRequests;
    private final ChannelOutputInt[] producersResponses;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutputInt[] consumersResponses;

    public Middleman(int buffersCount, int producersCount, int consumersCount) {
        this.buffersCount = buffersCount;
        this.producersCount = producersCount;
        this.consumersCount = consumersCount;

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
        // guards for every requestInput (prod and cons)
        // then assign them a buffer with index next<Prod/Cons>Buffer

        Guard[] guards = Arrays.stream(new Guard[][]{this.producersRequests, this.consumersRequests, {new Skip()}})
                .flatMap(Arrays::stream)
                .toArray(Guard[]::new);

        Alternative alternative = new Alternative(guards);

        while (true) {
            int index = alternative.select();
            AlternativeOutput action = index < producersCount ? AlternativeOutput.PRODUCER :
                    index < producersCount + consumersCount ? AlternativeOutput.CONSUMER : AlternativeOutput.SKIP;

            switch (action) {
                case PRODUCER -> {
                    int item = this.producersRequests[index].read();
                    this.producersResponses[index].write(this.nextProducerBuffer);
                    this.nextProducerBuffer = (this.nextProducerBuffer + 1) % this.buffersCount;
                }
                case CONSUMER -> {
                    int item = this.consumersRequests[index - this.producersCount].read();
                    this.consumersResponses[index - this.producersCount].write(this.nextConsumerBuffer);
                    this.nextConsumerBuffer = (this.nextConsumerBuffer + 1) % this.buffersCount;
                }
                case SKIP -> {
                }
            }
        }
    }
}

enum AlternativeOutput {
    PRODUCER,
    CONSUMER,
    SKIP
}
