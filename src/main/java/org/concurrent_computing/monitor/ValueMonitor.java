package src.main.java.org.concurrent_computing.monitor;

public class ValueMonitor {
    private int value = 0;

    public synchronized int getValue() {
        return value;
    }

    public synchronized void produce() throws InterruptedException {
        while (this.value != 0) wait();
        System.out.println("produce");
        this.value += 1;
        notifyAll();
    }

    public synchronized void consume() throws InterruptedException {
        while (this.value == 0) wait();
        System.out.println("consume");
        this.value -= 1;
        notifyAll();
    }
}
