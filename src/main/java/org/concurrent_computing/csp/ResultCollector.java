package src.main.java.org.concurrent_computing.csp;

import org.jcsp.lang.*;

import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class ResultCollector implements CSProcess {
    private final Producer[] producers;
    private final Consumer[] consumers;
    private final Buffer[] buffers;
    private final int timeSleep;

    public ResultCollector(Producer[] producers, Consumer[] consumers, Buffer[] buffers, int timeSleep) {
        this.producers = producers;
        this.consumers = consumers;
        this.buffers = buffers;
        this.timeSleep = timeSleep;
    }

    public void saveResults(int middlemanOpCount) {
        for (Buffer buffer : this.buffers) {
            buffer.deactivate();
        }
        System.out.println("All deactivated");
        exit(0);
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(this.timeSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Producer producer : this.producers) {
            producer.deactivate();
        }

        for (Consumer consumer : this.consumers) {
            consumer.deactivate();
        }
    }
}
