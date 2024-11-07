package src.main.java.org.concurrent_computing.pkb;

import java.util.function.Supplier;

public class Producer implements Runnable {
    private final Buffer buffer;
    private final Supplier<Integer> produce;

    public Producer(Buffer buffer, Supplier<Integer> produce) {
        this.buffer = buffer;
        this.produce = produce;
    }

    private int produce() {
        return this.produce.get();
    }

    @Override
    public void run() {
        while (true) {
            this.buffer.give(this.produce());
        }
    }
}
