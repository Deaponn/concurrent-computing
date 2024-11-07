package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer3Lock extends Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final ReentrantLock producersLock = new ReentrantLock();
    private final ReentrantLock consumersLock = new ReentrantLock();
    private final ReentrantLock commonLock = new ReentrantLock();
    private final Condition commonCondition = this.commonLock.newCondition();
    private int operationCount = 0;

    Buffer3Lock(int maxBuffer) {
        this.maxBuffer = maxBuffer;
    }

    public int getOperationCount() {
        return operationCount;
    }

    boolean hasNoSpaceFor(int quantity) {
        return this.buffer + quantity > this.maxBuffer;
    }

    boolean hasFewerThan(int quantity) {
        return this.buffer < quantity;
    }

    public void take(int quantity) {
        this.consumersLock.lock();
        try {
            this.commonLock.lock();
            try {
                while (this.hasFewerThan(quantity)) this.commonCondition.await();
                this.buffer -= quantity;
                this.operationCount++;
                this.commonCondition.signal();
            } finally {
                this.commonLock.unlock();
            }
        } catch (InterruptedException ignored) {
        } finally {
            this.consumersLock.unlock();
        }
    }

    public void give(int quantity) {
        this.producersLock.lock();
        try {
            this.commonLock.lock();
            try {
                while (this.hasNoSpaceFor(quantity)) this.commonCondition.await();
                this.buffer += quantity;
                this.operationCount++;
                this.commonCondition.signal();
            } finally {
                this.commonLock.unlock();
            }
        } catch (InterruptedException ignored) {
        } finally {
            this.producersLock.unlock();
        }
    }
}
