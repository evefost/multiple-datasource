
package com.eve.multiple.properties;

import com.eve.multiple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组合数据源属性
 * <p>
 *
 * @author xieyang
 * @date 2019/7/31
 */
public class CompositeMultipleSourceProperties<DP extends BaseDataSourceProperties> implements SourceProperties {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * key:来源
     * 普通共享源key为{@link PropertiesSource#DEFAULT}
     * 租户源以租户id为key
     */
    private Map<String, SourceProperties<MultipleSourceProperties, DP>> datasourceProperties = new HashMap<>();


    public void add(SourceProperties<MultipleSourceProperties, DP> multipleSourceProperties) {
        datasourceProperties.put(multipleSourceProperties.getSource(), multipleSourceProperties);
    }

    @Override
    public Map<String, SourceProperties<MultipleSourceProperties, DP>> getMultipleProperties() {
        return datasourceProperties;
    }


    @Override
    public Map<String, DP> getDatasourceProperties() {
        List<SourceProperties<MultipleSourceProperties, DP>> collect = datasourceProperties.values().stream().collect(Collectors.toList());
        Map<String, DP> merge = new HashMap<>();
        for (SourceProperties<MultipleSourceProperties, DP> spt : collect) {
            String source = spt.getSource();
            Map<String, DP> pp = spt.getDatasourceProperties();
            if (PropertiesSource.DEFAULT.equals(source)) {
                merge.putAll(pp);
            } else {
                Map<String, DP> tmpMap = new HashMap<>(pp.size());
                pp.forEach((k, v) -> {
                    String newKey = source + DatasourceManager.DATABASE_ID_APPEND + k;
                    tmpMap.put(newKey, v);
                });
                merge.putAll(tmpMap);
            }
        }
        return merge;
    }

    @Override
    public DP getProperties(String databaseId, DatabaseMeta meta) {
        if (SourceType.SHARE_DEFAULT.equals(meta.getSourceType()) || SourceType.SHARE.equals(meta.getSourceType())) {
            SourceProperties<MultipleSourceProperties, DP> configFileProperties = datasourceProperties.get(PropertiesSource.DEFAULT);
            if (configFileProperties != null) {
                DP properties = configFileProperties.getProperties(databaseId, meta);
                if (properties != null) {
                    return properties;
                }
            }
            throw new DynamicDatasourceException(meta + "找不到数据源[" + databaseId + "]");
        }

        String currentTenant = RouteContextManager.getCurrentTenant();
        if (currentTenant == null) {
            throw new DynamicDatasourceException(meta + "没有租户信息");
        }
        SourceProperties<MultipleSourceProperties, DP> tenantProperties = datasourceProperties.get(currentTenant);
        if (tenantProperties == null) {
            throw new DynamicDatasourceException(meta + " 没找到租户[" + currentTenant + "]数据源配置信息[" + databaseId + "]");
        }
        DP properties = tenantProperties.getProperties(databaseId, meta);
        if (properties == null) {
            Set<String> dsIds = tenantProperties.getDatasourceProperties().keySet();
            throw new DynamicDatasourceException(meta + "没找数据源[" + databaseId + "]当前租户Id[" + currentTenant + "]数据源" + dsIds);
        }
        return properties;
    }


    @Override
    public DP getDefaultDatabaseProperties(DatabaseMeta meta) {
        SourceType sourceType = meta.getSourceType();
        if (SourceType.SHARE_DEFAULT.equals(sourceType)) {
            DP defaultProperties = datasourceProperties.get(PropertiesSource.DEFAULT).getDefaultDatabaseProperties(meta);
            if (defaultProperties == null) {
                logger.info("没找到默认数据源,请配置默认数据源");
                throw new DynamicDatasourceException(meta + "没找到默认数据源: ");
            }
            return defaultProperties;
        } else {
            String currentTenant = RouteContextManager.getCurrentTenant();
            if (StringUtils.isEmpty(currentTenant)) {
                throw new DynamicDatasourceException(meta + "没有租户信息");
            }
            SourceProperties<MultipleSourceProperties, DP> tenantProperties = datasourceProperties.get(currentTenant);
            if (tenantProperties == null) {
                throw new DynamicDatasourceException(meta + " 没找到租户[" + currentTenant + "]配置数据源");
            }
            DP defaultProperties = tenantProperties.getDefaultDatabaseProperties(meta);
            if (defaultProperties == null) {
                Set<String> dsIds = tenantProperties.getDatasourceProperties().keySet();
                throw new DynamicDatasourceException(meta + "没找到租户[" + currentTenant + "]默认数据源,当前租户数据源" + dsIds);
            }
            return defaultProperties;
        }
    }

    @Override
    public String toString() {
        return "CompositeMultipleSourceProperties{" +
                "datasourceProperties=" + datasourceProperties +
                '}';
    }
}
