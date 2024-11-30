package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public class Buffer implements CSProcess {
    private final int index;
    private final int producersCount;
    private int bufferStorage = 0;
    private boolean bufferFull = false;
    private final AltingChannelInputInt[] producersInputs;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutputInt[] consumersResponses;
    private int opCount = 0;
    private boolean isActive = true;

    public Buffer(int bufferIndex, int producersCount, int consumersCount) {
        this.index = bufferIndex;
        this.producersCount = producersCount;

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

    int getOpCount() {
        return this.opCount;
    }

    void deactivate() {
        this.isActive = false;
    }

    public void run() {
        Guard[] guards = Arrays.stream(new Guard[][]{this.producersInputs, this.consumersRequests})
                .flatMap(Arrays::stream)
                .toArray(Guard[]::new);

        Alternative alternative = new Alternative(guards);

        while (this.isActive) {
            int index = alternative.select();
            AlternativeOutput action = index < producersCount ? AlternativeOutput.PRODUCER : AlternativeOutput.CONSUMER;

            switch (action) {
                case PRODUCER -> {
                    if (this.bufferFull) continue;
                    this.bufferStorage = this.producersInputs[index].read(); // consume value and release the producer
                    this.bufferFull = true;
                }
                case CONSUMER -> {
                    if (!this.bufferFull) continue;
                    int consumerIndex = this.consumersRequests[index - this.producersCount].read(); // release the consumer
                    this.bufferFull = false;
                    this.consumersResponses[index - this.producersCount].write(this.bufferStorage); // send the value to consumer
                }
            }
            this.opCount++;
        }
    }
}