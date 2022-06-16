package com.log4ic.utils.io.scanner.filter;

import com.log4ic.utils.io.scanner.FileScanner;

import java.net.URL;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-28
 * @time: 上午12:54
 */
public interface IScannerFilter {
    boolean filter(String urlStr, URL url, ClassLoader classLoader);
}
