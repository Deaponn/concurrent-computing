package src.main.java.org.concurrent_computing.monitor;

public class Value {
    private int value = 0;

    public synchronized int getValue() {
        return value;
    }

    public synchronized void addToValue(int change) {
        value += change;
    }
}