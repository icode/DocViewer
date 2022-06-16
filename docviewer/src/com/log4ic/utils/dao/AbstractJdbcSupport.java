package com.log4ic.utils.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.*;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-23
 * @time: 上午2:39
 */
public abstract class AbstractJdbcSupport {
    private Log logger = LogFactory.getLog(this.getClass());

    protected DataSource getDataSource(String dataSourceName) throws NamingException {
        Context initContext = new InitialContext();
        if (initContext == null) {
            throw new NamingException();
        }
        return (DataSource) initContext.lookup("java:comp/env/jdbc/" + dataSourceName);
    }

    protected Connection getConnection(String dataSourceName) throws SQLException, NamingException {
        DataSource dataSource = this.getDataSource(dataSourceName);
        if (dataSource == null) {
            return null;
        }
        return dataSource.getConnection();
    }

    protected int executeUpdate(String dataSourceName, String sql, Map<Integer, Object> params) throws SQLException, NamingException {
        Connection conn = this.getConnection(dataSourceName);
        if (conn == null) {
            throw new SQLException();
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (Integer index : params.keySet()) {
                    stmt.setObject(index, params.get(index));
                }
            }
            int result = stmt.executeUpdate();
            conn.commit();
            return result;
        } catch (Exception e) {
            conn.rollback();
            logger.error(e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.close();
        }
        return 0;
    }


    protected boolean execute(String dataSourceName, String sql, Map<Integer, Object> params) throws SQLException, NamingException {
        Connection conn = this.getConnection(dataSourceName);
        if (conn == null) {
            throw new SQLException();
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (Integer index : params.keySet()) {
                    stmt.setObject(index, params.get(index));
                }
            }
            return stmt.execute();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.commit();
            conn.close();
        }
    }

    protected boolean execute(String dataSourceName, String sql) throws SQLException, NamingException {
        return this.execute(dataSourceName, sql, null);
    }

    protected boolean hasTable(String dataSourceName, String schema, String name) {
        //判断某一个表是否存在
        boolean result = false;
        try {
            DatabaseMetaData meta = this.getConnection(dataSourceName).getMetaData();
            ResultSet set = meta.getTables(null, schema, name.toUpperCase(), new String[]{"TABLE"});
            while (set.next()) {
                result = true;
            }
        } catch (Exception eHasTable) {
            System.err.println(eHasTable);
            eHasTable.printStackTrace();
        }
        return result;
    }

    protected boolean hasTable(String dataSourceName, String name) {
        return this.hasTable(dataSourceName, null, name);
    }

    public String getTableName() {
        String name = null;

        Class selfClass = this.getClass();
        Annotation tableAnnotation = selfClass.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            try {
                Table table = (Table) tableAnnotation.getClass().newInstance();
                name = table.name();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isBlank(name)) {
            name = selfClass.getSimpleName();
        }
        return name;
    }

    public boolean hasTable(String dataSourceName) {

        String name = this.getTableName();

        return this.hasTable(dataSourceName, name);
    }


    public boolean createTable(String dataSourceName) throws NamingException, SQLException {
        StringBuffer buffer = new StringBuffer("create table ");
        buffer.append(this.getTableName());
        buffer.append("(");
        Class selfClass = this.getClass();
        Field[] fields = selfClass.getDeclaredFields();
        if (fields.length == 0) {
            return false;
        }
        boolean hasColumn = false;
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            Id idAnnotation = field.getAnnotation(Id.class);
            GeneratedValue generatedValueAnnotation = field.getAnnotation(GeneratedValue.class);
            if (columnAnnotation != null) {
                hasColumn = true;
                buffer.append(field.getName());
                buffer.append(" ");
                String columnDefinition = columnAnnotation.columnDefinition();
                boolean isInt = false;
                if (StringUtils.isNotBlank(columnDefinition)) {
                    buffer.append(columnDefinition);
                } else {
                    Class fieldType = field.getType();
                    if (fieldType.isInstance(0)) {
                        buffer.append("int ");
                    } else if (fieldType.getCanonicalName().equals("int")) {
                        buffer.append("int not null ");
                        isInt = true;
                    } else if (fieldType.isInstance("")) {
                        buffer.append("varchar");
                        buffer.append("(");
                        buffer.append(columnAnnotation.length());
                        buffer.append(") ");
                    } else if (fieldType.equals(Date.class)) {
                        buffer.append("date ");
                    } else if (fieldType.isInstance(new java.util.Date()) || fieldType.equals(Timestamp.class)) {
                        buffer.append("timestamp ");
                    }
                }

                if (generatedValueAnnotation != null && generatedValueAnnotation.strategy() == GenerationType.AUTO) {
                    buffer.append("generated always as identity (start with 1, increment by 1) ");
                }

                if (idAnnotation != null) {
                    buffer.append("primary key ");
                }

                if (!columnAnnotation.nullable() && !isInt) {
                    buffer.append("not null");
                }
                buffer.append(",");
            }
        }
        if (!hasColumn) {
            return hasColumn;
        }
        buffer.deleteCharAt(buffer.length() - 1);

        buffer.append(")");
        logger.info(buffer.toString().replace("  ", " "));
        return this.execute(dataSourceName, buffer.toString().replace("  ", " "));
    }
}
