package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer4Cond extends Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final Lock lock = new ReentrantLock();
    private boolean firstProducerWaits = false;
    private final Condition firstProducerCondition = this.lock.newCondition();
    private final Condition otherProducersCondition = this.lock.newCondition();
    private boolean firstConsumerWaits = false;
    private final Condition firstConsumerCondition = this.lock.newCondition();
    private final Condition otherConsumersCondition = this.lock.newCondition();
    private int operationCount = 0;

    Buffer4Cond(int maxBuffer) {
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
            while (this.firstConsumerWaits) this.otherConsumersCondition.await();
            this.firstConsumerWaits = true;
            while (this.hasFewerThan(quantity)) this.firstConsumerCondition.await();
            this.buffer -= quantity;
            this.operationCount++;
            this.firstConsumerWaits = false;
            otherConsumersCondition.signal();
            firstProducerCondition.signal();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }

    public void give(int quantity) {
        this.lock.lock();
        try {
            while (this.firstProducerWaits) this.otherProducersCondition.await();
            this.firstProducerWaits = true;
            while (this.hasNoSpaceFor(quantity)) this.firstProducerCondition.await();
            this.buffer += quantity;
            this.operationCount++;
            this.firstProducerWaits = false;
            otherProducersCondition.signal();
            firstConsumerCondition.signal();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }
}
