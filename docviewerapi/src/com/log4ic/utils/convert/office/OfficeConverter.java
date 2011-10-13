package com.log4ic.utils.convert.office;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.log4ic.utils.FileUtils;
import com.log4ic.utils.convert.office.connector.BootstrapSocketConnector;
import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XDesktop;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author: 张立鑫
 * @date: 11-8-15 下午4:17
 * office 转换器
 */
public class OfficeConverter {

    private static final Log LOGGER = LogFactory.getLog(OfficeConverter.class);

    private static XComponentContext context;

    private static BootstrapSocketConnector bootstrapSocketConnector;

    private static List<OpenOfficeConnection> connectionPoll = new ArrayList<OpenOfficeConnection>();
    /**
     * 配置文件
     */
    private static String CONFIG_FILE = "config" + File.separator + "office-convert.properties";
    /**
     * open office 目录
     */
    private static String OPEN_OFFICE_HOME = "";
    /**
     * *
     * 远程服务文件目录
     */
    private static String CLASS_PATH = OPEN_OFFICE_HOME + "program/";
    /**
     * 远程主机地址
     */
    private static String HOST = "127.0.0.1";
    /**
     * 远程地址端口
     */
    private static int PORT = 8100;
    //远程连接
    private OpenOfficeConnection connection;

    private static DocumentFormatRegistry documentFormatRegistry = new DefaultDocumentFormatRegistry();

    private static Properties properties;

    public static boolean isSupport(String fileExtends) {
        return documentFormatRegistry.getFormatByFileExtension(fileExtends) != null;
    }

    /**
     * 以默认端口获取远程连接
     *
     * @return
     */
    public OpenOfficeConnection getServiceConnection() throws ConnectException {
        return getServiceConnection(PORT);
    }

