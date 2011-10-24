package com.log4ic;

import com.log4ic.entity.IDocAttachment;
import com.log4ic.services.IAttachmentService;
import com.log4ic.utils.FileUtils;
import com.log4ic.utils.convert.*;
import com.log4ic.utils.convert.office.OfficeConverter;
import com.log4ic.utils.convert.pdf.PDFConverter;
import com.log4ic.utils.filter.SwfFileFilter;
import com.log4ic.utils.security.XXTEA;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.office.OfficeConnectionProtocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

/**
 * @author: 张立鑫
 * @date: 11-8-18 上午9:57
 */
public class DocViewer {
    private static final Log LOGGER = LogFactory.getLog(DocViewer.class);

    private static int workerId = 0;

    private static final String CONFIG_FILE = "conf" + File.separator + "docviewer.properties";
    private static String OUTPUT_PATH = "";
    private static boolean SPLIT_PAGE = true;
    private static int PDF_POOL_MAX_THREAD = 5;
    private static int OFFICE_POOL_MAX_THREAD = 5;
    private static boolean ENCRYPTION = true;
    private static boolean DYNAMIC_KEY = false;
    private static String SECRET_KEY = "";
    private static int KEY_LENGTH = 40;
    private static String ATTACHMENT_SERVICE;

    private static IAttachmentService attachmentService = null;

    public static void setPDFPoolMaxThread(int count) throws Exception {
        PDF_POOL_MAX_THREAD = count;
        LOGGER.debug("设置PDF转换最大线程(pdf_pool_max_thread)为:" + count);
    }

    public static void setOfficePoolMaxThread(int count) throws Exception {
        OFFICE_POOL_MAX_THREAD = count;
        LOGGER.debug("设置office转换最大线程为:" + count);
    }

    public static void setOutputPath(String path) throws Exception {
        OUTPUT_PATH = FileUtils.appendFileSeparator(path);
        LOGGER.debug("设置转换输出目录为为:" + path);
    }

    public static void setDynamicKey(boolean dynamicKey) throws Exception {
        DYNAMIC_KEY = dynamicKey;
        LOGGER.debug("设置是否为动态加密转换后的SWF文档为:" + dynamicKey);
    }

    public static void setSecretKey(String secretKey) throws Exception {
        SECRET_KEY = secretKey;
        LOGGER.debug("设置静态密钥为:" + secretKey);
    }

    public static void setEncryption(boolean encryption) throws Exception {
        ENCRYPTION = encryption;
        LOGGER.debug("设置是否加密转换后的SWF文档为:" + encryption);
    }

    public static void setKeyLength(int length) throws Exception {
        KEY_LENGTH = length;
        LOGGER.debug("设置密钥长度为:" + length);
    }

    public static void setSplitPage(boolean split) throws Exception {
        SPLIT_PAGE = split;
        LOGGER.debug("设置是否分页为:" + split);
    }

    public static int getPDFPoolMaxThread() {
        return PDF_POOL_MAX_THREAD;
    }

    public static int getOfficePoolMaxThread() {
        return OFFICE_POOL_MAX_THREAD;
    }

    public static String getOutputPath() {
        return OUTPUT_PATH;
    }

    public static boolean isDynamicKey() {
        return DYNAMIC_KEY;
    }

    public static String getSecretKey() {
        return SECRET_KEY;
    }

    public static boolean isEncryption() {
        return ENCRYPTION;
    }

    public static int getKeyLength() {
        return KEY_LENGTH;
    }

    public static boolean isSplitPage() {
        return SPLIT_PAGE;
    }

    public static String getCurrentSecretKey() {
        return isDynamicKey() ? getRandomKey(KEY_LENGTH) : SECRET_KEY;
    }


