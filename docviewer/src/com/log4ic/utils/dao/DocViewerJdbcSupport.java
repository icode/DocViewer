package com.log4ic.utils.dao;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-23
 * @time: 上午2:48
 */
public abstract class DocViewerJdbcSupport extends AbstractJdbcSupport {
    private static final String DEFAULT_DATA_SOURCE = "docviewerDB";

    protected DataSource getDocviewerDataSource() throws NamingException {
        return super.getDataSource(DEFAULT_DATA_SOURCE);
    }

    protected Connection getDocviewerConnection() throws NamingException, SQLException {
        return super.getConnection(DEFAULT_DATA_SOURCE);
    }

    protected int executeUpdate(String sql, Map<Integer, Object> params) throws SQLException, NamingException {
        return super.executeUpdate(DEFAULT_DATA_SOURCE, sql, params);
    }

    protected int executeUpdate(String sql) throws SQLException, NamingException {
        return super.executeUpdate(DEFAULT_DATA_SOURCE, sql, null);
    }

    protected boolean execute(String sql) throws SQLException, NamingException {
        return super.execute(DEFAULT_DATA_SOURCE, sql, null);
    }

    protected boolean execute(String sql, Map<Integer, Object> params) throws SQLException, NamingException {
        return super.execute(DEFAULT_DATA_SOURCE, sql, params);
    }

    public boolean hasTable() {
        return super.hasTable(DEFAULT_DATA_SOURCE);
    }


    public boolean createTable() throws NamingException, SQLException {
        return super.createTable(DEFAULT_DATA_SOURCE);
    }
}
