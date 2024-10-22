package src.main.java.org.concurrent_computing.pkb;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.stream.Stream;

public class Buffer {
    private int buffer = 0;
    private final int maxBuffer;
    private final CustomReentrantLock lock;
    private boolean pFirstWaits = false;
    private final Condition pFirstWait;
    private final Condition pWait;
    private boolean cFirstWaits = false;
    private final Condition cFirstWait;
    private final Condition cWait;
    private Collection<CustomThread> previousThreads = new LinkedList<>();

    Buffer(CustomReentrantLock lock, Condition pFirstWait, Condition pWait, Condition cFirstWait, Condition cWait, int maxBuffer) {
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

    private void log() {
        Collection<CustomThread> consumers = (Collection) this.lock.getWaitingThreadsOwn(this.cWait);
        Collection<CustomThread> producers = (Collection) this.lock.getWaitingThreadsOwn(this.pWait);
        Collection<CustomThread> allThreads = Stream.concat(consumers.stream(), producers.stream()).toList();

        for (CustomThread thread : allThreads) {
            if (this.previousThreads.contains(thread)) {
                thread.increaseStarving();
            } else {
                thread.startStarving();
            }
        }

        List<CustomThread> consumersSorted = consumers.stream().sorted(CustomThread::compare).toList();
        List<CustomThread> producersSorted = producers.stream().sorted(CustomThread::compare).toList();

        if (!consumersSorted.isEmpty() && consumersSorted.get(0).getStarving() > 300)
            System.out.println(
                    "Consumers: "
                            + consumersSorted
                            + ", Producers: "
                            + producersSorted
            );

        this.previousThreads = allThreads;
    }

    public void take(int quantity) {
        try {
            this.lock.lock();
            this.log();
            while (this.cFirstWaits) this.cWait.await();
            this.cFirstWaits = true;
            while (this.hasFewerThan(quantity)) this.cFirstWait.await();
            this.buffer -= quantity;
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
            this.log();
            while (this.pFirstWaits) this.pWait.await();
            this.pFirstWaits = true;
            while (this.hasNoSpaceFor(quantity)) this.pFirstWait.await();
            this.buffer += quantity;
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
