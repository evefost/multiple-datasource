
package com.eve.multiple.properties;

import com.eve.multiple.DynamicDatasourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.*;

/**
 * 组合数据源属性加载器
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class CompositePropertiesLoader implements DatasourcePropertiesLoader<BaseDataSourceProperties> {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private Collection<DatasourcePropertiesLoader> propertiesLoaders;

    private ConfigurableListableBeanFactory beanFactory;

    private AutoConfigScanner autoConfigScanner;

    public CompositePropertiesLoader(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        Map<String, DatasourcePropertiesLoader> beansOfType = beanFactory.getBeansOfType(DatasourcePropertiesLoader.class);
        this.propertiesLoaders = beansOfType.values();
        autoConfigScanner = new AutoConfigPropertiesScanner(beanFactory);
    }

    @Override
    public SourceProperties<SourceProperties, BaseDataSourceProperties> loadProperties() {
        List<SourceProperties<SourceProperties, BaseDataSourceProperties>> propertiesList = new ArrayList<>();
        for (DatasourcePropertiesLoader loader : propertiesLoaders) {
            SourceProperties multipleSourceProperties = loader.loadProperties();
            if (multipleSourceProperties != null) {
                propertiesList.add(multipleSourceProperties);
            }
        }
        try {
            SourceProperties<SourceProperties, BaseDataSourceProperties> scanProperties = autoConfigScanner.scanProperties();
            propertiesList.add(scanProperties);
        } catch (Exception e) {
            logger.error("加载自动配置属性失败:{}", e.getMessage());
        }
        if (propertiesList.isEmpty()) {
            throw new DynamicDatasourceException("没加载到数据源属性");
        }
        SourceProperties<SourceProperties, BaseDataSourceProperties> sourceProperties = mergeProperties(propertiesList);
        if (logger.isDebugEnabled()) {
            logger.debug("当前所有数据源信息:{}", sourceProperties);
        }
        checkAllDefaultDatabase(sourceProperties);
        return sourceProperties;
    }



    /**
     * 以合拼数据源属性
     *
     * @param srcPropertiesList 源数据源属性合并前的
     * @return SourceProperties  合并后的
     */
    private SourceProperties<SourceProperties, BaseDataSourceProperties> mergeProperties(List<SourceProperties<SourceProperties, BaseDataSourceProperties>> srcPropertiesList) {
        Set<String> sourceKeyList = new HashSet<>();
        List<SourceProperties<SourceProperties, BaseDataSourceProperties>> mergeCompositeList = new ArrayList<>();
        srcPropertiesList.forEach((p) -> {
            if (p instanceof CompositeMultipleSourceProperties) {
                CompositeMultipleSourceProperties cp = (CompositeMultipleSourceProperties) p;
                Map<String, MultipleSourceProperties<BaseDataSourceProperties>> multipleProperties = cp.getMultipleProperties();
                Set<String> strings = multipleProperties.keySet();
                multipleProperties.forEach((k, v) -> {
                    mergeCompositeList.add(v);
                });
                sourceKeyList.addAll(strings);
            } else {
                mergeCompositeList.add(p);
                sourceKeyList.add(p.getSource());
            }
        });
        CompositeMultipleSourceProperties result = new CompositeMultipleSourceProperties();
        sourceKeyList.forEach((source) -> {
            List<SourceProperties<SourceProperties, BaseDataSourceProperties>> sourceProperties = new ArrayList<>();
            mergeCompositeList.forEach((sp) -> {
                if (source.equals(sp.getSource())) {
                    sourceProperties.add(sp);
                }
            });
            SourceProperties<SourceProperties, BaseDataSourceProperties> mergeProperties = sourceProperties.get(0);
            for (SourceProperties<SourceProperties, BaseDataSourceProperties> p : sourceProperties) {
                mergeProperties.merge(p);
            }
            result.add(mergeProperties);
        });
        return result;
    }

    /**
     * 检测默认数据源是否配置有重复
     *
     * @param sourceProperties
     */
    private void checkAllDefaultDatabase(SourceProperties<SourceProperties, BaseDataSourceProperties> sourceProperties) {
        Map<String, SourceProperties<SourceProperties, BaseDataSourceProperties>> multipleProperties = sourceProperties.getMultipleProperties();
        if (multipleProperties != null) {
            multipleProperties.forEach((source, properties) -> {
                Set<String> defaultIds = new HashSet<>();
                Map<String, BaseDataSourceProperties> datasourceProperties = properties.getDatasourceProperties();
                datasourceProperties.forEach((id, value) -> {
                    if (value.isDefault()) {
                        defaultIds.add(value.getId());
                    }
                });
                if (defaultIds.size() > 1) {
                    logger.warn("源[{}] 有多个默认数据源{}，请检查配置", source, defaultIds);
                }
            });
        }
    }

}
