package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int quantity = 10;
        int producers = 1;
        int materials = 2147483647; // max is 2147483647
        int consumers = 1;
        int needs = 2147483647; // max is 2147483647
        int maxBuffer = 2 * quantity - 1;

        // uklad zdarzen prowadzacy do zakleszczenia dla niewlasciwej
        // dlugosci bufora (maxBuffer < 2 * quantity - 1), np. quantity = 10, maxBuffer = 18
        // buffer: 0, wait: []
        // producent produkuje 9
        // buffer: 9, wait: []
        // producent chce produkowac 10
        // buffer: 9, wait: [producent(10)]
        // konsument chce konsumowac 10
        // buffer: 9, wait: [producent(10), konsument(10)]

        Thread[] producersList = new Thread[producers];
        Thread[] consumersList = new Thread[consumers];
        Lock lock = new ReentrantLock();
        Condition producersWait = lock.newCondition();
        Condition consumersWait = lock.newCondition();
        Buffer buffer = new Buffer(lock, producersWait, consumersWait, maxBuffer);

        for (int i = 0; i < producers; i++) {
            Producer producer = new Producer(buffer, quantity, materials);
            producersList[i] = new Thread(producer);
        }

        for (int i = 0; i < consumers; i++) {
            Consumer consumer = new Consumer(buffer, quantity, needs);
            consumersList[i] = new Thread(consumer);
        }

        for (int i = 0; i < producers; i++) {
            producersList[i].start();
        }

        for (int i = 0; i < consumers; i++) {
            consumersList[i].start();
        }

        TimeUnit.SECONDS.sleep(10);

        System.out.print("Wykonanych operacji: " + buffer.getOperationCount());

        exit(0);
    }
}