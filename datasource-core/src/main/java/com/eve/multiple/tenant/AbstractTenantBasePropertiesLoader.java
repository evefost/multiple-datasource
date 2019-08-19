package com.eve.multiple.tenant;


import com.eve.multiple.DynamicDatasourceException;
import com.eve.multiple.properties.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.eve.multiple.DatasourceManager.DATABASE_ID_APPEND;


/**
 * 租户数据源属性加载器 基础类
 *
 * @param <SRC> 租户源数据类型
 * @param <DSP> 数据源属性基类限定
 * @author xieyang
 * @date 2019/7/26
 */
public abstract class AbstractTenantBasePropertiesLoader<SRC, DSP extends BaseDataSourceProperties> implements DatasourcePropertiesLoader {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 按租户id拆分成多类数据源map
     *
     * @param datasourceMap
     * @return
     * @version 1.0.0
     * @date 2019/7/26
     */
    private Map<String, MultipleSourceProperties> separateByTenantId(Map<String, DSP> datasourceMap) {
        Map<String, MultipleSourceProperties> tenantDatasource = new HashMap<>();
        Set<Map.Entry<String, DSP>> entries = datasourceMap.entrySet();
        for (Map.Entry<String, DSP> entry : entries) {
            DSP value = entry.getValue();
            String tenantId = value.getTenantId();
            MultipleSourceProperties multipleSourceProperties = tenantDatasource.get(tenantId);
            if (multipleSourceProperties == null) {
                multipleSourceProperties = new MultipleSourceProperties();
                multipleSourceProperties.setDatasourceProperties(new HashMap<String, DSP>(2));
                tenantDatasource.put(tenantId, multipleSourceProperties);
            }
            multipleSourceProperties.getDatasourceProperties().put(value.getId(), value);
        }
        return tenantDatasource;
    }


    /**
     * 把租数默认数据源找出，并设置，
     * 如果租户只有一个数据源，则默认设置为默认数据源
     *
     * @param tenantDatabases 专入租户-对应的数据源
     */
    private void findAndSetTenantDefaultDatabase(Map<String, MultipleSourceProperties> tenantDatabases) {
        Set<Map.Entry<String, MultipleSourceProperties>> entries = tenantDatabases.entrySet();
        for (Map.Entry<String, MultipleSourceProperties> entry : entries) {
            MultipleSourceProperties properties = entry.getValue();
            List<BaseDataSourceProperties> mapKeyList = new ArrayList<BaseDataSourceProperties>(properties.getDatasourceProperties().values());
            BaseDataSourceProperties defaultDb = null;
            if (mapKeyList.size() == 1) {
                defaultDb = mapKeyList.get(0);
                defaultDb.setDefault(true);
                properties.setDefaultDatabaseId(defaultDb.getId());
            } else {
                for (BaseDataSourceProperties p : mapKeyList) {
                    if (p.isDefault()) {
                        defaultDb = p;
                        break;
                    }
                }
            }
            if (defaultDb == null) {
                throw new DynamicDatasourceException("多个数据源没有默认数据源");
            }
            properties.setDefaultDatabaseId(defaultDb.getId());
        }
    }

    /**
     * @param list
     * @return
     */
    protected SourceProperties<SourceProperties, DSP> parseAllProperties(List<SRC> list) {
        Map<String, DSP> datasourceMap = new HashMap<>(list.size());
        for (SRC td : list) {
            DSP properties = processItem(td);
            datasourceMap.put(properties.getTenantId() + DATABASE_ID_APPEND + properties.getId(), properties);
        }
        return processResult(datasourceMap);
    }

    protected CompositeMultipleSourceProperties<DSP> processResult(Map<String, DSP> datasourceMap) {
        Map<String, MultipleSourceProperties> stringMultipleSourcePropertiesMap = separateByTenantId(datasourceMap);
        findAndSetTenantDefaultDatabase(stringMultipleSourcePropertiesMap);
        CompositeMultipleSourceProperties result = new CompositeMultipleSourceProperties();
        stringMultipleSourcePropertiesMap.forEach((k, v) -> {
            v.setSource(k);
            result.add(v);
        });
        return result;
    }

    protected abstract DSP processItem(SRC src);
}
