package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // we want producers * produce * material == consumers * consume * needs <= maxBuffer
        int producers = 1;
        int produce = 1;
        int materials = 10;
        int consumers = 2;
        int consume = 1;
        int needs = 5;
        int maxBuffer = 5;

        Thread[] producersList = new Thread[producers];
        Thread[] consumersList = new Thread[consumers];
        Lock lock = new ReentrantLock();
        Condition producersWait = lock.newCondition();
        Condition consumersWait = lock.newCondition();
        Buffer buffer = new Buffer(lock, producersWait, consumersWait, maxBuffer);

        for (int i = 0; i < producers; i++) {
            Producer producer = new Producer(buffer, produce, materials);
            producersList[i] = new Thread(producer);
        }

        for (int i = 0; i < consumers; i++) {
            Consumer consumer = new Consumer(buffer, consume, needs);
            consumersList[i] = new Thread(consumer);
        }

        for (int i = 0; i < producers; i++) {
            producersList[i].start();
        }

        for (int i = 0; i < consumers; i++) {
            consumersList[i].start();
        }

        for (int i = 0; i < producers; i++) {
            producersList[i].join();
        }

        for (int i = 0; i < consumers; i++) {
            consumersList[i].join();
        }

        System.out.print(buffer.getBuffer());
    }
}