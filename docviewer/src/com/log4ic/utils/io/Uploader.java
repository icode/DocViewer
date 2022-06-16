package com.log4ic.utils.io;

import javolution.util.FastList;
import org.apache.commons.io.*;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-21
 * @time: 下午8:59
 */
public class Uploader {
    private final static ThreadLocal<List<UploaderFile>> threadLocal = new ThreadLocal<List<UploaderFile>>();

    private static String tempDir;

    public static String getTempDir() {
        return tempDir;
    }

    public static void setTempDir(String tempDir) {
        Uploader.tempDir = tempDir.endsWith(File.separator) ? tempDir : tempDir + File.separator;
    }

    /**
     * 获取当前的文件列表
     *
     * @return list
     */
    public static List<UploaderFile> getFileList() {
        return threadLocal.get() == null ? new FastList<UploaderFile>() : threadLocal.get();
    }

    /**
     * 获取一个文件
     *
     * @param id
     * @return
     */
    public static UploaderFile getFile(String id) {
        for (UploaderFile file : getFileList()) {
            if (file.getId() != null && file.getId().equals(id)) {
                return file;
            }
        }
        return null;
    }

    /**
     * 添加一个文件到文件列表
     *
     * @param file 文件
     */
    public static void addFile(UploaderFile file) {
        List list = threadLocal.get();
        if (list == null) {
            list = new FastList<UploaderFile>();
            threadLocal.set(list);
        }
        list.add(file);
    }

    /**
     * 添加一个文件到文件列表
     *
     * @param fileList 文件
     */
    public static void addAll(List<UploaderFile> fileList) {
        List list = threadLocal.get();
        if (list == null) {
            threadLocal.set(fileList);
        } else {
            list.addAll(fileList);
        }
    }

    /**
     * 移除一个文件
     *
     * @param file
     */
    public static void removeFile(UploaderFile file) {
        List list = threadLocal.get();
        if (list == null) {
            return;
        }
        file.delete();
        list.remove(file);
    }

    /**
     * 清空文件列表
     */
    public static void removeAll() {
        List<UploaderFile> list = threadLocal.get();
        if (list == null) {
            return;
        }
        if (!list.isEmpty()) {
            File dir = list.get(0).getParentFile();

            for (File file : dir.listFiles()) {
                file.delete();
            }

            dir.delete();
        }
        threadLocal.remove();
    }

    /**
     * 格式化请求为文件列表
     *
     * @param request
     * @return
     */
    public static List<UploaderFile> parseRequest(ServletRequest request) {
        return parseRequest(new RequestParameterHelper(request));
    }

    /**
     * 格式化请求为文件列表
     *
     * @param request
     * @param uploaderId
     * @return
     */
    public static List<UploaderFile> parseRequest(ServletRequest request, String uploaderId) {
        return parseRequest(new RequestParameterHelper(request), uploaderId);
    }

    /**
     * 格式化请求为文件列表
     *
     * @param helper
     * @return
     */
    public static List<UploaderFile> parseRequest(IParameterHelper helper) {
        String uploaderId = helper.getParameter("uploader");
        return parseRequest(helper, uploaderId);
    }

    /**
     * 格式化请求为文件列表
     *
     * @param helper
     * @param uploaderId
     * @return
     */
    public static List<UploaderFile> parseRequest(IParameterHelper helper, String uploaderId) {

        List<UploaderFile> list = new FastList<UploaderFile>();

        if (StringUtils.isBlank(uploaderId)) {
            return list;
        }

        String[] ids = helper.getParameterValues(uploaderId);

        if (ids == null) {
            return list;
        }


        for (String id : ids) {
            String fileName = helper.getParameter(id);
            if (StringUtils.isNotBlank(fileName)) {
                String filePath = tempDir + uploaderId + File.separator + id + "." + FileUtils.getFileSuffix(fileName);
                UploaderFile tempFile = new UploaderFile(filePath, fileName, id);
                if (tempFile.exists()) {
                    list.add(tempFile);
                }
            }
        }
        return list;
    }


    /**
     * 格式化请求为文件列表
     *
     * @param map
     * @return
     */
    public static List<UploaderFile> parseRequest(Map<String, String> map) {
        return parseRequest(new MapParameterHelper(map));
    }

    /**
     * 格式化请求为文件列表
     *
     * @param map
     * @param uploaderId
     * @return
     */
    public static List<UploaderFile> parseRequest(Map<String, String> map, String uploaderId) {
        return parseRequest(new MapParameterHelper(map), uploaderId);
    }


    /**
     * 存储文件
     *
     * @param inputStream 上传文件
     * @param uploaderId  上传器ID
     * @param fileName    上传文件名
     * @param chunks      总文件块数
     * @param chunk       当前块大小
     * @param chunkSize   接受的块大小
     * @return 是否存存成功
     */
    public static boolean saveFile(InputStream inputStream, String uploaderId, String fileName, int chunks, int chunk, int chunkSize) {
        String dstPath = tempDir + uploaderId + File.separator + fileName;
        File dstFile = new File(dstPath);
        //分块上传
        if (chunks > 1) {
            if (chunkSize > 0) {
                File file = null;
                // 文件已存在删除旧文件（上传了同名的文件）
                if (chunk == 0 && dstFile.exists()) {
                    dstFile.delete();
                } else if (!(file = dstFile.getParentFile()).exists()) {
                    file.mkdirs();
                }
                Uploader.appendChunk(inputStream, dstFile, chunkSize);
            } else {
                return false;
            }
        } else {//一次上传
            FileUtils.writeFile(inputStream, dstPath);
        }
        return true;
    }


    /**
     * 向文件内追加文件块
     *
     * @param src        欲追加文件块
     * @param dst        追加到的目标文件
     * @param bufferSize 块大小
     */
    public static void appendChunk(File src, File dst, int bufferSize) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src), bufferSize);
            appendChunk(in, dst, bufferSize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向文件内追加文件块
     *
     * @param in         欲追加文件
     * @param dst        追加到的目标文件
     * @param bufferSize 块大小
     */
    public static void appendChunk(InputStream in, File dst, int bufferSize) {
        OutputStream out = null;
        try {
            if (dst.exists()) {
                out = new BufferedOutputStream(new FileOutputStream(dst, true),
                        bufferSize);  //plupload 配置了chunk的时候新上传的文件appand到文件末尾
            } else {
                out = new BufferedOutputStream(new FileOutputStream(dst),
                        bufferSize);
            }

            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static interface IParameterHelper {
        public String getParameter(String name);

        public String[] getParameterValues(String name);
    }

    public static class RequestParameterHelper implements IParameterHelper {
        private ServletRequest request;

        public RequestParameterHelper(ServletRequest request) {
            this.request = request;
        }

        public String getParameter(String name) {
            return this.request.getParameter(name);
        }

        public String[] getParameterValues(String name) {
            return this.request.getParameterValues(name);
        }
    }

    public static class MapParameterHelper implements IParameterHelper {
        private Map<String, String> map;

        public MapParameterHelper(Map<String, String> map) {
            this.map = map;
        }

        public String getParameter(String name) {
            return this.map.get(name);
        }

        public String[] getParameterValues(String name) {
            return map.get(name).split(",");
        }
    }

}
