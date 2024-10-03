package org.concurrent_computing;

public class Value {
    private int value = 0;

    public int getValue(){
        return value;
    }

    public void addToValue(int change){
        value += change;
    }
}