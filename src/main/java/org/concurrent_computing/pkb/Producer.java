package src.main.java.org.concurrent_computing.pkb;

import src.main.java.org.concurrent_computing.monitor.ValueMonitor;

public class Producer implements Runnable {
    private final Buffer buffer;
    private final int produce;
    private int materials;

    public Producer(Buffer buffer, int produce, int materials) {
        this.buffer = buffer;
        this.produce = produce;
        this.materials = materials;
    }

    @Override
    public void run() {
        while (this.materials-- > 0) {
            try {
                this.buffer.give(this.produce);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
