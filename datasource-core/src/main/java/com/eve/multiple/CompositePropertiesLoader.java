
package com.eve.multiple;


import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.MultipleSourceProperties;

import java.util.*;

/**
 * 组合数据源属性加载器
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/7/26
 */
public class CompositePropertiesLoader implements DatasourcePropertiesLoader {

    private Collection<DatasourcePropertiesLoader> propertiesLoaders;

    public CompositePropertiesLoader(Collection<DatasourcePropertiesLoader> propertiesLoaders) {
        this.propertiesLoaders = propertiesLoaders;
    }

    @Override
    public MultipleSourceProperties<DataSourceProperties> loadProperties(){
        List<MultipleSourceProperties<DataSourceProperties>> propertiesList = new ArrayList<>();
        for (DatasourcePropertiesLoader loader:propertiesLoaders){
            propertiesList.add(loader.loadProperties());
        }
        if(propertiesList.isEmpty()){
            throw new RuntimeException("没加载到数据源属性");
        }
        return mergeProperties(propertiesList);
    }

    private MultipleSourceProperties mergeProperties(List<MultipleSourceProperties<DataSourceProperties>> propertiesList){
        if(propertiesList.size()==1){
            return propertiesList.get(0);
        }
        MultipleSourceProperties result = new MultipleSourceProperties();
        Map<String, DataSourceProperties> datasourceProperties = new HashMap<>();
        String defautlId = null;
        for(MultipleSourceProperties ps:propertiesList){
            datasourceProperties.putAll(ps.getDatasourceProperties());
            defautlId = ps.getDefaultDatabaseId();
        }
        result.setDatasourceProperties(datasourceProperties);
        result.setDefaultDatabaseId(defautlId);
        return result;
    }
}
