package src.main.java.org.concurrent_computing;

public class ValueChanger implements Runnable {
    private final ValueMonitor value;
    private final boolean produce;
    private final int iterations;

    public ValueChanger(ValueMonitor value, boolean produce, int iterations) {
        this.value = value;
        this.produce = produce;
        this.iterations = iterations;
    }


    @Override
    public void run() {
        try {
            if (this.produce) {
                for (int i = 0; i < this.iterations; i++) {
                    this.value.produce();
                }
            } else {
                for (int i = 0; i < this.iterations; i++) {
                    this.value.consume();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}