package com.log4ic.servlet;

import com.log4ic.utils.io.Uploader;
import javolution.util.FastMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-21
 * @time: 下午11:11
 */
public class UploaderServlet extends HttpServlet {
    private static final Log LOGGER = LogFactory.getLog(UploaderServlet.class);


    private void uploadFile(HttpServletRequest request, HttpServletResponse response) {

        PrintWriter writer = null;

        //本次块大小
        int chunk = 0;
        //总共块数
        int chunks = 0;

        // 代上传文件的File对象
        InputStream upload = null;

        //本次上传器的ID 作为临时目录的目录名称
        String uploaderId = null;
        // 上传文件名
        String uploadFileName = null;
        //文件唯一标识名称
        String name = null;

        try {

            if (request.getContentType() != null && request.getContentType().indexOf("multipart/form-data;") != -1) {
                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();

                // Create a new file upload handler
                ServletFileUpload servletUpload = new ServletFileUpload(factory);

                // Parse the request
                List<FileItem> items = servletUpload.parseRequest(request);

                Map<String, String> map = new FastMap<String, String>();
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        map.put(item.getFieldName(), item.getString());
                    } else {
                        if ("upload".equals(item.getFieldName())) {
                            upload = item.getInputStream();
                            // 上传文件名
                            uploadFileName = item.getName();
                        }
                    }
                }
                try {
                    //本次块大小
                    chunk = Integer.parseInt(map.get("chunk"));
                    //总共块数
                    chunks = Integer.parseInt(map.get("chunks"));
                } catch (Exception e) {
                }
                //本次上传器的ID 作为临时目录的目录名称
                uploaderId = map.get("uploaderId");

                //文件唯一标识名称
                name = map.get("name");

            }

            if (upload == null ||
                    StringUtils.isBlank(uploaderId) ||
                    StringUtils.isBlank(name) ||
                    StringUtils.isBlank(uploadFileName)) {
                response.setStatus(404);
                return;
            }
            //分块上传
            String chunkSize = request.getHeader("chunkSize");
            int cSize = 0;
            try {
                cSize = StringUtils.isNotBlank(chunkSize) ? Integer.parseInt(chunkSize) : 0;
            } catch (Exception e) {
            }
            if (!Uploader.saveFile(upload, uploaderId, name, chunks, chunk, cSize)) {
                response.setStatus(404);
                return;
            }

            writer = response.getWriter();
            writer.print("{\"success\":true}");
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals(req.getContextPath() + "/upload")) {
            LOGGER.info("DocViewer doPost:" + req.getContextPath() + "/upload");
            LOGGER.info("上传文件...");
            uploadFile(req, resp);
        }
    }
}
