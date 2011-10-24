package com.log4ic.utils.convert.office;

import com.log4ic.utils.FileUtils;
import com.sun.star.comp.helper.BootstrapException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeConnectionProtocol;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;

/**
 * @author: 张立鑫
 * @date: 11-8-15 下午4:17
 * office 转换器
 */
public class OfficeConverter {

    private static final Log LOGGER = LogFactory.getLog(OfficeConverter.class);

    private static OfficeConnectionProtocol CONNECTION_PROTOCOL;

    private static File OFFICE_PROFILE;

    private static OfficeManager officeManager;

    /**
     * 配置文件
     */
    private static String CONFIG_FILE = "config" + File.separator + "office-convert.properties";
    /**
     * open office 目录
     */
    private static String OFFICE_HOME = "";

    /**
     * 远程主机地址
     */
    private static String HOST = "127.0.0.1";
    /**
     * 远程地址端口
     */
    private static int PORT = 8100;

    private static DocumentFormatRegistry documentFormatRegistry = new DefaultDocumentFormatRegistry();

    private static Properties properties;

    public static boolean isSupport(String fileExtends) {
        return documentFormatRegistry.getFormatByExtension(fileExtends) != null;
    }


    /**
     * 将office转换为PDF
     *
     * @param inputFile
     * @param out       文件名或文件地址
     * @throws ConnectException
     */
    public File toPDF(File inputFile, String out) throws IOException {
        File outputFile = new File(out);
        if (inputFile.exists()) {
            if (!outputFile.exists()) {
                outputFile.mkdirs();
            }
            if (outputFile.isDirectory()) {
                String name = FileUtils.getFilePrefix(inputFile);
                outputFile = new File(FileUtils.appendFileSeparator(out) + name + ".pdf");
            } else {
                if (!out.endsWith(".pdf")) {
                    outputFile = new File(FileUtils.getFilePrefix(out) + ".pdf");
                }
            }
            return convert(inputFile, outputFile);
        } else {
            throw new IOException("file not found!");
        }
    }

    /**
     * 将office转换为PDF
     *
     * @param inputFile 生成文件为当前目录
     * @throws ConnectException
     */
    public File toPDF(File inputFile) throws IOException {
        String name = FileUtils.getFilePrefix(inputFile) + ".pdf";
        return toPDF(inputFile, name);
    }

    /**
     * 根据传入文件后缀名转换文件
     *
     * @param in
     * @param out
     */
    public File convert(String in, String out) throws IOException {
        File inputFile = new File(in);
        File outputFile = new File(out);
        return convert(inputFile, outputFile);
    }

    /**
     * 根据传入文件后缀名转换文件
     *
     * @param inputFile
     * @param outputFile
     */
    public File convert(File inputFile, File outputFile) throws IOException {
        try {
            if (System.getProperty("os.name").startsWith("Windows") && inputFile.getName().endsWith(".txt")) {
                File tmp = new File(inputFile.getName().replace(".txt", ".odt"));
                if (!tmp.exists()) {
                    org.apache.commons.io.FileUtils.copyFile(inputFile, tmp);
                }
                inputFile = tmp;
            }
            LOGGER.debug("进行文档转换转换:" + inputFile.getPath() + " --> " + outputFile.getPath());
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

            converter.convert(inputFile, outputFile);

            LOGGER.debug("文档转换完成:" + inputFile.getPath() + " --> " + outputFile.getPath());
            return outputFile;
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
        }
        return null;
    }

    public static void setConfig(String cfgName, String cfgValue) throws Exception {
        Properties properties = getProperties();
        properties.setProperty(cfgName, cfgValue);
    }

    public static Properties getProperties() throws Exception {
        if (properties == null) {
            properties = new Properties();
            //获取class文件夹
            ClassLoader loader = OfficeConverter.class.getClassLoader();
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

    public static void startService() throws Exception {
        DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
        try {
            LOGGER.debug("准备启动服务....");
            configuration.setOfficeHome(getOfficeHome());
            configuration.setPortNumber(getPort());
            officeManager = configuration.buildOfficeManager();
            if (CONNECTION_PROTOCOL != null) {
                configuration.setConnectionProtocol(CONNECTION_PROTOCOL);
            }
            if (OFFICE_PROFILE != null) {
                configuration.setTemplateProfileDir(OFFICE_PROFILE);
            }
            officeManager.start();
            LOGGER.debug("office转换服务启动成功!");
        } catch (Exception ce) {
        } finally {

        }

    }

    public static void stopService() throws Exception, BootstrapException {
        LOGGER.debug("关闭office转换服务....");
        if (officeManager != null) {
            officeManager.stop();
        }
        LOGGER.debug("关闭office转换成功!");
    }

    //getter and setter
    public static String getOfficeHome() {
        return OFFICE_HOME;
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPort() {
        return PORT;
    }

    public static void setOfficeHome(String officeHome) {
        OfficeConverter.OFFICE_HOME = FileUtils.appendFileSeparator(officeHome);
        LOGGER.debug("设置office目录为：" + OfficeConverter.OFFICE_HOME);
    }

    public static void setPort(int port) {
        LOGGER.debug("设置office转换服务端口为：" + port);
        OfficeConverter.PORT = port;
    }

    public static void setHost(String host) {
        LOGGER.debug("设置office转换服务主机为：" + host);
        OfficeConverter.HOST = host;
    }

    public static void setConnectionProtocol(OfficeConnectionProtocol protocol) {
        LOGGER.debug("设置office转换服务协议为：" + protocol);
        OfficeConverter.CONNECTION_PROTOCOL = protocol;
    }

    public static void setTemplateProfileDir(File file) {
        LOGGER.debug("设置office转换服务临时实例目录为：" + file.getPath());
        OfficeConverter.OFFICE_PROFILE = file;
    }
}
