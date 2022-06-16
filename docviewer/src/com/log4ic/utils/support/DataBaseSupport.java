package com.log4ic.utils.support;

import com.log4ic.utils.dao.DocViewerJdbcSupport;
import com.log4ic.utils.io.scanner.FileScanner;
import com.log4ic.utils.support.scanner.filter.AnnotationFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-28
 * @time: 上午2:51
 */
public class DataBaseSupport implements ServletContextListener {
    private static final Log LOGGER = LogFactory.getLog(DataBaseSupport.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.info("初始化实体表....");
        AnnotationFilter annotationFilter = new AnnotationFilter();
        FileScanner fileScanner = new FileScanner(annotationFilter);
        LOGGER.info("查找相关实体....");
        fileScanner.find("com.log4ic.entity");

        List<Class> entityClassList = annotationFilter.getClassList();

        try {
            LOGGER.info("查看是否建表....");
            for (Class clazz : entityClassList) {
                DocViewerJdbcSupport support = (DocViewerJdbcSupport) clazz.newInstance();
                boolean exist = support.hasTable();
                String tableName = support.getTableName();
                LOGGER.info("表[" + tableName + "]" + (exist ? "" : "不") + "存在");
                if (!exist) {
                    LOGGER.info("创建表[" + tableName + "]");
                    support.createTable();
                }
            }
        } catch (NamingException e) {
            LOGGER.error(e);
        } catch (SQLException e) {
            LOGGER.error(e);
        } catch (InstantiationException e) {
            LOGGER.error(e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
        }

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOGGER.debug("deregistering jdbc driver:" + driver);
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        }

    }
}
