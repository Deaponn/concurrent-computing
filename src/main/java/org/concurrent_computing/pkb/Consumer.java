package src.main.java.org.concurrent_computing.pkb;

public class Consumer implements Runnable {
    private final Buffer buffer;
    private final int consume;
    private int needs;

    public Consumer(Buffer buffer, int consume, int needs) {
        this.buffer = buffer;
        this.consume = consume;
        this.needs = needs;
    }

    private int consume() {
        return (int) (Math.random() * this.consume + 1);
    }

    @Override
    public void run() {
        while (this.needs-- > 0) {
            this.buffer.take(this.consume());
        }
    }
}