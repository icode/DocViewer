package com.log4ic.utils.web.filter;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-28
 * @time: 下午10:31
 */
public class CharacterEncodingFilter implements Filter {
    private static boolean responseSetCharacterEncodingAvailable = false;

    static {
        try {
            responseSetCharacterEncodingAvailable = ClassUtils.getPublicMethod(
                    HttpServletResponse.class, "setCharacterEncoding", new Class[]{String.class}) != null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean forceEncoding;
    private String encoding;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.encoding = filterConfig.getInitParameter("encoding");
        String fe = filterConfig.getInitParameter("forceEncoding");
        if (StringUtils.isNotBlank(fe)) {
            this.forceEncoding = Boolean.parseBoolean(fe);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (this.encoding != null && (this.forceEncoding || request.getCharacterEncoding() == null)) {
            request.setCharacterEncoding(this.encoding);
            if (this.forceEncoding && responseSetCharacterEncodingAvailable) {
                response.setCharacterEncoding(this.encoding);
            }
        }
        filterChain.doFilter(request, response);
    }

    public void destroy() {
    }
}
