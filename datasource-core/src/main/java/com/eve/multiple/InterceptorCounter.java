
package com.eve.multiple;

/**
 * 业务拦截层计数器
 *
 * @author xieyang
 */
public class InterceptorCounter {

    private volatile int counter;

    int increase() {
        counter++;
        return counter;
    }

    int decrease() {
        counter--;
        return counter;
    }

    int value() {
        return counter;
    }
}