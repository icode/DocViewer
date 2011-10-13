package com.log4ic.utils.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-25 下午12:05
 */
public class SwfFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        return pathname.getName().toString().endsWith(".swf");
    }
}
