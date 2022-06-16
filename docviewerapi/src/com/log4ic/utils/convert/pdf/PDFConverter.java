package com.log4ic.utils.convert.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.log4ic.utils.io.FileUtils;
import com.log4ic.utils.security.PDFSecurer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: 张立鑫
 * @date: 11-8-15 下午4:21
 * PDF 转换器
 */
public class PDFConverter {
    private static final Log LOGGER = LogFactory.getLog(PDFConverter.class);

    private static String COMMAND = "";

    private static Integer SINGLE_PAGE_MODE_MAX_THREAD = 5;

    public static void setCommand(String command) {
        PDFConverter.COMMAND = command;
        LOGGER.debug("设置命令配置为 :" + command);
    }

    public static String getCommand() {
        return COMMAND;
    }

    public static void setSinglePageModeMaxThread(Integer value) {
        PDFConverter.SINGLE_PAGE_MODE_MAX_THREAD = value;
        LOGGER.debug("设置单页面转换模式最大线程配置为 :" + value);
    }

    public static Integer getSinglePageModeMaxThread() {
        return SINGLE_PAGE_MODE_MAX_THREAD;
    }

    public static void setOnePageModeMaxThreadConfig(Integer value) throws Exception {
        setSinglePageModeMaxThread(value);
    }

    public static void setCommandConfig(String command) throws Exception {
        setCommand(command);
    }

