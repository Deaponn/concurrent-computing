package src.main.java.org.concurrent_computing.pkb;

public class Producer implements Runnable {
    private final Buffer buffer;
    private final int produce;
    private int materials;

    public Producer(Buffer buffer, int produce, int materials) {
        this.buffer = buffer;
        this.produce = produce;
        this.materials = materials;
    }

    private int produce() {
        return (int) (Math.random() * this.produce + 1);
    }

    @Override
    public void run() {
        while (this.materials-- > 0) {
            this.buffer.give(this.produce());
        }
    }
}