    /**
     * *
     * 获取远程服务连接
     *
     * @param port
     * @return
     */
    public OpenOfficeConnection getServiceConnection(int port) throws ConnectException {
        if (this.connection == null) {
            this.connection = new SocketOpenOfficeConnection(port);
            connectionPoll.add(this.connection);
        }
        if (!this.connection.isConnected()) {
            try {
                // 链接远程服务
                this.connection.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.connection;
    }

    /**
     * 关闭连接
     */
    public void closeServiceConnection() {
        if (this.connection != null) {
            this.connection.disconnect();
            connectionPoll.remove(this.connection);
        }
    }

    /**
     * 销毁连接
     */
    public void destroyServiceConnection() {
        this.closeServiceConnection();
        this.connection = null;
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
        LOGGER.debug("链接运行的远程实例");
        getServiceConnection();
        try {
            if (System.getProperty("os.name").startsWith("Windows") && inputFile.getName().endsWith(".txt")) {
                File tmp = new File(inputFile.getName().replace(".txt", ".odt"));
                if (!tmp.exists()) {
                    org.apache.commons.io.FileUtils.copyFile(inputFile, tmp);
                }
                inputFile = tmp;
            }
            if (connection != null && connection.isConnected()) {
                // 转换
                LOGGER.debug("进行文档转换转换:" + inputFile.getPath() + " --> " + outputFile.getPath());
                DocumentConverter converter = new OpenOfficeDocumentConverter(connection);

                converter.convert(inputFile, outputFile);

                LOGGER.debug("文档转换完成:" + inputFile.getPath() + " --> " + outputFile.getPath());
                return outputFile;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            try {
                //关闭连接
                LOGGER.debug("关闭链接中的运行的远程实例链接");
                closeServiceConnection();
                LOGGER.debug("关闭链接中的运行的远程实例链接完毕!");
            } catch (Exception e) {
                LOGGER.error("关闭链接中的运行的远程实例链失败:" + e);
            }
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

    /**
     * 读取配置文件
     */
    public static void loadConfig() throws Exception {
        Properties properties = getProperties();

        //设置office目录  property中的中文进行转码处理
        setOpenOfficeHome(new String(properties.getProperty("open_office_home", OPEN_OFFICE_HOME).getBytes("ISO-8859-1"), "UTF-8"));
        setHost(new String(properties.getProperty("host", HOST).getBytes("ISO-8859-1"), "UTF-8"));
        setPort(Integer.parseInt(properties.getProperty("port", PORT + "")));
    }

    public static void startService() throws Exception {
        SocketOpenOfficeConnection connection = new SocketOpenOfficeConnection(PORT);
        try {
            LOGGER.debug("检测openoffice服务....");
            connection.connect();
            connection.disconnect();
            LOGGER.debug("openoffice服务已启动!");
        } catch (Exception ce) {
            LOGGER.debug("openoffice服务未启动!准备启动服务....");
            //读取配置
            try {
                LOGGER.debug("初始化openoffice服务配置....");
                loadConfig();
                LOGGER.debug("初始化服务配置完毕!启动服务....");
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            }

            // 启动open office远程服务
            bootstrapSocketConnector = new BootstrapSocketConnector(CLASS_PATH);
            context = bootstrapSocketConnector.connect(HOST, PORT);
            LOGGER.debug("启动openoffice成功!");
        } finally {
            connection = null;
        }

    }

    public static void stopService() throws Exception, BootstrapException {
        LOGGER.debug("关闭openoffice服务....");
        for (OpenOfficeConnection conn : connectionPoll) {
            conn.disconnect();
        }

        if (bootstrapSocketConnector != null) {
            bootstrapSocketConnector.disconnect();

            bootstrapSocketConnector = null;
        } else {
            XComponentContext xLocalContext = Bootstrap.createInitialComponentContext(null);
            if (xLocalContext == null) {
                throw new BootstrapException("no local component context!");
            }

            // create a URL resolver
            XUnoUrlResolver xUrlResolver = UnoUrlResolver.create(xLocalContext);

            // get remote context

            String hostAndPort = "host=" + HOST + ",port=" + PORT;

            // connection string
            String unoConnectString = "uno:socket," + hostAndPort + ";urp;StarOffice.ComponentContext";

            Object context = xUrlResolver.resolve(unoConnectString);
            XComponentContext xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, context);
            if (xRemoteContext == null) {
                throw new BootstrapException("no component context!");
            }

            // get desktop to terminate office
            Object desktop = xRemoteContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            xDesktop.terminate();
        }

        context = null;
        LOGGER.debug("关闭openoffice成功!");
    }

    public static void setOpenOfficeHomeConfig(String openOfficeHome) throws Exception {
        setConfig("open_office_home", openOfficeHome);
        setOpenOfficeHome(openOfficeHome);
    }

    public static void setPortConfig(int port) throws Exception {
        setConfig("port", port + "");
        setPort(port);
    }

    public static void setHostConfig(String host) throws Exception {
        setConfig("host", host);
        setHost(host);
    }

    //getter and setter
    public static String getClassPath() {
        return CLASS_PATH;
    }

    public static String getOpenOfficeHome() {
        return OPEN_OFFICE_HOME;
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPort() {
        return PORT;
    }

    private static void setOpenOfficeHome(String openOfficeHome) {
        OfficeConverter.OPEN_OFFICE_HOME = FileUtils.appendFileSeparator(openOfficeHome);
        OfficeConverter.setClassPath(FileUtils.appendFileSeparator(OfficeConverter.OPEN_OFFICE_HOME + "program"));
        LOGGER.debug("设置openoffice目录为：" + OfficeConverter.OPEN_OFFICE_HOME);
    }

    private static void setClassPath(String classPath) {
        OfficeConverter.CLASS_PATH = FileUtils.appendFileSeparator(classPath);
        LOGGER.debug("设置openoffice服务文件目录为：" + OfficeConverter.CLASS_PATH);
    }

    private static void setPort(int port) {
        LOGGER.debug("设置openoffice服务端口为：" + port);
        OfficeConverter.PORT = port;
    }

    private static void setHost(String host) {
        LOGGER.debug("设置openoffice服务主机为：" + host);
        OfficeConverter.HOST = host;
    }


    public static void main(String[] args) {

        try {
            OfficeConverter.startService();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        Thread thread0 = new Thread(new Runnable() {
//            public void run() {
//                OfficeConverter converter = new OfficeConverter();
//                try {
//                    converter.toPDF(new File("/home/icode/test/test1.doc"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread0.start();
//
//        Thread thread1 = new Thread(new Runnable() {
//            public void run() {
//                OfficeConverter converter = new OfficeConverter();
//                try {
//                    converter.toPDF(new File("/home/icode/test/test2.doc"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread1.start();
//
//        Thread thread2 = new Thread(new Runnable() {
//            public void run() {
//                OfficeConverter converter = new OfficeConverter();
//                try {
//                    converter.toPDF(new File("/home/icode/test/test3.doc"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread2.start();
//
//        Thread thread3 = new Thread(new Runnable() {
//            public void run() {
//                OfficeConverter converter = new OfficeConverter();
//                try {
//                    converter.toPDF(new File("/home/icode/test/test4.doc"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread3.start();
//
//        Thread thread4 = new Thread(new Runnable() {
//            public void run() {
//                OfficeConverter converter = new OfficeConverter();
//                try {
//                    converter.toPDF(new File("/home/icode/test/test5.doc"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread4.start();


        OfficeConverter converter = new OfficeConverter();
        try {
            converter.toPDF(new File("/home/icode/test/test.txt"), "/home/icode/test/2");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OfficeConverter.stopService();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
