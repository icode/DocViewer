package com.log4ic.utils.convert;

import com.log4ic.utils.thread.IWorker;

import java.io.File;
import java.io.Serializable;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-22 上午11:20
 */
public abstract class ConvertWorker implements IWorker, Serializable {

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public void setInFile(File inFile) {
        this.inFile = inFile;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {

        return outputPath;
    }

    public File getInFile() {

        return inFile;
    }


    private File inFile;

    private String outputPath;

    public ConvertWorker(int id) {
        this.id = id;
    }

    public ConvertWorker(int id, File inFile, String outputPath) {
        this.id = id;
        this.inFile = inFile;
        this.outputPath = outputPath;
    }

    public ConvertWorker(int id, File inFile) {
        this.id = id;
        this.inFile = inFile;

    }

    public int getId() {
        return this.id;
    }


    public abstract void run();
}
