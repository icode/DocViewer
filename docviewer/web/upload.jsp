<%@ page import="com.log4ic.DocViewer" %>
<%@ page import="java.util.List" %>
<%@ page import="org.artofsolving.jodconverter.document.DocumentFormat" %>
<%@ page import="java.util.Locale" %>

<%--
  @author: 张立鑫
  @version: 11-10-23 下午8:10
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>上传文档</title>
    <link rel="stylesheet" href="style/uploader.css"/>
    <script src="scripts/lib/jquery-1.6.4.min.js"></script>
    <script src="scripts/lib/plupload/js/plupload.full.js"></script>
    <script src="scripts/uploader.js"></script>
    <%if(!new Locale("en").equals(request.getLocale())){%>
    <script src="scripts/i18n/<%=request.getLocale().toString().toLowerCase()%>/uploader.js"></script>
    <%}%>
    <%
        List<DocumentFormat> allSupport = DocViewer.getAllSupport();
        StringBuffer allSupportStr = new StringBuffer();
        for (DocumentFormat format : allSupport) {
            allSupportStr.append(format.getExtension());
            allSupportStr.append(",");
        }
        allSupportStr.deleteCharAt(allSupportStr.length() - 1);
    %>
    <script>
        $(function () {
            var uploader = new docviewer.io.uploader({
                renderTo:'#uploader',
                maxSize:'100m',
                filters:[
                    {title:'所有支持文件',extensions:'<%=allSupportStr%>'}
                ]
            });

            $('form').submit(function () {
                if (!uploader.isComplete()) {
                    alert('亲，您还有文件没有上传完哦！请耐心等待文件上传完毕！');
                    return false;
                }
            });

        });
    </script>

</head>
<body>
<form action="/docviewer/upload" method="post">
    <div id="uploader"></div>
    <input type="submit" value="upload">
</form>
</body>
</html>