package src.main.java.org.concurrent_computing.pkb;

import java.util.function.Supplier;

public class Consumer implements Runnable {
    private final Buffer buffer;
    private final Supplier<Integer> consume;
    private int needs;

    public Consumer(Buffer buffer, Supplier<Integer> consume, int needs) {
        this.buffer = buffer;
        this.consume = consume;
        this.needs = needs;
    }

    @Override
    public void run() {
        while (this.needs-- > 0) {
            this.buffer.take(this.consume.get());
        }
    }
}