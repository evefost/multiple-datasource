
package com.eve.multiple;


import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.MultipleSourceProperties;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author xieyang
 */
public class RouteContextManager {

    public static final String DATABASE_ID_APPEND="_";

    private static Logger logger = LoggerFactory.getLogger(RouteContextManager.class);

    private static Map<Method, MethodDatabase> methodDatabaseMapping;

    private static Map<String, DatabaseMeta> statementDatabaseMapping;

    private static MultipleSourceProperties<DataSourceProperties> multipleSourceProperties;

    private static ContextHolder serviceContextHolder = new ContextHolder();


    private static ContextHolder transactionContextHolder = new ContextHolder();

    private static ThreadLocal<MappedStatement> currentMapStatement = ThreadLocal.withInitial(() -> null);

    private static ThreadLocal<Boolean> hadUpdateOperateBefore = ThreadLocal.withInitial(() -> false);

    private static ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> null);

    private static Random r = new Random();

    public static void setMultipleSourceProperties(MultipleSourceProperties multipleSourceProperties) {
        RouteContextManager.multipleSourceProperties = multipleSourceProperties;
    }

    public static void setMapStatement(MappedStatement statement) {
        currentMapStatement.set(statement);
    }

    public static void removeMapStatement() {
        currentMapStatement.remove();
    }


    public static MappedStatement getMapStatement() {
        return currentMapStatement.get();
    }


    public static int increase(boolean transaction) {
        if (transaction) {
            return transactionContextHolder.increase();
        } else {
            return serviceContextHolder.increase();
        }

    }

    public static int decrease(boolean transaction) {
        if (transaction) {
            return transactionContextHolder.decrease();
        } else {
            return serviceContextHolder.decrease();
        }
    }

    public static void setCurrentDatabase(DatabaseMeta database, boolean transaction) {
        if (transaction) {
            transactionContextHolder.setCurrentDatabase(database);
        } else {
            serviceContextHolder.setCurrentDatabase(database);
        }
    }

    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null) {
            currentTenant.remove();
        } else {
            currentTenant.set(tenantId);
        }
    }


    public static DatabaseMeta getDatabase(Method method) {
        MethodDatabase methodMapping = methodDatabaseMapping.get(method);
        if (methodMapping == null) {
            return null;
        }
        DatabaseMeta databaseMeta = methodMapping.getDatabaseMeta();
        return databaseMeta;
    }


    public static void setMethodDatabaseMapping(Map<Method, MethodDatabase> mapping) {
        methodDatabaseMapping = mapping;
    }

    public static void setStatementDatabaseMapping(Map<String, DatabaseMeta> mapping) {
        statementDatabaseMapping = mapping;
    }

    public static DatabaseMeta getStatementDatabaseMeta(String statementId) {
        return statementDatabaseMapping.get(statementId);
    }

    public static String currentDatabaseId() {
        DatabaseMeta databaseMeta = null;
        if (hasTransaction()) {
            databaseMeta = transactionContextHolder.currentDatabase();
        } else {
            databaseMeta = serviceContextHolder.currentDatabase();
        }
        if (databaseMeta.isShare()) {
            return databaseMeta.getDatabaseId();
        }
        if (currentTenant.get() == null) {
            return databaseMeta.getDatabaseId();
        }
        return currentTenant.get() + "_" + databaseMeta.getDatabaseId();

    }

    public static DatabaseMeta currentDatabase() {
        if (hasTransaction()) {
            return transactionContextHolder.currentDatabase();
        }
        return serviceContextHolder.currentDatabase();
    }

    public static boolean hasTransaction() {
        return transactionContextHolder.counterValue() != 0;
    }


    public static boolean isMaster(DatabaseMeta database) {
        String databaseId = getDatabaseId(database);
        DataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(databaseId);
        return dataSourceProperties.getParentId() == null;
    }


    public static DatabaseMeta getMaster(DatabaseMeta slaver) {
        String slaverId = getDatabaseId(slaver);
        DataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(slaverId);
        String masterId = dataSourceProperties.getParentId();
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setDatabaseId(masterId);
        databaseMeta.setShare(slaver.isShare());
        return databaseMeta;
    }

    private static String getDatabaseId(DatabaseMeta database) {

        if (database.isShare()) {
            return database.getDatabaseId();
        } else {
            String tenantId = currentTenant.get();
            if(tenantId == null){
               throw new RuntimeException("没有租户信息");
            }
            return currentTenant.get() + DATABASE_ID_APPEND + database.getDatabaseId();
        }
    }


    public static DatabaseMeta getSlaver(DatabaseMeta master) {
        String masterId = getDatabaseId(master);
        DataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(masterId);
        List<DataSourceProperties> slavers = dataSourceProperties.getSlavers();
        if (slavers.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("master db[{}] no have slaver db ", masterId);
            }
            return null;
        }
        DatabaseMeta slaver = new DatabaseMeta();
        slaver.setShare(master.isShare());
        if (slavers.size() == 1) {
            String slaverId = slavers.get(0).getId();
            slaver.setDatabaseId(slaverId);

            return slaver;
        }
        String slaverId = slavers.get(r.nextInt(slavers.size())).getId();
        slaver.setDatabaseId(slaverId);
        return slaver;
    }


    public static DatabaseMeta getDefaultDatabase() {
        DataSourceProperties defaultDatabaseProperties = multipleSourceProperties.getDefaultDatabaseProperties();
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setDatabaseId(defaultDatabaseProperties.getId());
        databaseMeta.setShare(defaultDatabaseProperties.isShare());
        return databaseMeta;
    }


    public static void markUpdateOperateFlag() {
        hadUpdateOperateBefore.set(true);
    }

    public static void removeUpdateOperateFlag() {
        hadUpdateOperateBefore.set(false);
    }

    public static boolean hadUpdateBefore() {
        return hadUpdateOperateBefore.get();
    }

}
