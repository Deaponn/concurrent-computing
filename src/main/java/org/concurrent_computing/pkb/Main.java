package src.main.java.org.concurrent_computing.pkb;

import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int maxProduction = 100;
        int maxConsumption = 100;
        Supplier<Integer> production = () -> (int) (Math.random() * maxProduction + 1);
        Supplier<Integer> consumption = () -> (int) (Math.random() * maxConsumption + 1);
        int producers = 250;
        int materials = 2147483647; // max is 2147483647
        int consumers = 250;
        int needs = 2147483647; // max is 2147483647
        int maxBuffer = 2 * Math.max(maxProduction, maxConsumption) - 1;

        // uklad zdarzen prowadzacy do zakleszczenia dla niewlasciwej
        // dlugosci bufora (maxBuffer < 2 * quantity - 1), np. quantity = 10, maxBuffer = 18
        // buffer: 0, wait: []
        // producent produkuje 9
        // buffer: 9, wait: []
        // producent chce produkowac 10
        // buffer: 9, wait: [producent(10)]
        // konsument chce konsumowac 10
        // buffer: 9, wait: [producent(10), konsument(10)]

        CustomThread[] producersList = new CustomThread[producers];
        CustomThread[] consumersList = new CustomThread[consumers];
        CustomReentrantLock lock = new CustomReentrantLock();
        Condition firstProducerWait = lock.newCondition();
        Condition producersWait = lock.newCondition();
        Condition firstConsumerWait = lock.newCondition();
        Condition consumersWait = lock.newCondition();
        Buffer buffer = new Buffer(lock, firstProducerWait, producersWait, firstConsumerWait, consumersWait, maxBuffer);

        for (int i = 0; i < producers; i++) {
            Producer producer = new Producer(buffer, production, materials);
            producersList[i] = new CustomThread(producer, "P #" + i);
        }

        for (int i = 0; i < consumers; i++) {
            Consumer consumer = new Consumer(buffer, consumption, needs);
            consumersList[i] = new CustomThread(consumer, "K #" + i);
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