
package com.eve.multiple;

import java.util.HashMap;
import java.util.Map;

/**
 * 执行链上下文 holder
 *
 * @author xieyang
 */
public class ContextHolder {


    private ThreadLocal<InterceptorCounter> interceptorCounter = ThreadLocal.withInitial(() -> new InterceptorCounter());


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


    int increase() {
        return interceptorCounter.get().increase();
    }

    int decrease() {
        return interceptorCounter.get().decrease();
    }


    DatabaseMeta currentDatabase() {
        InterceptorCounter transactionCounter = interceptorCounter.get();
        return currentDatabaseHolder.get().get(transactionCounter.value());
    }

    int counterValue() {
        return interceptorCounter.get().value();
    }


}
