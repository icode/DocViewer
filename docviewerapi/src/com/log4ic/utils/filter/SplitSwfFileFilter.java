package com.log4ic.utils.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-25 下午12:05
 */
public class SplitSwfFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        Pattern pattern = Pattern.compile("^page\\d+\\.swf$");
        Matcher matcher = pattern.matcher(pathname.getName().toString());
        return matcher.matches();
    }
}
