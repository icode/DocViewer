package com.log4ic.utils.io.scanner.filter;

import com.log4ic.utils.io.scanner.FileScanner;
import javolution.util.FastList;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-28
 * @time: 上午12:59
 */
public class ClassNameScannerFilter implements IScannerFilter {

    List<String> classFilters = new FastList<String>();

    public ClassNameScannerFilter(List<String> classFilters) {
        this.classFilters = classFilters;
    }

    /**
     * 根据过滤规则判断类名
     *
     * @param url
     * @return
     */
    public boolean filter(String urlStr, URL url, ClassLoader classLoader) {
        String fileName = url.getFile();
        if (!fileName.endsWith(".class")) {
            return false;
        }
        if (null == this.classFilters || this.classFilters.isEmpty()) {
            return true;
        }
        String tmpName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length() - 6);
        boolean flag = false;
        for (String str : classFilters) {
            String tmpreg = "^" + str.replace("*", ".*") + "$";
            Pattern p = Pattern.compile(tmpreg);
            if (p.matcher(tmpName).find()) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
