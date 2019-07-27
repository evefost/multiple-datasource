
package com.eve.multiple;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author xieyang
 */
public class ContextHolder {


    private  ThreadLocal<InterceptorCounter> interceptorCounter = ThreadLocal.withInitial(() -> new InterceptorCounter());


    private ThreadLocal<Map<Integer, DatabaseMeta>> currentDatabaseHolder = ThreadLocal.withInitial(() -> new HashMap<>());

    public void setCurrentDatabase(DatabaseMeta database) {
        Map<Integer, DatabaseMeta> currentDatabase = currentDatabaseHolder.get();
        if (database == null) {
            currentDatabase.remove(interceptorCounter.get().value());
        } else {
            int value = interceptorCounter.get().value();
            currentDatabase.put(value, database);
        }
    }



    public  int increase() {
        return interceptorCounter.get().increase();
    }

    public  int decrease() {
        return interceptorCounter.get().decrease();
    }



    public DatabaseMeta currentDatabase() {
        InterceptorCounter transactionCounter = interceptorCounter.get();
        return currentDatabaseHolder.get().get(transactionCounter.value());
    }

    public  int counterValue() {
        return interceptorCounter.get().value();
    }


}
