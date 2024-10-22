package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final CustomReentrantLock lock;
    private final Condition cWait;
    private final Condition pWait;

    Buffer(CustomReentrantLock lock, Condition pWait, Condition cWait, int maxBuffer) {
        this.maxBuffer = maxBuffer;
        this.lock = lock;
        this.cWait = cWait;
        this.pWait = pWait;
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

    private void log() {
        System.out.println("consumers: " + this.lock.getWaitingThreadsOwn(this.cWait) +
                " producers: " + this.lock.getWaitingThreadsOwn(this.pWait));
    }

    public void take(int quantity) {
        try {
            this.lock.lock();
            while (this.hasFewerThan(quantity)) this.cWait.await();
            this.buffer -= quantity;
            System.out.println("consuming " + quantity + ", current " + this.buffer);
            this.log();
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
            while (this.hasNoSpaceFor(quantity)) this.pWait.await();
            this.buffer += quantity;
            System.out.println("producing " + quantity + ", current " + this.buffer);
            this.log();
            cWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
