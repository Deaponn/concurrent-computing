package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.AltingChannelInputInt;

public class Consumer implements CSProcess {
    private final AltingChannelInputInt channel;

    public Consumer(final AltingChannelInputInt in) {
        channel = in;
    } // constructor

    public void run() {
        int item = channel.read();
        System.out.println(item);
    } // run
} // class Consumer
