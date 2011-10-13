package com.log4ic.utils.convert;

import com.log4ic.utils.convert.pdf.PDFConverter;

import java.io.File;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-22 上午11:20
 */
public class PDFConvertWorker extends ConvertWorker {

    boolean splitPage = true;

    public PDFConvertWorker(int id, boolean splitPage) {
        super(id);
        this.splitPage = splitPage;
    }

    public PDFConvertWorker(int id, File inFile, String outputPath, boolean splitPage) {
        super(id, inFile, outputPath);
        this.splitPage = splitPage;
    }

    public PDFConvertWorker(int id, File inFile, boolean splitPage) {
        super(id, inFile);
        this.splitPage = splitPage;
    }

    @Override
    public void run() {
        File in = this.getInFile();
        PDFConverter converter = new PDFConverter();
        try {
            converter.convert(in, this.getOutputPath(), splitPage, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
