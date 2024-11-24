package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public class Buffer implements CSProcess {
    private final int index;
    private final int producersCount;
    private final int consumersCount;
    private boolean bufferFull = false;
    private final AltingChannelInputInt[] producersInputs;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutputInt[] consumersResponses;

    public Buffer(int bufferIndex, int producersCount, int consumersCount) {
        this.index = bufferIndex;
        this.producersCount = producersCount;
        this.consumersCount = consumersCount;

        this.producersInputs = new AltingChannelInputInt[producersCount];
        this.consumersRequests = new AltingChannelInputInt[consumersCount];
        this.consumersResponses = new ChannelOutputInt[consumersCount];
    }

    public int getIndex() {
        return this.index;
    }

    public void registerProducer(int producerIndex, AltingChannelInputInt receiveData) {
        this.producersInputs[producerIndex] = receiveData;
    }

    public AltingChannelInputInt registerConsumer(int consumerIndex, AltingChannelInputInt request) {
        One2OneChannelInt responseChannel = Channel.one2oneInt();

        this.consumersRequests[consumerIndex] = request;
        this.consumersResponses[consumerIndex] = responseChannel.out();

        return responseChannel.in();
    }

    public void run() {
        Guard[] guards = Arrays.stream(new Guard[][]{this.producersInputs, this.consumersRequests})
                .flatMap(Arrays::stream)
                .toArray(Guard[]::new);

        Alternative alternative = new Alternative(guards);

        while (true) {
            int index = alternative.select();
            AlternativeOutput action = index < producersCount ? AlternativeOutput.PRODUCER :
                    index < producersCount + consumersCount ? AlternativeOutput.CONSUMER : AlternativeOutput.SKIP;

            switch (action) {
                case PRODUCER -> {
                    if (this.bufferFull) continue;
                    int item = this.producersInputs[index].read(); // consume value and release the producer
                    this.bufferFull = true;
                }
                case CONSUMER -> {
                    if (!this.bufferFull) continue;
                    int item = this.consumersRequests[index - this.producersCount].read(); // release the consumer
                    this.bufferFull = false;
                    this.consumersResponses[index - this.producersCount].write(0); // send the value to consumer
                }
                case SKIP -> {
                }
            }
        }
    }
}