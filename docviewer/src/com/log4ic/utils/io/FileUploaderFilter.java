package com.log4ic.utils.io;

import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-21
 * @time: 下午9:11
 */
public class FileUploaderFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        String tempDir = filterConfig.getInitParameter("tempDir");
        if (StringUtils.isBlank(tempDir)) {
            tempDir = this.getClass().getResource("/").getFile();
            tempDir += "uploader" + File.separator + "tempDir";
        } else {

        }
        Uploader.setTempDir(tempDir);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String uploaderId = request.getParameter("uploader");

        if (StringUtils.isBlank(uploaderId)) {
            filterChain.doFilter(request, response);
            return;
        }

        String[] ids = request.getParameterValues(uploaderId);

        if (ids == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Uploader.addAll(Uploader.parseRequest(request));

        filterChain.doFilter(request, response);
        Uploader.removeAll();
    }

    public void destroy() {
        Uploader.removeAll();
    }
}

