package src.main.java.org.concurrent_computing.pkb;

import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CustomReentrantLock extends ReentrantLock {
    Collection<Thread> getWaitingThreadsOwn(Condition condition) {
        return this.getWaitingThreads(condition);
    }
}
