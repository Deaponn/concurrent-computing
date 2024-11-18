package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutputInt;

public class Producer implements CSProcess {
    private final ChannelOutputInt channel;

    public Producer(final ChannelOutputInt out) {
        channel = out;
    } // constructor

    public void run() {
        int item = (int) (Math.random() * 100) + 1;
        channel.write(item);
    } // run
} // class Producer
