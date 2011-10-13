package com.log4ic.servlet;

import com.log4ic.DocViewer;
import com.log4ic.enums.Permissions;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author: 张立鑫
 * @date: 11-8-18 下午2:22
 */
public class DocViewerServlet extends HttpServlet {

    public void getDocInfo(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("docId");
            if (StringUtils.isBlank(id)) {
                response.setStatus(404);
                return;
            }


            int docId = Integer.parseInt(id);

            Permissions permissions = DocViewer.getAttachmentService().getDocPermissionsById(docId, request);

            if (permissions.equals(Permissions.NONE)) {
                //response.setStatus(404);
                return;
            }

            int pageCount = DocViewer.getDocPageCount(docId);
            if (pageCount == 0) {
                response.setStatus(404);
                return;
            }
            response.setContentType("application/json");
            String url = "/docviewer?doc=";
            String docUri = request.getContextPath() + url + id + "-{[*,0]," + pageCount + "}\"";
            if (!DocViewer.isSplitPage()) {
                docUri = request.getContextPath() + url + id + ".swf";
            }

            if (DocViewer.isEncryption()) {
                String secretKey = DocViewer.getCurrentSecretKey();
                request.getSession().setAttribute("secretKey", secretKey);
                response.getWriter().write("{\"uri\":\"" + docUri + ",\"key\":\"" + secretKey + "\",\"permissions\":" + permissions.ordinal() + "}");
            } else {
                response.getWriter().write("{\"uri\":\"" + docUri + ",\"permissions\":" + permissions.ordinal() + "}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(404);
        }
    }

    public void getDoc(HttpServletRequest request, HttpServletResponse response) {
        String doc = request.getParameter("doc");
        if (StringUtils.isBlank(doc)) {
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
            if (DocViewer.isEncryption()) {
                secretKey = request.getSession().getAttribute("secretKey") + "";
            }
        } catch (Exception e) {
            response.setStatus(404);
            return;
        }

        OutputStream outp = null;
        InputStream in = null;
        try {
            if (DocViewer.getDoc(docId) != null) {
                outp = response.getOutputStream();
                if (DocViewer.isSplitPage()) {
                    if (DocViewer.isEncryption()) {
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
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if (outp != null) {
                try {
                    outp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outp = null;
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals("/docviewer/info")) {
            getDocInfo(req, resp);
        } else if (req.getRequestURI().equals("/docviewer")) {
            getDoc(req, resp);
        }
    }
}
