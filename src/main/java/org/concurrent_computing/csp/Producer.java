package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

public class Producer implements CSProcess {
    private final int index;
    private final ChannelOutputInt requestBuffer;
    private final AltingChannelInputInt responseBuffer;
    private final ChannelOutputInt[] sendToBuffer;

    public Producer(int producerIndex, int buffersCount, Middleman middleman) {
        this.index = producerIndex;

        One2OneChannelInt middlemanChannel = Channel.one2oneInt();
        this.requestBuffer = middlemanChannel.out();
        this.responseBuffer = middleman.registerProducer(this.index, middlemanChannel.in());

        this.sendToBuffer = new ChannelOutputInt[buffersCount];
    }

    void registerBuffer(Buffer buffer) {
        int bufferIndex = buffer.getIndex();
        One2OneChannelInt bufferChannel = Channel.one2oneInt();
        this.sendToBuffer[bufferIndex] = bufferChannel.out();
        buffer.registerProducer(this.index, bufferChannel.in());
    }

    public void run() {
        while (true) {
            int item = (int) (Math.random() * 100) + 1; // produce
            this.requestBuffer.write(this.index); // request buffer index
            int bufferIndex = this.responseBuffer.read(); // wait for assigned buffer index
            this.sendToBuffer[bufferIndex].write(item); // send to the buffer
        }
    }
}
