package com.log4ic.utils.convert.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.log4ic.utils.FileUtils;
import com.log4ic.utils.security.PDFSecurer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author: 张立鑫
 * @date: 11-8-15 下午4:21
 * PDF 转换器
 */
public class PDFConverter {
    private static final Log LOGGER = LogFactory.getLog(PDFConverter.class);

    private static final String CONFIG_FILE = "config" + File.separator + "pdf-convert.properties";

    private static String COMMAND = "";

    private static Integer SINGLE_PAGE_MODE_MAX_THREAD = 5;

    private static Properties properties;

    private static void setCommand(String command) {
        PDFConverter.COMMAND = command;
        LOGGER.debug("设置命令(command)配置为 :" + command);
    }

    public static String getCommand() {
        return COMMAND;
    }

    private static void setSinglePageModeMaxThread(Integer value) {
        PDFConverter.SINGLE_PAGE_MODE_MAX_THREAD = value;
        LOGGER.debug("设置单页面转换模式最大线程(single_page_mode_max_thread)配置为 :" + value);
    }

    public static Integer getOnePageModeMaxThread() {
        return SINGLE_PAGE_MODE_MAX_THREAD;
    }

    public static void setOnePageModeMaxThreadConfig(Integer value) throws Exception {
        setConfig("single_page_mode_max_thread", value.toString());
        setSinglePageModeMaxThread(value);
    }

    public static void setCommandConfig(String command) throws Exception {
        setConfig("command", command);
        setCommand(command);
    }


    public static void setConfig(String cfgName, String cfgValue) throws Exception {
        Properties properties = getProperties();
        properties.setProperty(cfgName, cfgValue);
    }

    public static Properties getProperties() throws Exception {
        if (properties == null) {
            properties = new Properties();
            //获取class文件夹
            ClassLoader loader = PDFConverter.class.getClassLoader();
            //加载文件
            InputStream is = loader.getResourceAsStream(CONFIG_FILE);
            if (is == null) {
                throw new Exception("properties is not found");
            }
            //读取
            properties.load(is);
        }
        return properties;
    }

    /**
     * 读取配置文件
     */
    public static void loadConfig() throws Exception {

        LOGGER.debug("初始PDF转换器化配置....");

        Properties properties = getProperties();

        setCommand(new String(properties.getProperty("command", COMMAND).getBytes("ISO-8859-1"), "UTF-8"));

        String max = properties.getProperty("single_page_mode_max_thread", SINGLE_PAGE_MODE_MAX_THREAD + "");
        try {
            setSinglePageModeMaxThread(Integer.parseInt(max));
        } catch (Exception e) {
            throw new Exception("single_page_mode_max_thread config error");
        }

        LOGGER.debug("初始化PDF转换器配置完毕!");
    }

    public int execConvertCommand(String command, final List<String> outResources) throws InterruptedException, IOException {
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
                        if (line != null) {
                            if (outResources != null) {
                                outResources.add(line);
                            }
                            LOGGER.debug(line);
                        }
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
                        if (line != null) {
                            LOGGER.debug(line);
                            if (outResources != null) {
                                outResources.add(line);
                            }
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

        PdfReader reader = new PdfReader(pdfPath);
        int pageCount = reader.getNumberOfPages();

        // if pdf is a encrypted file unencrypted
        if (reader.isEncrypted()) {
            LOGGER.debug("encrypted pdf! 准备另存");
            pdfPath = PDFSecurer.decrypt(reader, FileUtils.getFilePrefix(pdfFile) + "_decrypted.pdf").getPath();
            LOGGER.debug("PDF解密完成");
        } else {
            LOGGER.debug("---文档未加密---");
        }

        reader.close();

        return new PDFConverterDeploy(outputDirectory, pageCount, COMMAND.replace("${in}", pdfPath)
                .replace("${out}", outPath + (splitPage ? "page%.swf" : "page.swf")) +
                //是否将图片转换成点阵形式
                (poly2bitmap ? " -s poly2bitmap" : ""));

    }

    public PDFConvertError checkError(List<String> outResources) {
        boolean errorMsg = false;
        int position = -1;
        String item;
        PDFConvertError error = new PDFConvertError();
        for (int i = outResources.size() - 1; i >= 0; i--) {

            item = outResources.get(i);

            if (errorMsg == false) {

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

    private void errorHandler(List<String> outResources, final File pdfFile, final String outPath, boolean splitPage, boolean poly2bitmap) throws Exception {
        // 如果第一次没有将图片转换成点阵图报错，则进行第二次转换，并将图片转换成点阵
        final PDFConvertError error = checkError(outResources);
        if (error.getType() == PDFConvertErrorType.COMPLEX && poly2bitmap != true) {
            LOGGER.debug("---第一次转换失败，进行第二次转换---");
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
                convert(pdfFile, outPath, true);
            }
        } else {
            throw new Exception("Conversion failed:" + error.getType() + ";\n" + StringUtils.join(outResources, "\n"));
        }
    }

    public void run(String command, File pdfFile, String outPath, boolean splitPage, boolean poly2bitmap) throws Exception {
        LOGGER.debug("---转换开始---");
        List<String> outResources = new ArrayList<String>();

        if (execConvertCommand(command, outResources) != 0) {
            errorHandler(outResources, pdfFile, outPath, splitPage, poly2bitmap);
        }
        LOGGER.debug("---转换结束---");
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
            final int nowPage = i;
            final String commandExec = command += " -p " + nowPage;
            Thread t = new Thread() {
                public void run() {
                    List<String> outResources = new ArrayList<String>();
                    try {
                        if (execConvertCommand(commandExec, outResources) != 0) {
                            PDFConvertError error = checkError(outResources);
                            if (error.getType() == PDFConvertErrorType.COMPLEX && poly2bitmap != true) {
                                LOGGER.debug("---第一次转换失败，进行第二次转换---");
                                execConvertCommand(commandExec + " -s poly2bitmap", outResources);
                            } else if (error.getType() != PDFConvertErrorType.NONE) {
                                throw new Exception("Conversion failed:" + error.getType() + ";\n" + StringUtils.join(outResources, "\n"));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(e);
                        e.printStackTrace();
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
