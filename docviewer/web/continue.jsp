<%--
  Created by IntelliJ IDEA.
  User: icode
  Date: 12-1-28
  Time: 上午8:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>跳转提示</title>
</head>
<body>
<div>
    文档已经上传完毕，并进入转换列队，系统将在<span>10</span>秒钟后为您跳转至文档列表....
</div>
<script>
    (function () {
        var timeCmp = document.getElementsByTagName('span')[0];
        var times = 0;
        setInterval(function () {
            timeCmp.innerHTML = 10 - times;
            times++;
            if (times === 10) {
                window.location.replace('documents.jsp');
            }
        }, 1000);
    })();
</script>
</body>
</html>