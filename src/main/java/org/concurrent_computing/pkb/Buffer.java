package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final Lock lock;
    private boolean pFirstWaits = false;
    private final Condition pFirstWait;
    private final Condition pWait;
    private int operationCount = 0;
    private boolean cFirstWaits = false;
    private final Condition cFirstWait;
    private final Condition cWait;

    Buffer(Lock lock, Condition pFirstWait, Condition pWait, Condition cFirstWait, Condition cWait, int maxBuffer) {
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
            while (this.cFirstWaits) this.cWait.await();
            this.cFirstWaits = true;
            while (this.hasFewerThan(quantity)) this.cFirstWait.await();
            this.buffer -= quantity;
            this.operationCount++;
            this.cFirstWaits = false;
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
            while (this.pFirstWaits) this.pWait.await();
            this.pFirstWaits = true;
            while (this.hasNoSpaceFor(quantity)) this.pFirstWait.await();
            this.buffer += quantity;
            this.operationCount++;
            this.pFirstWaits = false;
            pWait.signal();
            cFirstWait.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
