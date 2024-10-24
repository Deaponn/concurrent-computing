package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int quantity = 10;
        int producers = 20;
        int materials = 2147483647; // max is 2147483647
        int consumers = 20;
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
        ReentrantLock lock = new ReentrantLock();
        Condition firstProducerWait = lock.newCondition();
        Condition producersWait = lock.newCondition();
        Condition firstConsumerWait = lock.newCondition();
        Condition consumersWait = lock.newCondition();
        Buffer buffer = new Buffer(lock, firstProducerWait, producersWait, firstConsumerWait, consumersWait, maxBuffer);

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

        for (int i = 0; i < producers; i++) {
            producersList[i].join();
        }

        for (int i = 0; i < consumers; i++) {
            consumersList[i].join();
        }

        System.out.print(buffer.getBuffer());
    }
}