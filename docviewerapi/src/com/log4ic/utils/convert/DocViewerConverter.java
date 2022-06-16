package com.log4ic.utils.convert;

import com.log4ic.DocViewer;
import com.log4ic.utils.convert.office.OfficeConverter;
import com.log4ic.utils.convert.pdf.PDFConverter;
import com.log4ic.utils.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午4:38
 */
public class DocViewerConverter {
    private static OfficeConverter officeConverter;
    private static PDFConverter pdfConverter;
    private static final Object lock = new Object();
    private static final Log logger = LogFactory.getLog(DocViewerConverter.class);

    private static LinkedList<File> runningQueue = new LinkedList<File>();
    private static final Lock queueLock = new ReentrantLock();

    public static File deploy(File file, String outPath) {

        String fileName = FileUtils.getFilePrefix(file);

        File dir = new File(FileUtils.appendFileSeparator(outPath) + fileName);

        if (dir.isFile()) {
            dir.renameTo(new File(dir.getPath() + ".backup"));
        } else {
            dir.mkdirs();
        }

        return dir;
    }

    public static Lock getRunningQueueLock() {
        return queueLock;
    }

    public static LinkedList<File> getRunningQueue() {
        return runningQueue;
    }

    /**
     * 转换为swf
     *
     * @param file
     * @param outPath
     * @return 返回转换后输出文件目录
     * @throws Exception
     */
    public static File toSwf(File file, String outPath) throws Exception {
        File pdf = file;
        if (pdfConverter == null) {
            queueLock.lock();
            if (pdfConverter == null) {
                //PDFConverter.loadConfig();
                pdfConverter = new PDFConverter();
            }
            queueLock.unlock();
        }
        queueLock.lock();
        runningQueue.add(file);
        queueLock.unlock();
        try {
            logger.debug("toSwf after add size:" + runningQueue.size() + ".[" + file.getName() + "]");
            String suffix = FileUtils.getFileSuffix(file);
            if (suffix == null) {
                throw new Exception("The file not has a suffix!");
            }
            if (!suffix.toLowerCase().equals("pdf")) {
                pdf = toPDF(file, outPath);
            }

            return pdfConverter.convert(pdf, outPath, DocViewer.isSplitPage(), false);
        } finally {
            queueLock.lock();
            boolean isok = runningQueue.remove(file);

            logger.debug("toSwf after remove size:" + runningQueue.size() + ". isok:" + isok);
            queueLock.unlock();
        }
    }

    public static File toPDF(File file, String outPath) throws Exception {
        if (officeConverter == null) {
            queueLock.lock();
            if (officeConverter == null) {
                officeConverter = new OfficeConverter();
            }
            queueLock.unlock();
        }
        queueLock.lock();
        runningQueue.add(file);
        queueLock.unlock();
        try {
            logger.debug("toPDF after add size:" + runningQueue.size() + ".[" + file.getName() + "]");
            File pdf = null;

            File dir = deploy(file, outPath);
            pdf = new File(dir.getPath() + File.separator + FileUtils.getFilePrefix(file) + ".pdf");
            if (!pdf.exists()) {
                pdf = officeConverter.toPDF(file, dir.getPath());
            }

            return pdf;
        } finally {
            queueLock.lock();
            boolean isok = runningQueue.remove(file);
            logger.debug("toPDF after remove size:" + runningQueue.size() + ". isok:" + isok);
            queueLock.unlock();
        }
    }
}
