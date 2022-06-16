package com.log4ic.utils.io.scanner;

import com.log4ic.utils.io.scanner.filter.ClassNameScannerFilter;
import com.log4ic.utils.io.scanner.filter.IScannerFilter;
import com.log4ic.utils.support.scanner.filter.AnnotationFilter;
import javolution.util.FastList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-27
 * @time: 下午4:46
 */
public class FileScanner {

    /**
     *  LOGGER 
     */
    private static final Log LOGGER = LogFactory.getLog(FileScanner.class);

    /**
     * 过滤规则适用情况 true—>搜索符合规则的 false->排除符合规则的
     */
    private boolean checkInOrEx = true;

    private IScannerFilter scannerFilter = null;

    private List<URL> fileList = new FastList<URL>();

    /**
     * 无参构造器，默认是排除内部类、并搜索符合规则
     */
    public FileScanner() {
        this.scannerFilter = new ClassNameScannerFilter(null);
    }

    /**
     * checkInOrEx：过滤规则适用情况 true—>搜索符合规则的 false->排除符合规则的<br>
     * classFilters：自定义过滤规则，如果是null或者空，即全部符合不过滤
     *
     * @param checkInOrEx
     * @param classFilters
     */
    public FileScanner(Boolean checkInOrEx,
                       List<String> classFilters) {
        this.checkInOrEx = checkInOrEx;
        this.scannerFilter = new ClassNameScannerFilter(classFilters);

    }

    public FileScanner(Boolean checkInOrEx,
                       IScannerFilter filter) {
        this.checkInOrEx = checkInOrEx;
        this.scannerFilter = filter;
    }

    public FileScanner(AnnotationFilter filter) {
        this.scannerFilter = filter;
    }

    /**
     * 扫描包
     *
     * @param basePackage 基础包
     * @param recursive   是否递归搜索子包
     * @return Set
     */
    public List<URL> find(ClassLoader classLoader, String basePackage, boolean recursive) {

        if (basePackage.endsWith(".")) {
            basePackage = basePackage
                    .substring(0, basePackage.lastIndexOf('.'));
        }
        String package2Path = basePackage.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = classLoader.getResources(package2Path);
            if (!dirs.hasMoreElements()) {
                return this.fileList;
            }
            this.fileList.clear();
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                     LOGGER .info("扫描file类型的文件:[" + url + "]");
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    doScanFileByDir(basePackage, filePath, recursive, classLoader);
                } else if ("jar".equals(protocol)) {
                     LOGGER .info("扫描jar文件中的类:[" + url + "]");
                    doScanFileByJar(basePackage, url, recursive, classLoader);
                }
            }
        } catch (IOException e) {
             LOGGER .error("IOException error:", e);
        }

        return fileList;
    }

    public List<URL> find(String basePackage, boolean recursive) {
        return this.find(Thread.currentThread().getContextClassLoader(), basePackage, recursive);
    }

    public List<URL> find(ClassLoader classLoader, boolean recursive) {
        return this.find(classLoader, "", recursive);
    }

    public List<URL> find(ClassLoader classLoader, String basePackage) {
        return this.find(classLoader, basePackage, true);
    }

    public List<URL> find(ClassLoader classLoader) {
        return this.find(classLoader, true);
    }

    public List<URL> find(boolean recursive) {
        return this.find("", recursive);
    }

    public List<URL> find(String basePackage) {
        return this.find(basePackage, true);
    }

    public List<URL> find() {
        return this.find(true);
    }

    /**
     * 以jar的方式扫描包下的所有文件
     *
     * @param basePackage
     * @param url
     * @param recursive
     */
    private void doScanFileByJar(String basePackage, URL url, final boolean recursive, ClassLoader classLoader) {
        String packageName = basePackage;
        String package2Path = packageName.replace('.', '/');
        JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue;
                }

                // 判断是否递归搜索子包
                if (!recursive && name.lastIndexOf('/') != package2Path.length()) {
                    continue;
                }

                URL fileUrl = new URL(url + name.substring(name.lastIndexOf("/")));

                // 判定是否符合过滤条件
                boolean filter = scannerFilter.filter(name, fileUrl, classLoader);
                if (checkInOrEx ? filter : !filter) {
                    this.fileList.add(fileUrl);
                }
            }
        } catch (IOException e) {
             LOGGER .error("IOException error:", e);
        }
    }

    /**
     * 以文件目录的方式扫描包下的所有Class文件
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     */
    private void doScanFileByDir(final String packageName, String packagePath, boolean recursive, final ClassLoader classLoader) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        final boolean fileRecursive = recursive;
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义文件过滤规则
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return fileRecursive;
                }
                boolean filter = false;
                try {
                    filter = scannerFilter.filter(packageName + "." + file.getName(), file.toURI().toURL(), classLoader);
                } catch (MalformedURLException e) {
                     LOGGER .error(e);
                }
                return checkInOrEx ? filter : !filter;
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                doScanFileByDir(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classLoader);
            } else {
                try {
                    this.fileList.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                     LOGGER .error(e);
                }

            }
        }
    }


    public boolean isCheckInOrEx() {
        return checkInOrEx;
    }


    public void setCheckInOrEx(boolean pCheckInOrEx) {
        checkInOrEx = pCheckInOrEx;
    }

    public IScannerFilter getScannerFilter() {
        return scannerFilter;
    }

    public void setScannerFilter(IScannerFilter scannerFilter) {
        this.scannerFilter = scannerFilter;
    }

    public static void main(String[] args) {
        FileScanner fileScanner = new FileScanner();
        List<URL> list = fileScanner.find("com.log4ic.entity");
        for (URL url : list) {
            System.out.println(url.getPath());
        }
    }
}
