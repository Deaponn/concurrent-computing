package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final Lock lock;
    private final Condition cWait;
    private final Condition pWait;
    private int operationCount = 0;

    Buffer(Lock lock, Condition pWait, Condition cWait, int maxBuffer) {
        this.maxBuffer = maxBuffer;
        this.lock = lock;
        this.cWait = cWait;
        this.pWait = pWait;
    }

    public int getBuffer() {
        return this.buffer;
    }

    public int getOperationCount() {
        return operationCount;
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
            while (this.hasFewerThan(quantity)) this.cWait.await();
            this.buffer -= quantity;
            this.operationCount++;
            pWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void give(int quantity) {
        try {
            this.lock.lock();
            while (this.hasNoSpaceFor(quantity)) this.pWait.await();
            this.buffer += quantity;
            this.operationCount++;
            cWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