    public static IAttachmentService getAttachmentService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (attachmentService == null) {
            Class serviceClass = Class.forName(ATTACHMENT_SERVICE);
            attachmentService = (IAttachmentService) serviceClass.newInstance();
        }
        return attachmentService;
    }

    public static void setAttachmentService(String service) {
        ATTACHMENT_SERVICE = service;
        LOGGER.debug("设置附件服务类(attachment_service)为:" + service);
    }



    public static boolean isSupport(String fileExtends) {
        return OfficeConverter.isSupport(fileExtends);
    }


    DocViewer() {
    }

    public static void initialize() throws Exception {
        Properties properties = FileUtils.getProperties(CONFIG_FILE);
        String baseConfigSpace = "docviewer.";
        String baseConverterConfigSpace = baseConfigSpace + "converter.";
        String basePdfConfigSpace = baseConverterConfigSpace + "pdf.";
        String baseOfficeConfigSpace = baseConverterConfigSpace + "office.";

        LOGGER.debug("初始化office转换服务配置....");
        OfficeConverter.setOfficeHome(new String(properties.getProperty(baseOfficeConfigSpace + "home", OfficeConverter.getOfficeHome()).getBytes("ISO-8859-1"), "UTF-8"));
        //OfficeConverter.setHost(new String(properties.getProperty(baseOfficeConfigSpace + "host", OfficeConverter.getHost()).getBytes("ISO-8859-1"), "UTF-8"));
        OfficeConverter.setPort(Integer.parseInt(properties.getProperty(baseOfficeConfigSpace + "port", OfficeConverter.getPort() + "")));
        String protocol = properties.getProperty(baseOfficeConfigSpace + "protocol");
        if (StringUtils.isNotBlank(protocol)) {
            protocol = protocol.toLowerCase();
            if (protocol.equals("socket")) {
                OfficeConverter.setConnectionProtocol(OfficeConnectionProtocol.SOCKET);
            } else if (protocol.equals("pipe")) {
                OfficeConverter.setConnectionProtocol(OfficeConnectionProtocol.PIPE);
            }
        }
        String profile = properties.getProperty(baseOfficeConfigSpace + "profile");
        if (StringUtils.isNotBlank(profile)) {
            OfficeConverter.setTemplateProfileDir(new File(profile));
        }
        LOGGER.debug("初始化服务配置完毕!启动服务....");

        OfficeConverter.startService();

        LOGGER.debug("初始PDF转换器化配置....");

        PDFConverter.setCommand(new String(properties.getProperty(basePdfConfigSpace + "command", PDFConverter.getCommand()).getBytes("ISO-8859-1"), "UTF-8"));

        String max = properties.getProperty(basePdfConfigSpace + "mode.singlePage.maxThread", PDFConverter.getSinglePageModeMaxThread() + "");
        try {
            PDFConverter.setSinglePageModeMaxThread(Integer.parseInt(max));
        } catch (Exception e) {
            throw new Exception(basePdfConfigSpace + "mode.singlePage.maxThread" + " config error");
        }

        LOGGER.debug("初始化PDF转换器配置完毕!");

        LOGGER.debug("初始化文档阅读器主程序配置....");

        setOutputPath(new String(properties.getProperty(baseConfigSpace + "output", OUTPUT_PATH).getBytes("ISO-8859-1"), "UTF-8"));

        String pdfPoolMaxThread = properties.getProperty(basePdfConfigSpace + "maxThread", PDF_POOL_MAX_THREAD + "");

        try {
            setPDFPoolMaxThread(Integer.parseInt(pdfPoolMaxThread));
        } catch (Exception e) {
            throw new Exception("pdf_pool_max_thread config error");
        }

        String officePoolMaxThread = properties.getProperty(baseOfficeConfigSpace + "maxThread", OFFICE_POOL_MAX_THREAD + "");
        try {
            setOfficePoolMaxThread(Integer.parseInt(officePoolMaxThread));
        } catch (Exception e) {
            throw new Exception("office_pool_max_thread config error");
        }
        try {
            setEncryption(Boolean.parseBoolean(properties.getProperty(baseConfigSpace + "encryption", ENCRYPTION + "")));
        } catch (Exception e) {
            throw new Exception("encryption config error");
        }

        try {
            setSecretKey(properties.getProperty(baseConfigSpace + "encryption.key", ""));
        } catch (Exception e) {
            throw new Exception("secretKey config error");
        }

        try {
            setDynamicKey(Boolean.parseBoolean(properties.getProperty(baseConfigSpace + "encryption.dynamic", DYNAMIC_KEY + "")));
        } catch (Exception e) {
            throw new Exception("dynamicKey config error");
        }
        try {
            setKeyLength(Integer.parseInt(properties.getProperty(baseConfigSpace + "encryption.dynamic.key.length", KEY_LENGTH + "")));
        } catch (Exception e) {
            throw new Exception("keyLength config error");
        }
        try {
            setAttachmentService(properties.getProperty(baseConfigSpace + "attachment.service"));
        } catch (Exception e) {
            throw new Exception("attachment_service config error");
        }

        LOGGER.debug("初始化文档阅读器主程序配置完毕!");
    }

    public static void destroy() throws Exception {
        OfficeConverter.stopService();
    }

    private synchronized static void checkWorker(ConvertWorker worker) throws Exception {
        worker.setOutputPath(OUTPUT_PATH);
        if (worker.getInFile() == null) {
            throw new Exception("input file is null");
        }
    }

    public synchronized static void addConvertWorker(PDFConvertWorker worker) throws Exception {
        checkWorker(worker);
        synchronized (pdfQueue) {
            pdfQueue.addWorker(worker);
        }
    }

    public synchronized static void addConvertWorker(OfficeConvertWorker worker) throws Exception {
        checkWorker(worker);
        synchronized (officeQueue) {
            officeQueue.addWorker(worker);
        }
    }

    public synchronized static void addConvertWorker(int id) throws Exception {

        File in = getDocFileFromSource(id);

        if (in.getName().toLowerCase().endsWith(".pdf")) {
            addConvertWorker(new PDFConvertWorker(id, in, OUTPUT_PATH, isSplitPage()));
        } else {
            addConvertWorker(new OfficeConvertWorker(id, in, OUTPUT_PATH, isSplitPage()));
        }
    }

    public synchronized static int getNextId() {
        return ++workerId;
    }

    private static final ConvertQueue officeQueue = new ConvertQueue(OFFICE_POOL_MAX_THREAD, "office_queue");
    private static final ConvertQueue pdfQueue = new ConvertQueue(PDF_POOL_MAX_THREAD, "pdf_queue");


    public static File getDoc(int id) throws Exception {
        if (DocViewer.hasDocDir(id) || DocViewer.isConverting(id)) {
            while (DocViewer.isConverting(id)) {
                Thread.sleep(200);
            }
            return new File(OUTPUT_PATH + id);
        }
        return null;
    }

    public synchronized static boolean isConverting(int id) {

        LinkedList<File> fileList = DocViewerConverter.getRunningQueue();

        synchronized (fileList) {
            for (File f : fileList) {
                if (FileUtils.getFilePrefix(f).equals(id + "")) {
                    return true;
                }
            }
        }

        return officeQueue.isRunning(id) || pdfQueue.isRunning(id);
    }

    public static File getPDFDoc(int id) throws Exception {
        if (DocViewer.hasDocDir(id)) {
            while (DocViewer.isConverting(id)) {
                Thread.sleep(200);
            }
            File file = new File(OUTPUT_PATH + id + File.separator + id + ".pdf");
            if (file.exists()) {
                return file;
            }
        }
        ConvertWorker worker = null;
        if (pdfQueue.isWaiting(id)) {
            worker = (ConvertWorker) pdfQueue.getWaitingWorker(id);
            if (worker != null) {
                pdfQueue.removeWaitingWorker(worker);
            }

        }

        File in = null;

        if (worker != null) {
            in = worker.getInFile();
        } else {
            in = getDocFileFromSource(id);
        }

        return DocViewerConverter.toPDF(in, OUTPUT_PATH);
    }


    public static int getDocPageCount(int id) throws Exception {
        if (DocViewer.hasDocDir(id)) {
            while (DocViewer.isConverting(id)) {
                Thread.sleep(200);
            }
            int count = new File(OUTPUT_PATH + id).listFiles(new SwfFileFilter()).length;
            if (count != 0) {
                return count;
            }
        }
        ConvertWorker worker = null;
        if (pdfQueue.isWaiting(id)) {
            worker = (ConvertWorker) pdfQueue.getWaitingWorker(id);
            if (worker != null) {
                pdfQueue.removeWaitingWorker(worker);
            }

        } else if (officeQueue.isWaiting(id)) {
            worker = (ConvertWorker) officeQueue.getWaitingWorker(id);
            if (worker != null) {
                officeQueue.removeWaitingWorker(worker);
            }
        }

        File in = null;

        if (worker != null) {
            in = worker.getInFile();
        } else {
            in = getDocFileFromSource(id);
        }

        return DocViewerConverter.toSwf(in, OUTPUT_PATH).listFiles(new SwfFileFilter()).length;
    }

    public static File getDocFileFromSource(int id) throws Exception {
        IDocAttachment attachment = attachmentService.getDocAttachmentById(id);

        if (attachment == null) {
            throw new Exception("Document is not exists!");
        }

        File docFile = new File(OUTPUT_PATH + id + File.separator + id + "." + attachment.getFileType());

        if (docFile.exists() && docFile.isFile()) {
            return docFile;
        } else if (!docFile.getParentFile().exists()) {
            docFile.getParentFile().mkdirs();
        }

        if (!docFile.exists()) {
            docFile.createNewFile();
        }


        InputStream in = attachment.getContentStream();

        OutputStream out = new FileOutputStream(docFile);

        byte[] buffer = new byte[1024];
        int position = 0;
        try {
            while ((position = in.read(buffer)) != -1) {
                out.write(buffer, 0, position);
            }
        } finally {
            out.flush();
            out.close();
            in.close();
        }

        return docFile;
    }

    public static boolean hasDocDir(int id) {

        File file = new File(OUTPUT_PATH + id);

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            return true;
        }

        return false;
    }


    public static boolean hasDoc(int id) {

        File file = new File(OUTPUT_PATH + id);

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory() && file.list().length > 0) {
            return true;
        }

        return false;
    }


    public static byte[] encryptToBytes(int id, int page, String key) throws Exception {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page" + page + ".swf";
        File in = new File(dir + fileName);
        if (!in.exists() || in.isDirectory()) {
            return null;
        }
        return XXTEA.encrypt(FileUtils.getBytesFromFile(in), key.getBytes("UTF-8"));
    }


    public static File encryptToFile(int id, int page, String key) throws Exception {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page" + page + ".swf";
        String output = dir + key + File.separator + fileName;
        File in = new File(dir + fileName);
        File out = new File(output);
        if (!in.exists() || in.isDirectory()) {
            return null;
        }
        if (out.exists() && out.isFile()) {
            return out;
        } else {
            out.getParentFile().mkdir();
        }
        return FileUtils.getFileFromBytes(XXTEA.encrypt(FileUtils.getBytesFromFile(in), key.getBytes("UTF-8")), out.getPath());
    }

    public static File getDocFile(int id) {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page.swf";
        return new File(dir + fileName);
    }

    public static File getDocFile(int id, int page) {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page" + page + ".swf";
        return new File(dir + fileName);
    }

    public static byte[] encryptToBytes(int id, String key) throws Exception {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page.swf";
        String output = dir + key + File.separator + fileName;
        File in = new File(dir + fileName);
        File out = new File(output);
        if (!in.exists() || in.isDirectory()) {
            return null;
        }
        if (out.exists() && out.isFile()) {
            return FileUtils.getBytesFromFile(out);
        } else {
            out.getParentFile().mkdir();
        }

        return XXTEA.encrypt(FileUtils.getBytesFromFile(in), key.getBytes("UTF-8"));
    }


    public static File encryptToFile(int id, String key) throws Exception {
        String dir = OUTPUT_PATH + id + File.separator;
        String fileName = "page.swf";
        String output = dir + key + File.separator + fileName;
        File in = new File(dir + fileName);
        File out = new File(output);
        if (!in.exists() || in.isDirectory()) {
            return null;
        }
        if (out.exists() && out.isFile()) {
            return out;
        } else {
            out.getParentFile().mkdir();
        }

        // return in;
        return FileUtils.getFileFromBytes(XXTEA.encrypt(FileUtils.getBytesFromFile(in), key.getBytes("UTF-8")), out.getPath());
    }

    public static String getRandomKey(int length) {
        StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuffer sb = new StringBuffer();
        Random r = new Random();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
    }


    public static void main(String[] args) {
//        String path = "/home/icode/test/";
        try {

            System.out.print(
                    isSupport("xxpdf")
            );
//            byte[] keyValue = "abcs".getBytes("UTF-8");
//            DocViewer.addConvertWorker(new File(path + "3489682.pdf"));
//            DocViewer.addConvertWorker(new File(path + "3692243.pdf"));
//            DocViewer.addConvertWorker(new File(path + "7828624.pdf"));
//            DocViewer.addConvertWorker(new File(path + "test.pdf"));
//            DocViewer.addConvertWorker(new File(path + "test.txt"));
//            DocViewer.addConvertWorker(new File(path + "test1.doc"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
