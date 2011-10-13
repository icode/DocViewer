<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="text/html;charset=utf-8"/>
    <title>文档查看器</title>
    <script src="scripts/lib/jquery-1.6.4.min.js"></script>
    <script src="scripts/lib/jquery.json.min.js"></script>
    <script src="scripts/docviewer.js"></script>
    <script>
        $(function() {
            function getParameter(propties) {
                var re = new RegExp(propties + "=([^\&]*)", "i"),
                        a = re.exec(location.search);
                if (!a) return null;
                return a[1];
            }



            var docViewer = new DocViewer({
                docUrl:getParameter('url'),
                'case':DocViewer.NODE_CASE,
                width:'100%',
                height:'100%',
                docId:getParameter('id'),
                requestDocInfo:getParameter('info')
            });
            if (!docViewer.error) {
                docViewer.on({
                    'saveannotations': function(e, params, data) {
                        var el = $('<div>' + (data || '保存成功！') + '</div>');
                        el.appendTo(this.el);
                        el.dialog({
                            modal: true,
                            buttons: {
                                Ok: function() {
                                    el.dialog('destroy');
                                }
                            }
                        });
                    },
                    'saveannotationserror': function() {
                        var el = $('<div>发生错误！</div>');
                        el.appendTo(this.el);
                        el.dialog({
                            modal: true,
                            buttons: {
                                Ok: function() {
                                    el.dialog('destroy');
                                }
                            }
                        });
                    }
                });
            }
        });
    </script>
    <style>
        * {
            margin: 0;
            padding: 0;
        }

        body,html {
            overflow: hidden;
            height: 100%;
            width: 100%;
        }
    </style>
    <!--<script src="../../external/jquery.bgiframe-2.1.2.js"></script>-->
    <!--<script src="../../ui/jquery.ui.core.js"></script>-->
    <!--<script src="../../ui/jquery.ui.widget.js"></script>-->
    <!--<script src="../../ui/jquery.ui.mouse.js"></script>-->
    <!--<script src="../../ui/jquery.ui.button.js"></script>-->
    <!--<script src="../../ui/jquery.ui.draggable.js"></script>-->
    <!--<script src="../../ui/jquery.ui.position.js"></script>-->
    <!--<script src="../../ui/jquery.ui.resizable.js"></script>-->
    <!--<script src="../../ui/jquery.ui.dialog.js"></script>-->
    <!--<script src="../../ui/jquery.effects.core.js"></script>-->
</head>
<body>

</body>
</html>