    public int execCommand(String command, final List<String> outResources) throws InterruptedException, IOException {
        LOGGER.debug(command);
        final Process convertProcess = Runtime.getRuntime().exec(command);
        final InputStream inputStream = convertProcess.getInputStream();
        final InputStream errorStream = convertProcess.getErrorStream();

        new Thread() {
            public void run() {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(inputStream));
                try {
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        if (outResources != null) {
                            outResources.add(line);
                        }
                        LOGGER.debug(line);
                    }
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }.start();
        new Thread() {
            public void run() {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(errorStream));
                try {

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        LOGGER.debug(line);
                        if (outResources != null) {
                            outResources.add(line);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }.start();
        convertProcess.waitFor();
        return convertProcess.exitValue();
    }

    private PDFConverterDeploy deploy(File pdfFile, String outPath, boolean splitPage, boolean poly2bitmap) throws Exception, DocumentException {
        if (!pdfFile.isFile() || !pdfFile.exists()) {
            throw new Exception("Not a Valid PDF File!");
        }

        String pdfPath = pdfFile.getPath();

        String pdfFullName = pdfFile.getName();

        int i = pdfFullName.toLowerCase().lastIndexOf(".pdf");
        if (i == -1) {
            throw new Exception("Not a Valid PDF File!");
        }

        String pdfName = pdfFullName.substring(0, i);

        LOGGER.debug("---加载文档---");


        File outputDirectory = new File(FileUtils.appendFileSeparator(outPath) + pdfName);


        //if is a file , backup the file
        if (outputDirectory.isFile()) {
            outputDirectory.renameTo(new File(outputDirectory.getPath() + ".backup"));
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        outPath = FileUtils.appendFileSeparator(outputDirectory.getPath());
        File decryptedPdf = new File(FileUtils.getFilePrefix(pdfFile) + "_decrypted.pdf");
        File info = new File(FileUtils.appendFileSeparator(outputDirectory.getPath()) + "info");
        int pageCount = 0;
        if (!info.exists()) {
            PdfReader reader = new PdfReader(pdfPath);
            pageCount = reader.getNumberOfPages();
            
            if(!decryptedPdf.exists()){ 
                // if pdf is a encrypted file unencrypted
                if (reader.isEncrypted()) {
                    LOGGER.debug("encrypted pdf! 准备另存");
                    pdfPath = PDFSecurer.decrypt(reader, FileUtils.getFilePrefix(pdfFile) + "_decrypted.pdf").getPath();
                    LOGGER.debug("PDF解密完成");
                } else {
                    LOGGER.debug("---文档未加密---");
                }
            }else {
                pdfPath = decryptedPdf.getPath();
            }

            reader.close();

            if (pageCount != 0) {
                OutputStream out = null;
                try {

                    info.createNewFile();

                    out = new FileOutputStream(info);

                    out.write((pageCount + "").getBytes("UTF-8"));
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                }

            }
        } else {
            pdfPath = decryptedPdf.getPath();
            
            FileInputStream in = new FileInputStream(info);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            try {
                pageCount = Integer.parseInt(reader.readLine());
            } catch (Exception e) {
            } finally {
                reader.close();
                in.close();
            }
        }

        return new PDFConverterDeploy(outputDirectory, pageCount, COMMAND.replace("${in}", pdfPath)
                .replace("${out}", outPath + (splitPage ? "page%.swf" : "page.swf")) +
                //是否将图片转换成点阵形式
                (poly2bitmap ? " -s poly2bitmap -s multiply=4" : ""));

    }

    public PDFConvertError checkError(List<String> outResources) {
        boolean errorMsg = false;
        int position = -1;
        String item;
        PDFConvertError error = new PDFConvertError();
        for (int i = outResources.size() - 1; i >= 0; i--) {

            item = outResources.get(i);

            if (!errorMsg) {

                if (item.lastIndexOf(PDFConvertErrorType.COMPLEX.getErrorMessage()) > -1) {
                    errorMsg = true;
                    error.setType(PDFConvertErrorType.COMPLEX);
                } else if (item.lastIndexOf(PDFConvertErrorType.INVALID_CHARID.getErrorMessage()) > -1) {
                    errorMsg = true;
                    error.setType(PDFConvertErrorType.INVALID_CHARID);
                }

            } else if (position == -1) {
                String reg = "NOTICE  processing PDF page ";
                position = item.lastIndexOf(reg);
                if (position != -1) {
                    item = item.replace(reg, "");
                    error.setPosition(Integer.parseInt(item.split(" ")[0]));
                }
            }
        }
        return error;
    }

    private void errorHandler(String command, final File pdfFile, final String outPath, boolean splitPage, boolean poly2bitmap, List<String> outResources) throws Exception {
        // 如果第一次没有将图片转换成点阵图报错，则进行第二次转换，并将图片转换成点阵
        final PDFConvertError error = checkError(outResources);
        if (error.getType() == PDFConvertErrorType.COMPLEX && !poly2bitmap) {
            LOGGER.debug("---第一次转换失败，进行第二次转换 poly2bitmap = true ---");
            if (splitPage) {
                new Thread() {
                    public void run() {
                        try {
                            convert(pdfFile, outPath, error.getPosition(), error.getPosition(), true, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                convertAsOnePageMode(pdfFile, outPath, error.getPosition() + 1);
            } else {
                convert(pdfFile, outPath, splitPage, true);
            }
        } else {
            throw new Exception("Conversion failed:" + error.getType() + ";\n" + StringUtils.join(outResources, "\n"));
        }
    }

    public void run(String command, File pdfFile, String outPath, boolean splitPage, boolean poly2bitmap) throws Exception {
        LOGGER.debug("---开始---");
        List<String> outResources = new ArrayList<String>();

        if (execCommand(command, outResources) != 0) {
            errorHandler(command, pdfFile, outPath, splitPage, poly2bitmap, outResources);
        }
        LOGGER.debug("---结束---");
    }

    public File convertAsOnePageMode(File pdfFile, String outPath) throws Exception {
        return convertAsOnePageMode(pdfFile, outPath, 1, null, false);
    }

    public File convertAsOnePageMode(File pdfFile, String outPath, int startPage) throws Exception {
        return convertAsOnePageMode(pdfFile, outPath, startPage, null, false);
    }

    public File convertAsOnePageMode(File pdfFile, String outPath, boolean poly2bitmap) throws Exception {
        return convertAsOnePageMode(pdfFile, outPath, 1, null, poly2bitmap);
    }

    public File convertAsOnePageMode(final File pdfFile, final String outPath, final int startPage, final Integer endPage, final boolean poly2bitmap) throws Exception {

        final List<Thread> pool = new LinkedList<Thread>();

        int covertCount = 0;

        PDFConverterDeploy deploy = deploy(pdfFile, outPath, true, poly2bitmap);

        String command = deploy.getCommand();

        int page = getSinglePageModeMaxThreadCount(deploy);

        int pageCount = deploy.getPageCount();

        for (int i = startPage; i <= (endPage == null || endPage > pageCount ? pageCount : endPage); i++) {
            final String commandExec = command += " -p " + i;
            Thread t = new Thread() {
                public void run() {
                    List<String> outResources = new ArrayList<String>();
                    try {
                        if (execCommand(commandExec, outResources) != 0) {
                            PDFConvertError error = checkError(outResources);
                            if (error.getType() == PDFConvertErrorType.COMPLEX && !poly2bitmap) {
                                LOGGER.debug("---第一次转换失败，进行第二次转换---");
                                execCommand(commandExec + " -s poly2bitmap", outResources);
                            } else if (error.getType() != PDFConvertErrorType.NONE) {
                                throw new Exception("Conversion failed:" + error.getType() + ";\n" + StringUtils.join(outResources, "\n"));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(e);
                    }

                }
            };
            pool.add(t);
            t.start();
            covertCount++;
            if (covertCount == page) {
                for (Thread thread : pool) {
                    thread.join();
                }
                pool.removeAll(pool);
                covertCount = 0;
            }
        }
        return deploy.getOutputDirectory();
    }

    public File convert(File pdfFile, String outPath, int startPage, Integer endPage, boolean splitPage, boolean poly2bitmap) throws Exception {
        PDFConverterDeploy deploy = deploy(pdfFile, outPath, splitPage, poly2bitmap);
        String command = deploy.getCommand();
        int pageCount = deploy.getPageCount();
        command += " -p " + startPage + (endPage != null && startPage == endPage ? "" : "-" + (endPage == null || endPage > pageCount ? pageCount : endPage));

        run(command, pdfFile, outPath, splitPage, poly2bitmap);
        return deploy.getOutputDirectory();
    }

    public File convert(File pdfFile, String outPath) throws Exception {
        return convert(pdfFile, outPath, true, false);
    }

    public File convert(File pdfFile, String outPath, boolean poly2bitmap) throws Exception {
        return convert(pdfFile, outPath, true, poly2bitmap);
    }

    /**
     * pdf转换为swf
     *
     * @param pdfFile
     * @param outPath
     * @param splitPage
     * @param poly2bitmap
     * @return 返回转换后输出文件目录
     * @throws Exception
     */
    public File convert(File pdfFile, String outPath, boolean splitPage, boolean poly2bitmap) throws Exception {

        LOGGER.debug("---准备转换文档---");

        PDFConverterDeploy deploy = deploy(pdfFile, outPath, splitPage, poly2bitmap);
        String command = deploy.getCommand();
        run(command, pdfFile, outPath, splitPage, poly2bitmap);

        return deploy.getOutputDirectory();
    }


    public int getSinglePageModeMaxThreadCount(PDFConverterDeploy deploy) {
        int pageCount = deploy.getPageCount();
        if (SINGLE_PAGE_MODE_MAX_THREAD < 1) {
            return pageCount;
        } else {
            return SINGLE_PAGE_MODE_MAX_THREAD > pageCount ? pageCount : SINGLE_PAGE_MODE_MAX_THREAD;
        }
    }

}
