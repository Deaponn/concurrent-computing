package src.main.java.org.concurrent_computing.pkb;

import java.util.Objects;

public class CustomThread extends Thread {
    private final String threadName;
    private int starvingFor = 0;

    public CustomThread(Runnable runnable, String name) {
        super(runnable);
        this.threadName = name;
    }

    public int getStarving() {
        return this.starvingFor;
    }

    public void increaseStarving() {
        this.starvingFor++;
    }

    public void startStarving() {
        this.starvingFor = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomThread that = (CustomThread) o;
        return Objects.equals(threadName, that.threadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadName);
    }

    @Override
    public String toString() {
        return this.threadName + " - " + this.starvingFor;
    }

    public static int compare(CustomThread a, CustomThread b) {
        return b.getStarving() - a.getStarving();
    }
}
