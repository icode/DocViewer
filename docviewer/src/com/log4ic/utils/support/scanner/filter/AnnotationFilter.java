package com.log4ic.utils.support.scanner.filter;

import com.log4ic.utils.io.scanner.FileScanner;
import com.log4ic.utils.io.scanner.filter.IScannerFilter;
import javolution.util.FastList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-28
 * @time: 上午3:26
 */
public class AnnotationFilter implements IScannerFilter {

    private static final Log LOGGER = LogFactory.getLog(AnnotationFilter.class);

    private List<Class> classList = new FastList<Class>();

    public boolean filter(String urlStr, URL url, ClassLoader classLoader) {
        String classPathStr = urlStr.replace("/", ".");
        classPathStr = classPathStr.substring(0, classPathStr.length() - 6);
        try {
            Class clazz = classLoader.loadClass(classPathStr);
            if (clazz.isAnnotationPresent(Entity.class)) {
                classList.add(clazz);
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return false;
    }

    public List<Class> getClassList() {
        return classList;
    }

    public static void main(String[] args) {
        FileScanner fileScanner = new FileScanner(new AnnotationFilter());
        List<URL> list = fileScanner.find("com.log4ic.entity");
        for (URL url : list) {
            System.out.println(url.getPath());
        }
    }
}
