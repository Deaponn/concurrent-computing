package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final ReentrantLock lock;
    private final Condition pFirstWait;
    private final Condition pWait;
    private final Condition cFirstWait;
    private final Condition cWait;

    Buffer(ReentrantLock lock, Condition pFirstWait, Condition pWait, Condition cFirstWait, Condition cWait, int maxBuffer) {
        this.maxBuffer = maxBuffer;
        this.lock = lock;
        this.pFirstWait = pFirstWait;
        this.pWait = pWait;
        this.cFirstWait = cFirstWait;
        this.cWait = cWait;
    }

    public int getBuffer() {
        return this.buffer;
    }

    public boolean hasNoSpaceFor(int quantity) {
        return this.buffer + quantity > this.maxBuffer;
    }

    public boolean hasFewerThan(int quantity) {
        return this.buffer < quantity;
    }

    public void take(int quantity) {
        try {
            this.lock.lock();
            while (this.lock.hasWaiters(this.cFirstWait)) this.cWait.await();
            while (this.hasFewerThan(quantity)) this.cFirstWait.await();
            this.buffer -= quantity;
            System.out.println("consuming " + quantity + ", current " + this.buffer);
            cWait.signal();
            pFirstWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void give(int quantity) {
        try {
            this.lock.lock();
            while (this.lock.hasWaiters(this.pFirstWait)) this.pWait.await();
            while (this.hasNoSpaceFor(quantity)) this.pFirstWait.await();
            this.buffer += quantity;
            System.out.println("producing " + quantity + ", current " + this.buffer);
            pWait.signal();
            cFirstWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
