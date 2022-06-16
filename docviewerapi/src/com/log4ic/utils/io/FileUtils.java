package com.log4ic.utils.io;

import com.sun.istack.internal.Nullable;
import info.monitorenter.cpdetector.io.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Properties;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-25 上午10:17
 */
public class FileUtils {

    public static Properties getProperties(String config) throws Exception {
        Properties properties = new Properties();
        //获取class文件夹
        ClassLoader loader = FileUtils.class.getClassLoader();
        //加载文件
        InputStream is = loader.getResourceAsStream(config);
        if (is == null) {
            throw new Exception("properties is not found");
        }
        //读取
        properties.load(is);
        return properties;
    }

    public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");


    public static String getFilePrefix(String fileFullName) {
        int splitIndex = fileFullName.lastIndexOf(".");
        return fileFullName.substring(0, splitIndex);
    }

    public static String getFilePrefix(File file) {
        String fileFullName = file.getName();
        return getFilePrefix(fileFullName);
    }

    public static String getFileSuffix(String fileFullName) {
        int splitIndex = fileFullName.lastIndexOf(".");
        return fileFullName.substring(splitIndex + 1);
    }

    public static String getFileSuffix(File file) {
        String fileFullName = file.getName();
        return getFileSuffix(fileFullName);
    }


    public static String appendFileSeparator(String path) {
        return path + (path.lastIndexOf(File.separator) == path.length() - 1 ? "" : File.separator);
    }

    /**
     * 文件转化为字节数组
     */
    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 把字节数组保存为一个文件
     */
    public static File getFileFromBytes(byte[] b, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 从字节数组获取对象
     */
    public static Object getObjectFromBytes(byte[] objBytes) throws Exception {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }

    /**
     * 从对象获取一个字节数组
     */
    public static byte[] getBytesFromObject(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        return bo.toByteArray();
    }

    /**
     * 获取文件编码格式(字符集)
     *
     * @param file 输入文件
     * @return 字符集名称，如果不支持的字符集则返回null
     */
    public static Charset getFileEncoding(File file) {
        /*------------------------------------------------------------------------
          detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
          cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法
          加进来，如ParsingDetector、 JChardetFacade、ASCIIDetector、UnicodeDetector。
          detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
          字符集编码。
           使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
           cpDetector是基于统计学原理的，不保证完全正确。
        --------------------------------------------------------------------------*/
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        /*-------------------------------------------------------------------------
          ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
          指示是否显示探测过程的详细信息，为false不显示。
        ---------------------------------------------------------------------------*/
        detector.add(new ParsingDetector(false));
        /*--------------------------------------------------------------------------
         JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
        ---------------------------------------------------------------------------*/
        detector.add(JChardetFacade.getInstance());//用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        Charset charset = null;
        try {
            charset = detector.detectCodepage(file.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return charset;
    }


    /**
     * 文件编码格式转换
     *
     * @param inFile     输入文件
     * @param inCharset  输入文件字符集
     * @param outFile    输出文件
     * @param outCharset 输出文件字符集
     * @return 转码后的字符流
     * @throws IOException
     */
    public static byte[] convertFileEncoding(File inFile, Charset inCharset, @Nullable File outFile, Charset outCharset) throws IOException {

        RandomAccessFile inRandom = new RandomAccessFile(inFile, "r");


        FileChannel inChannel = inRandom.getChannel();

        //将输入文件的通道通过只读的权限 映射到内存中。
        MappedByteBuffer byteMapper = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) inFile.length());

        CharsetDecoder inDecoder = inCharset.newDecoder();
        CharsetEncoder outEncoder = outCharset.newEncoder();

        CharBuffer cb = inDecoder.decode(byteMapper);
        ByteBuffer outBuffer = null;
        try {
            outBuffer = outEncoder.encode(cb);

            RandomAccessFile outRandom = null;
            FileChannel outChannel = null;
            if (outFile != null) {
                try {
                    outRandom = new RandomAccessFile(outFile, "rw");
                    outChannel = outRandom.getChannel();
                    outChannel.write(outBuffer);
                } finally {
                    if (outChannel != null) {
                        outChannel.close();
                    }
                    if (outRandom != null) {
                        outRandom.close();
                    }
                }
            }
        } finally {
            inChannel.close();
            inRandom.close();
        }


        return outBuffer.array();
    }

    /**
     * 文件编码格式转换
     *
     * @param inFile     输入文件
     * @param inCharset  输入文件字符集
     * @param outCharset 输出字符集
     * @return 转码后的字符流
     * @throws IOException Exception
     */
    public static byte[] convertFileEncoding(File inFile, Charset inCharset, Charset outCharset) throws IOException {
        return convertFileEncoding(inFile, inCharset, (File) null, outCharset);
    }

    /**
     * 将文件字符集转化为指定字符集
     *
     * @param inFile     输入文件
     * @param outFile    输出文件
     * @param outCharset 输出文件字符集
     * @return 转码后的字符流
     * @throws IOException
     */
    public static byte[] convertFileEncoding(File inFile, @Nullable File outFile, Charset outCharset) throws IOException {
        return convertFileEncoding(inFile, getFileEncoding(inFile), outFile, outCharset);
    }

    /**
     * 将文件字符集转化为指定字符集
     *
     * @param inFile 输入文件
     * @return 转码后的字符流
     * @throws IOException
     */
    public static byte[] convertFileEncoding(File inFile, Charset outCharset) throws IOException {
        return convertFileEncoding(inFile, (File) null, outCharset);
    }

    /**
     * 将文件字符集转换为系统字符集
     *
     * @param inFile 输入文件
     * @return 转码后的字符流
     * @throws IOException
     */
    public static byte[] convertFileEncodingToSys(File inFile) throws IOException {
        return convertFileEncoding(inFile, (File) null, Charset.defaultCharset());
    }

    /**
     * 将文件字符集转换为系统字符集
     *
     * @param inFile  输入文件
     * @param outFile 输出文件
     * @return 转码后的字符流
     * @throws IOException
     */
    public static byte[] convertFileEncodingToSys(File inFile, @Nullable File outFile) throws IOException {
        return convertFileEncoding(inFile, outFile, Charset.defaultCharset());
    }


    /**
     * 将数据写入文件
     *
     * @param inputStream
     * @param filePath
     * @return
     */
    public static File writeFile(InputStream inputStream, String filePath) {
        FileOutputStream out = null;
        FileChannel outChannel = null;

        File file = new File(filePath);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {

            out = new FileOutputStream(file);
            outChannel = out.getChannel();

            byte[] buffer = new byte[1024];

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            ByteBuffer outBuffer = ByteBuffer.allocate(buffer.length);

            while (bufferedInputStream.read(buffer)!=-1) {
                outBuffer.put(buffer);
                outBuffer.flip();
                outChannel.write(outBuffer);
                outBuffer.clear();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file.exists() ? file : null;
    }
}
