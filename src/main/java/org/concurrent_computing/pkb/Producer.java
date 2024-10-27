package src.main.java.org.concurrent_computing.pkb;

import java.util.function.Supplier;

public class Producer implements Runnable {
    private final Buffer buffer;
    private final Supplier<Integer> produce;
    private int materials;

    public Producer(Buffer buffer, Supplier<Integer> produce, int materials) {
        this.buffer = buffer;
        this.produce = produce;
        this.materials = materials;
    }

    @Override
    public void run() {
        while (this.materials-- > 0) {
            this.buffer.give(this.produce.get());
        }
    }
}
