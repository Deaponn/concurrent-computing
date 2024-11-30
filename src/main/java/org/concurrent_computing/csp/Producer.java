package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.Arrays;

public class Producer implements CSProcess {
    private final int index;
    private final char[] item;
    private final ChannelOutputInt requestBuffer;
    private final AltingChannelInputInt responseBuffer;
    private final ChannelOutput[] sendToBuffer;
    private int opCount = 0;
    private boolean isActive = true;
    private final Middleman middleman;

    public Producer(int producerIndex, int itemSize, int buffersCount, Middleman middleman) {
        this.index = producerIndex;
        this.item = new char[itemSize];

        One2OneChannelInt middlemanChannel = Channel.one2oneInt();
        this.requestBuffer = middlemanChannel.out();
        this.responseBuffer = middleman.registerProducer(this.index, middlemanChannel.in());

        this.sendToBuffer = new ChannelOutput[buffersCount];

        this.middleman = middleman;

        Arrays.fill(this.item, (char) 65);
    }

    void registerBuffer(Buffer buffer) {
        int bufferIndex = buffer.getIndex();
        One2OneChannel bufferChannel = Channel.one2one();
        this.sendToBuffer[bufferIndex] = bufferChannel.out();
        buffer.registerProducer(this.index, bufferChannel.in());
    }

    int getOpCount() {
        return this.opCount;
    }

    void deactivate() {
        this.isActive = false;
    }

    public void run() {
        while (this.isActive) {
            this.requestBuffer.write(this.index); // request buffer index
            int bufferIndex = this.responseBuffer.read(); // wait for assigned buffer index
            this.sendToBuffer[bufferIndex].write(this.item); // send to the buffer
            this.opCount++;
        }
        this.middleman.deactivate();
    }
}
