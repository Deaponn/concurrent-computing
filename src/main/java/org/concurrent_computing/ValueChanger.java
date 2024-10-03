package org.concurrent_computing;

public class ValueChanger implements Runnable{
    private final Value value;
    private final int delta;
    private final int iterations;
    public ValueChanger(Value value, int delta, int iterations){
        this.value = value;
        this.delta = delta;
        this.iterations = iterations;
    }


    @Override
    public void run() {
        for (int i = 0; i < iterations; i++){
            value.addToValue(this.delta);
        }
    }
}