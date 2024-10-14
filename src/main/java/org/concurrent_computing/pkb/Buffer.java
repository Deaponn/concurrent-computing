package src.main.java.org.concurrent_computing.pkb;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;

    Buffer(int maxBuffer) {
        this.maxBuffer = maxBuffer;
    }

    public synchronized int getBuffer() {
        return this.buffer;
    }

    public void take(int quantity) throws InterruptedException {
        synchronized (this) {
            if (this.buffer < quantity) wait();
            this.buffer -= quantity;
            System.out.println("consuming " + quantity + ", current " + this.buffer);
            notify();
        }
    }

    public void give(int quantity) throws InterruptedException {
        synchronized (this) {
            if (this.buffer + quantity > this.maxBuffer) wait();
            this.buffer += quantity;
            System.out.println("producing " + quantity + ", current " + this.buffer);
            notify();
        }
    }
}
