package com.log4ic.utils;

import java.io.*;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-25 上午10:17
 */
public class FileUtils {

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

}
