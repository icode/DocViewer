package com.log4ic.support;

import com.log4ic.DocViewer;
import com.log4ic.utils.convert.office.OfficeConverter;
import com.log4ic.utils.convert.pdf.PDFConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-9-17 下午2:09
 */
public class DocViewerServiceListener implements ServletContextListener {

    private static final Log LOGGER = LogFactory.getLog(DocViewerServiceListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            OfficeConverter.startService();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        try {
            PDFConverter.loadConfig();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        try {
            DocViewer.loadConfig();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            OfficeConverter.stopService();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
