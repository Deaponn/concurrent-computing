package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public class Buffer implements CSProcess {
    private final int index;
    private final int producersCount;
    private final char[][] bufferStorage;
    private int bufferStackPointer = 0;
    private final AltingChannelInput[] producersInputs;
    private final AltingChannelInputInt[] consumersRequests;
    private final ChannelOutput[] consumersResponses;
    private int opCount = 0;
    private boolean isActive = true;

    public Buffer(int bufferIndex, int bufferCapacity, int itemSize, int producersCount, int consumersCount) {
        this.index = bufferIndex;
        this.bufferStorage = new char[bufferCapacity][itemSize];
        this.producersCount = producersCount;

        this.producersInputs = new AltingChannelInput[producersCount];
        this.consumersRequests = new AltingChannelInputInt[consumersCount];
        this.consumersResponses = new ChannelOutput[consumersCount];
    }

    public int getIndex() {
        return this.index;
    }

    public void registerProducer(int producerIndex, AltingChannelInput receiveData) {
        this.producersInputs[producerIndex] = receiveData;
    }

    public AltingChannelInput registerConsumer(int consumerIndex, AltingChannelInputInt request) {
        One2OneChannel responseChannel = Channel.one2one();

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

        Alternative alternativeAll = new Alternative(guards);
        // these other alternatives are used when there is no possibility for the other
        // class of processes to receive attention
        // for example when all buffers are full no Producer should receive attention
        Alternative alternativeProducers = new Alternative(this.producersInputs);
        Alternative alternativeConsumers = new Alternative(this.consumersRequests);

        while (this.isActive) {
            boolean producersOnly = this.bufferStackPointer == 0;
            boolean consumersOnly = this.bufferStackPointer == this.bufferStorage.length;

            int index = producersOnly ? alternativeProducers.select() :
                    consumersOnly ? alternativeConsumers.select() :
                            alternativeAll.select();

            if (consumersOnly) index += this.producersCount;

            AlternativeOutput action = index < producersCount ? AlternativeOutput.PRODUCER : AlternativeOutput.CONSUMER;

            switch (action) {
                case PRODUCER -> {
                    this.bufferStorage[this.bufferStackPointer] = (char[]) this.producersInputs[index].read(); // consume value and release the producer
                    this.bufferStackPointer++;
                }
                case CONSUMER -> {
                    this.bufferStackPointer--;
                    int consumerIndex = this.consumersRequests[index - this.producersCount].read(); // release the consumer
                    this.consumersResponses[index - this.producersCount].write(this.bufferStorage[this.bufferStackPointer]); // send the value to consumer
                }
            }
            this.opCount++;
        }
    }
}