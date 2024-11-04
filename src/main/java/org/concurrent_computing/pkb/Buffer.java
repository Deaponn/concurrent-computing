package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final ReentrantLock lockProd;
    private final ReentrantLock lockCons;
    private final ReentrantLock lockCommon;
    private final Condition condCommon;
    private int operationCount = 0;

    Buffer(ReentrantLock lockProd, ReentrantLock lockCons, ReentrantLock lockCommon, Condition condCommon, int maxBuffer) {
        this.maxBuffer = maxBuffer;
        this.lockProd = lockProd;
        this.lockCons = lockCons;
        this.lockCommon = lockCommon;
        this.condCommon = condCommon;
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
        this.lockCons.lock();
        try {
            this.lockCommon.lock();
            while (this.hasFewerThan(quantity)) this.condCommon.await();
            this.buffer -= quantity;
            this.operationCount++;
            this.condCommon.signal();
            this.lockCommon.unlock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.lockCons.unlock();
        }
    }

    public void give(int quantity) {
        this.lockProd.lock();
        try {
            this.lockCommon.lock();
            while (this.hasNoSpaceFor(quantity)) this.condCommon.await();
            this.buffer += quantity;
            this.operationCount++;
            this.condCommon.signal();
            this.lockCommon.unlock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.lockProd.unlock();
        }
    }
}
