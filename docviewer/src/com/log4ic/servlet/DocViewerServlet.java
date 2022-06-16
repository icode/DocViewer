package com.log4ic.servlet;

import com.log4ic.DocViewer;
import com.log4ic.dao.impl.DocumentRelationDao;
import com.log4ic.entity.DocumentRelation;
import com.log4ic.enums.Permissions;
import com.log4ic.utils.io.Uploader;
import com.log4ic.utils.io.UploaderFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author: 张立鑫
 * @date: 11-8-18 下午2:22
 */
public class DocViewerServlet extends HttpServlet {

    private static final Log LOGGER = LogFactory.getLog(DocViewerServlet.class);

    public void getDocInfo(HttpServletRequest request, HttpServletResponse response) {

        PrintWriter writer = null;

        try {
            String id = request.getParameter("docId");
            LOGGER.info("请求文档信息 ID:" + id);
            if (StringUtils.isBlank(id)) {
                LOGGER.info("ID为空!");
                response.setStatus(404);
                return;
            }


            int docId = Integer.parseInt(id);
            LOGGER.info("获取文档权限...");
            Permissions permissions = DocViewer.getAttachmentService().getDocPermissionsById(docId, request);
            LOGGER.info("文档权限:" + permissions);
            if (permissions.equals(Permissions.NONE)) {
                //response.setStatus(404);
                LOGGER.info("无权查看该文档!");
                return;
            }

            response.setContentType("application/json");
            response.setStatus(202);
            response.setHeader("Keep-Alive", "600");
            response.flushBuffer();
            writer = response.getWriter();

            writer.flush();

            LOGGER.info("获取文档页数...");
            int pageCount = DocViewer.getDocPageCount(docId);
            if (pageCount == 0) {
                response.setStatus(404);
                LOGGER.info("空白文档!");
                return;
            }
            LOGGER.info("文档页数:" + pageCount + "页.");
            String docUri;
            String url = "/docviewer?doc=";
            if (!DocViewer.isSplitPage()) {
                docUri = request.getContextPath() + url + id;
            } else {
                docUri = request.getContextPath() + url + id + "-{[*,0]," + pageCount + "}";
            }
            LOGGER.info("回传文档信息...");
            if (DocViewer.isEncryption()) {
                String secretKey = DocViewer.getCurrentSecretKey();
                request.getSession().setAttribute("secretKey", secretKey);
                writer.write("{\"uri\":\"" + docUri + "\",\"key\":\"" + secretKey + "\",\"permissions\":" + permissions.ordinal() + "}");
            } else {
                writer.write("{\"uri\":\"" + docUri + "\",\"permissions\":" + permissions.ordinal() + "}");
            }
        } catch (Exception e) {
            LOGGER.error(e);
            response.setStatus(404);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public void getDoc(HttpServletRequest request, HttpServletResponse response) {
        String doc = request.getParameter("doc");

        if (StringUtils.isBlank(doc)) {
            LOGGER.info("doc不能为空!");
            response.setStatus(404);
            return;
        }
        int docId = -1;
        int docPage = -1;
        String secretKey = "";
        try {
            if (DocViewer.isSplitPage()) {
                String[] docInfo = doc.split("-");
                if (docInfo.length != 2) {
                    response.setStatus(404);
                    return;
                }
                docId = Integer.parseInt(docInfo[0]);
                docPage = Integer.parseInt(docInfo[1]);
            } else {
                docId = Integer.parseInt(doc);
            }
        } catch (Exception e) {
            response.setStatus(404);
            return;
        }

        OutputStream outp = null;
        InputStream in = null;
        try {
            LOGGER.info("获取文档!");
            if (DocViewer.getDoc(docId) != null) {
                if (DocViewer.isEncryption()) {
                    secretKey = request.getSession().getAttribute("secretKey") + "";
                }
                outp = response.getOutputStream();
                if (DocViewer.isSplitPage()) {
                    if (DocViewer.isEncryption()) {
                        LOGGER.info("加密文档...");
                        if (DocViewer.isDynamicKey()) {
                            byte[] page = DocViewer.encryptToBytes(docId, docPage, secretKey);
                            in = new ByteArrayInputStream(page);
                        } else {
                            File page = DocViewer.encryptToFile(docId, docPage, secretKey);
                            in = new FileInputStream(page.getPath());
                        }
                    } else {
                        in = new FileInputStream(DocViewer.getDocFile(docId, docPage));
                    }
                } else {
                    if (DocViewer.isEncryption()) {
                        LOGGER.info("加密文档...");
                        if (DocViewer.isDynamicKey()) {
                            byte[] page = DocViewer.encryptToBytes(docId, secretKey);
                            in = new ByteArrayInputStream(page);
                        } else {
                            File page = DocViewer.encryptToFile(docId, secretKey);
                            in = new FileInputStream(page.getPath());
                        }
                    } else {
                        in = new FileInputStream(DocViewer.getDocFile(docId));
                    }
                }
                response.setContentLength(in.available());

                byte[] b = new byte[1024];
                int i = 0;

                while ((i = in.read(b)) > 0) {
                    outp.write(b, 0, i);
                }
                outp.flush();
            }
        } catch (Exception ex) {
            response.setStatus(404);
            LOGGER.error(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
                in = null;
            }
            if (outp != null) {
                try {
                    outp.flush();
                    outp.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
                outp = null;
            }
        }
    }

    private DocumentRelationDao relationDao = new DocumentRelationDao();

    private void uploadFile(HttpServletRequest request, HttpServletResponse response) {
        try {
            List<UploaderFile> uploaderFileList = Uploader.getFileList();
            if (uploaderFileList.size() > 0) {
                String path = this.getClass().getResource("/").getPath();
                for (UploaderFile file : uploaderFileList) {
                    File newFile = new File(path + File.separator + "documents" + File.separator + file.getName());
                    FileUtils.copyFile(file, newFile);
                    DocumentRelation relation = new DocumentRelation();
                    relation.setFileName(file.getUploadName());
                    relation.setLocation(newFile.getPath());
                    relation.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    relationDao.save(relation);
                    if(DocViewer.isSupport(FilenameUtils.getExtension(file.getUploadName()))){
                        relation = relationDao.getRelationByLocation(relation.getLocation());
                        DocViewer.addConvertWorker(relation.getId());
                    }
                }

                response.sendRedirect("/continue.jsp");
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals(req.getContextPath() + "/docviewer/info")) {
            LOGGER.info("DocViewer doGet:" + req.getContextPath() + "/docviewer/info");
            LOGGER.info("请求文档信...");
            getDocInfo(req, resp);
        } else if (req.getRequestURI().equals(req.getContextPath() + "/docviewer")) {
            LOGGER.info("DocViewer doGet:" + req.getContextPath() + "/docviewer");
            LOGGER.info("加载文档...");
            getDoc(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals(req.getContextPath() + "/docviewer/upload")) {
            LOGGER.info("DocViewer doPost:" + req.getContextPath() + "/docviewer/upload");
            LOGGER.info("上传文件...");
            uploadFile(req, resp);
        }
    }

}
