package com.eve.multiple;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author xieyang
 */
public class ContextHolder {


    private  ThreadLocal<InterceptorCounter> interceptorCounter = ThreadLocal.withInitial(() -> new InterceptorCounter());



    private   ThreadLocal<Map<Integer,String>> currentDatabaseId = ThreadLocal.withInitial(() -> new HashMap<>());



    public   void setCurrentDatabaseId(String databaseId){
        Map<Integer, String> currentDatabase = currentDatabaseId.get();
        if (databaseId == null) {
            currentDatabase.remove(interceptorCounter.get().value());
        } else {
            int value = interceptorCounter.get().value();
            currentDatabase.put(value, databaseId);
        }
    }

    public  int increase() {
        return interceptorCounter.get().increase();
    }

    public  int decrease() {
        return interceptorCounter.get().decrease();
    }

    public  String currentDatabaseId() {
        InterceptorCounter transactionCounter = interceptorCounter.get();
        return currentDatabaseId.get().get(transactionCounter.value());
    }

    public  int counterValue() {
        return interceptorCounter.get().value();
    }


}
