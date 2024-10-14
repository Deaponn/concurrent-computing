package src.main.java.org.concurrent_computing.pkb;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // we want producers * produce * material == consumers * consume * needs <= maxBuffer
        int producers = 3;
        int produce = 1;
        int materials = 5;
        int consumers = 1;
        int consume = 3;
        int needs = 5;
        int maxBuffer = 5;

        Thread[] producersList = new Thread[producers];
        Thread[] consumersList = new Thread[consumers];
        Buffer buffer = new Buffer(maxBuffer);

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