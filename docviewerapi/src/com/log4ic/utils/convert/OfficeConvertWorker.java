package com.log4ic.utils.convert;

import com.log4ic.DocViewer;
import com.log4ic.utils.convert.office.OfficeConverter;

import java.io.File;
import java.io.IOException;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-22 上午11:20
 */
public class OfficeConvertWorker extends ConvertWorker {

    boolean splitePage = true;

    public OfficeConvertWorker(int id, boolean splitPage) {
        super(id);
        this.splitePage = splitPage;
    }

    public OfficeConvertWorker(int id, File inFile, String outputPath, boolean splitPage) {
        super(id, inFile, outputPath);
        this.splitePage = splitPage;
    }

    public OfficeConvertWorker(int id, File inFile, boolean splitPage) {
        super(id, inFile);
        this.splitePage = splitPage;
    }

    @Override
    public void run() {
        File in = this.getInFile();
        String out = this.getOutputPath();
        File dir = DocViewerConverter.deploy(in, out);
        OfficeConverter converter = new OfficeConverter();
        try {
            in = converter.toPDF(in, dir.getPath());
            DocViewer.addConvertWorker(new PDFConvertWorker(this.getId(), in, out, splitePage));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
