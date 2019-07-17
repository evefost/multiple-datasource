package com.eve.multiple;

/**
 * @author xieyang
 */
public class InterceptorCounter {

    private volatile int counter;

    public int increase() {
        counter++;
        return counter;
    }

    public int decrease() {
        counter--;
        return counter;
    }

    public int value() {
        return counter;
    }
}