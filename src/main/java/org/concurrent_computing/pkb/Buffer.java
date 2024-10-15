package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final Lock lock;
    private final Condition cWait;
    private final Condition pWait;

    Buffer(Lock lock, Condition pWait, Condition cWait, int maxBuffer) {
        this.maxBuffer = maxBuffer;
        this.lock = lock;
        this.cWait = cWait;
        this.pWait = pWait;
    }

    public int getBuffer() {
        return this.buffer;
    }

    public boolean isFull() {
        return this.buffer == this.maxBuffer;
    }

    public boolean isEmpty() {
        return this.buffer == 0;
    }

    public void take(int quantity) {
//        if (this.buffer < quantity) wait();
        try {
            this.lock.lock();
            while (this.isEmpty()) this.cWait.await();
            this.buffer -= quantity;
            System.out.println("consuming " + quantity + ", current " + this.buffer);
            pWait.signal();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void give(int quantity) {
//        if (this.buffer + quantity > this.maxBuffer) wait();
        try {
            this.lock.lock();
            while (this.isFull()) this.pWait.await();
            this.buffer += quantity;
            System.out.println("producing " + quantity + ", current " + this.buffer);
            cWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
