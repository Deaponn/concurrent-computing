package src.main.java.org.concurrent_computing.pkb;

public abstract class Buffer {
    public abstract int getOperationCount();

    public abstract void take(int quantity);

    public abstract void give(int quantity);
}
