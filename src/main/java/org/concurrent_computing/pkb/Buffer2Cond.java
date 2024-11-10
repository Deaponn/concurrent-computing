package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer2Cond extends Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final Lock lock = new ReentrantLock();
    private final Condition consumersCondition = this.lock.newCondition();
    private final Condition producersCondition = this.lock.newCondition();
    private int operationCount = 0;

    Buffer2Cond(int maxBuffer) {
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
        this.lock.lock();
        try {
            while (this.hasFewerThan(quantity)) this.consumersCondition.await();
            this.buffer -= quantity;
            this.operationCount++;
            producersCondition.signal();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }

    public void give(int quantity) {
        this.lock.lock();
        try {
            while (this.hasNoSpaceFor(quantity)) this.producersCondition.await();
            this.buffer += quantity;
            this.operationCount++;
            consumersCondition.signal();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }
}
