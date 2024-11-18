package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannelInt;
import org.jcsp.lang.Parallel;

public final class Main {
    public static void main(String[] args) {
        final One2OneChannelInt channel = Channel.one2oneInt();
        CSProcess[] procList = {new Producer(channel.out()), new Consumer(channel.in())};
        Parallel par = new Parallel(procList);
        par.run();
    }
}
