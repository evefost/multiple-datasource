package com.eve.multiple.datasource;

import com.eve.multiple.RouteContextManager;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author xieyang
 */
public class MultipleDataSource extends AbstractDataSource {


    private Map<String, DataSource> dataSources;


    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }



    public DataSource getDatasource(String datasourceId){
       return dataSources.get(datasourceId);
    }

    @Override
    public Connection getConnection() throws SQLException {
        String databaseId = RouteContextManager.currentDatabaseId();
        return dataSources.get(databaseId).getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported by MultipleDataSource");
    }


}
