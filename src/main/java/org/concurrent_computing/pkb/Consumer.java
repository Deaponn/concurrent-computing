package src.main.java.org.concurrent_computing.pkb;

import java.util.function.Supplier;

public class Consumer implements Runnable {
    private final Buffer buffer;
    private final Supplier<Integer> consume;

    public Consumer(Buffer buffer, Supplier<Integer> consume) {
        this.buffer = buffer;
        this.consume = consume;
    }

    private int consume() {
        return this.consume.get();
    }

    @Override
    public void run() {
        while (true) {
            this.buffer.take(this.consume());
        }
    }
}