<%@ page import="com.log4ic.dao.impl.DocumentRelationDao" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="com.log4ic.entity.DocumentRelation" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%--
  Created by IntelliJ IDEA.
  User: icode
  Date: 12-1-23
  Time: 上午12:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>所有文档</title>
    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        a {
            text-decoration: none;
        }

        ol {
            list-style: none;
        }
    </style>
</head>
<body>
<ol style="margin:10px;">
    <%
        DocumentRelationDao relationDao = new DocumentRelationDao();
        List<DocumentRelation> documentRelationList = relationDao.getAllRelation();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (DocumentRelation relation : documentRelationList) {
    %>
    <li><a href="docviewer.jsp?info=true&id=<%=relation.getId()%>"><%=relation.getFileName()%>
    </a><span>创建时间:<%=sdf.format(relation.getCreateDate())%></span></li>
    <%}%>
</ol>
<div style="margin: 10px;">
    <a href="upload.jsp">去上传新的文档 >></a>
</div>
</body>
</html>