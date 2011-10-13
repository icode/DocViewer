package com.log4ic.utils.convert.pdf;

import java.io.File;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午6:14
 */
public class PDFConverterDeploy {
    private File outputDirectory;
    private int pageCount;
    private String command;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getCommand() {
        return command;
    }

    public PDFConverterDeploy(File outputDirectory, int pageCount, String command) {
        this.outputDirectory = outputDirectory;
        this.pageCount = pageCount;
        this.command = command;
    }
}
