package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

public class Consumer implements CSProcess {
    private final int index;
    private final ChannelOutputInt requestBuffer;
    private final AltingChannelInputInt responseBuffer;
    private final ChannelOutputInt[] requestFromBuffer;
    private final AltingChannelInputInt[] receiveFromBuffer;
    private int opCount = 0;
    private boolean isActive = true;
    private final Middleman middleman;

    public Consumer(int consumerIndex, int buffersCount, Middleman middleman) {
        this.index = consumerIndex;

        One2OneChannelInt middlemanChannel = Channel.one2oneInt();
        this.requestBuffer = middlemanChannel.out();
        this.responseBuffer = middleman.registerConsumer(this.index, middlemanChannel.in());

        this.requestFromBuffer = new ChannelOutputInt[buffersCount];
        this.receiveFromBuffer = new AltingChannelInputInt[buffersCount];

        this.middleman = middleman;
    }

    void registerBuffer(Buffer buffer) {
        int bufferIndex = buffer.getIndex();
        One2OneChannelInt requestChannel = Channel.one2oneInt();
        this.requestFromBuffer[bufferIndex] = requestChannel.out();
        this.receiveFromBuffer[bufferIndex] = buffer.registerConsumer(this.index, requestChannel.in());
    }

    int getOpCount() {
        return this.opCount;
    }

    void deactivate() {
        this.isActive = false;
    }

    public void run() {
        while (this.isActive) {
            this.requestBuffer.write(this.index); // request buffer index from middleman
            int bufferIndex = this.responseBuffer.read(); // wait for assigned buffer index
            this.requestFromBuffer[bufferIndex].write(this.index); // tell buffer to go into sending state
            int bufferContent = this.receiveFromBuffer[bufferIndex].read(); // receive from the buffer
            this.opCount++;
        }
        this.middleman.deactivate();
    }
}
