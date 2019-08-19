
package com.eve.multiple;

import com.eve.multiple.properties.BaseDataSourceProperties;
import com.eve.multiple.properties.MultipleSourceProperties;
import com.eve.multiple.properties.SourceProperties;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.eve.multiple.DatasourceManager.DATABASE_ID_APPEND;

/**
 * 数据源路由环境管理器
 * @author xieyang
 */
public class RouteContextManager {


    private static Logger logger = LoggerFactory.getLogger(RouteContextManager.class);

    /**
     * key:className
     */
    private static Map<String, ClassMethodMapper> classMethodMapperMapping;


    private static Map<String, DatabaseMeta> statementDatabaseMapping;

    private static SourceProperties<MultipleSourceProperties, BaseDataSourceProperties> multipleSourceProperties;

    private static ContextHolder serviceContextHolder = new ContextHolder();

    private static ContextHolder transactionContextHolder = new ContextHolder();

    private static ThreadLocal<MappedStatement> currentMapStatement = ThreadLocal.withInitial(() -> null);

    private static ThreadLocal<Boolean> hadUpdateOperateBefore = ThreadLocal.withInitial(() -> false);

    private static ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> null);

    private static Random r = new Random();

    public static void setMultipleSourceProperties(SourceProperties multipleSourceProperties) {
        RouteContextManager.multipleSourceProperties = multipleSourceProperties;
    }

    public static void removeMapStatement() {
        currentMapStatement.remove();
    }

    public static MappedStatement getMapStatement() {
        return currentMapStatement.get();
    }

    public static void setClassMethodMapperMapping(Map<String, ClassMethodMapper> methodMapperMapping) {
        classMethodMapperMapping = methodMapperMapping;
    }

    public static void setMapStatement(MappedStatement statement) {
        currentMapStatement.set(statement);
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

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null) {
            currentTenant.remove();
        } else {
            currentTenant.set(tenantId);
        }
    }

    /**
     * 启动扫描，保证该方法不会返回空
     * 调用方不用作判空处理
     *
     * @param method 某业务方法
     * @return DatabaseMeta DatabaseMeta
     */
    public static DatabaseMeta getDatabase(Class<?> targetClass, Method method) {
        ClassMethodMapper methodMapper = classMethodMapperMapping.get(targetClass.getName());
        MethodDatabase methodMapping = methodMapper.getDatabase(method);
        DatabaseMeta databaseMeta = methodMapping.getDatabaseMeta();
        return databaseMeta;
    }


    static void setStatementDatabaseMapping(Map<String, DatabaseMeta> mapping) {
        statementDatabaseMapping = mapping;
    }

    public static DatabaseMeta getStatementDatabaseMeta(String statementId) {
        return statementDatabaseMapping.get(statementId);
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

    public static void markUpdateOperateFlag() {
        hadUpdateOperateBefore.set(true);
    }

    public static void removeUpdateOperateFlag() {
        hadUpdateOperateBefore.set(false);
    }

    public static boolean hadUpdateBefore() {
        return hadUpdateOperateBefore.get();
    }

    public static String currentDatabaseId() {
        DatabaseMeta databaseMeta;
        if (hasTransaction()) {
            databaseMeta = transactionContextHolder.currentDatabase();
        } else {
            databaseMeta = serviceContextHolder.currentDatabase();
        }
        if (databaseMeta == null) {
            //非普通调用，如检康检查
            databaseMeta = new DatabaseMeta();
            databaseMeta.setSourceType(SourceType.SHARE_DEFAULT);
        }
        SourceType sourceType = databaseMeta.getSourceType();
        if (SourceType.SHARE_DEFAULT.equals(sourceType) ||
                SourceType.TENANT_DEFAULT.equals(sourceType)) {
            if (StringUtils.isEmpty(databaseMeta.getDatabaseId())) {
                databaseMeta = getDefaultDatabase(databaseMeta);
            }
        }

        if (SourceType.SHARE.equals(sourceType) || SourceType.SHARE_DEFAULT.equals(sourceType)) {
            return databaseMeta.getDatabaseId();
        }
        return currentTenant.get() + DATABASE_ID_APPEND + databaseMeta.getDatabaseId();
    }


    public static boolean isMaster(DatabaseMeta meta) {
        String databaseId = getDatabaseId(meta);
        BaseDataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(databaseId, meta);
        return dataSourceProperties.getParentId() == null;
    }


    public static DatabaseMeta getMaster(DatabaseMeta slaver) {
        String slaverId = getDatabaseId(slaver);
        BaseDataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(slaverId, slaver);
        String masterId = dataSourceProperties.getParentId();
        DatabaseMeta databaseMeta = copyDatabaseMeta(slaver);
        databaseMeta.setDatabaseId(masterId);
        return databaseMeta;
    }

    private static DatabaseMeta copyDatabaseMeta(DatabaseMeta meta) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setSourceType(meta.getSourceType());
        databaseMeta.setMethodName(meta.getMethodName());
        return databaseMeta;
    }

    private static String getDatabaseId(DatabaseMeta meta) {
        if (SourceType.SHARE_DEFAULT.equals(meta.getSourceType()) ||
                SourceType.TENANT_DEFAULT.equals(meta.getSourceType())) {
            DatabaseMeta defaultDatabase = getDefaultDatabase(meta);
            return defaultDatabase.getDatabaseId();
        }
        return meta.getDatabaseId();
    }


    public static DatabaseMeta getSlaver(DatabaseMeta master) {
        String masterId = getDatabaseId(master);
        BaseDataSourceProperties dataSourceProperties = multipleSourceProperties.getProperties(masterId, master);
        List<BaseDataSourceProperties> slavers = dataSourceProperties.getSlavers();
        if (slavers.isEmpty()) {
            if (logger.isTraceEnabled()) {
                logger.trace("master db[{}] no have slaver db ", masterId);
            }
            return null;
        }
        DatabaseMeta slaver = copyDatabaseMeta(master);
        if (slavers.size() == 1) {
            String slaverId = slavers.get(0).getId();
            slaver.setDatabaseId(slaverId);
            return slaver;
        }
        String slaverId = slavers.get(r.nextInt(slavers.size())).getId();
        slaver.setDatabaseId(slaverId);
        return slaver;
    }


    private static DatabaseMeta getDefaultDatabase(DatabaseMeta meta) {
        BaseDataSourceProperties defaultDatabaseProperties = multipleSourceProperties.getDefaultDatabaseProperties(meta);
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setMethodName(meta.getMethodName());
        databaseMeta.setSourceType(meta.getSourceType());
        databaseMeta.setDatabaseId(defaultDatabaseProperties.getId());
        return databaseMeta;
    }


}
