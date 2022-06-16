/**
 * @author: 张立鑫
 * @version: 11-8-16 下午1:42
 * 文档阅读器
 */
(function(w, u) {
    var ID_INDEX = 0,
        getId = function() {
            return 'docViewer_' + (++ID_INDEX);
        };
    var head = document.getElementsByTagName('head')[0],
        style = document.createElement('style'),
        cssContent = ['.doc-viewer-loading-message{width:350px;height:30px;position:absolute;top:0;left:50%;text-align:center;margin-left:-180px;z-index:1000;background-color:#FFF1A8;border:1px solid #FFF1A8;border-radius:0 0 10px 10px;-moz-border-radius:0 0 10px 10px;-webkit-border-radius:0 0 10px 10px;box-shadow:#000 0px 0px 10px;-webkit-box-shadow:#000 0px 0px 10px;-moz-box-shadow:#000 0px 0px 10px;}}',
            '.full-fill{width:100%;width:100%;}',
            '.doc-viewer-content{padding:0;overflow:hidden;}',
            '.doc-viewer-save-btn{border:#fff 1px solid;background:none;padding:0;margin:0;}'
        ].join('');
    head.appendChild(style);
    if (style.styleSheet) {   //For IE
        style.styleSheet.cssText = cssContent;
    } else {
        var css_text = document.createTextNode(cssContent);
        style.appendChild(css_text);
    }
    function errorAlert(error, message) {
        if (!$.fn.dialog) {
            return error;
        }
        var el = $('<div>' + message + '</div>');
        el.appendTo('body');
        el.dialog({
            modal: true,
            buttons: {
                Ok: function() {
                    el.dialog('destroy');
                }
            }
        });
        return error;
    }


    w.DocViewer = function(args) {
        args = args || {};
        var me = this;
        me.config = $.extend({
            'case':w.DocViewer.WINDOW_CASE,
            renderTo:document.body,
            width:700,
            height:500,
            minHeight:300,
            minWidth:650,
            permissions:w.DocViewer.READ_ONLY_PERMS,
            requestDocInfo:true,
            docId:u,
            flash:{
                //vars
                //params
                //attributes
            }
            //params
        }, (me.arguments = args));

        if (!args.docUrl && !(this.config.requestDocInfo && args.docId)) {
            var error = {msg:'文档地址错误！',error:'arguments.docUrl'};
            return errorAlert(error, '文档地址错误！');
        } else if ((!me.config.permissions || me.config.permissions === w.DocViewer.NONE_PERMS) && !(this.config.requestDocInfo && args.docId)) {
            var error = {msg:'您无权查看此文档！',error:'arguments.permissions'};
            return errorAlert(error, '您无权查看此文档！');
        }

        me.docUrl = args.docUrl;

        me.docId = args.docId;

        me.id = (me.config.id = args.id || getId());
        me.swfId = (me.config.swfId = args.swfId || me.id + '_swf');
        if (!(this.config.requestDocInfo && args.docId)) {
            me.deployPermissions();
        }

        me.el = $('<div id="' + me.id + '" class="doc-viewer-content" style="width:' + me.config.width + ';height:' + me.config.height + ';display:' + (me.config['case'] == w.DocViewer.WINDOW_CASE ? 'none;margin:5px' : 'block;margin:0') + ';"><div class="doc-viewer-loading-message">初始化...</div><div id="' + me.swfId + '" class="full-fill"></div></div>');

        me.loadingMessage = me.el.find('div.doc-viewer-loading-message');

        me.el.appendTo(me.config.renderTo);

        switch (me.config['case']) {
            case w.DocViewer.WINDOW_CASE:
                if (me.el.dialog) {
                    me.el.dialog($.extend({
                        title:'文档查看器',
                        autoOpen:false
                    }, me.config));
                    $.extend(me, {
                        show:function() {
                            me.el.dialog('open');
                            return me;
                        },
                        hide:function() {
                            me.el.dialog('close');
                            return me;
                        },
                        destroy:function() {
                            me.el.dialog('destroy');
                            return me;
                        }
                    });
                }
                break;
            case w.DocViewer.NODE_CASE:

                break;
        }


        if (!w.swfobject) {
            $.getScript(w.DocViewer.SWF_OBJECT_URL, function() {
                me.build();
            })
        } else {
            me.build();
        }

        return me;
    };
    w.DocViewer.prototype = {
        show:function() {
            this.el.show();
            return this;
        },
        hide:function() {
            this.el.hide();
            return this;
        },
        destroy:function() {
            this.el.remove();
            return this;
        },
        build:function() {
            this.el.trigger('beforebuild');

            var me = this,
                el = me.el,
                cfg = me.config,
                fa = cfg.flash,
                timer,
                build = function() {
                    swfobject.embedSWF(fa.url,
                        me.swfId, '100%', '100%', '10.0.0', w.DocViewer.SWF_OBJECT_SWF_URL,
                        fa.vars, fa.params, fa.attributes, function() {
                            el.flash = document.getElementById(me.swfId);
                            if (cfg.permissions == w.DocViewer.WRITE_ANNOTATIONS_PERMS && !el.saveButton) {
                                var eventArgs = {
                                    click:function() {
                                        me.saveAnnotations();
                                    },
                                    mouseenter:function() {
                                        el.saveButton.css(
                                            'border', '#0085E7 1px solid'
                                        );
                                    },
                                    mouseleave:function() {
                                        el.saveButton.css(
                                            'border', '#fff 1px solid'
                                        );
                                    },
                                    mousedown:function() {
                                        el.saveButton.css('background', '#B2E1FF');
                                    },
                                    mouseup:function() {
                                        el.saveButton.css('background', 'none');
                                    }
                                };
                                setTimeout(function() {
                                    el.saveButton = $('<button type="button" class="doc-viewer-save-btn">Save</button>');
                                    el.saveButton.appendTo(el).css({
                                        position : 'absolute',
                                        'z-index' : 100,
                                        bottom : 5,
                                        right : 10,
                                        'font-size':12
                                    }).bind(eventArgs);
                                    me.on({
                                        'beforesaveannotations':function() {
                                            el.saveButton.css('border', '#fff 1px solid');
                                            el.saveButton.unbind(eventArgs);
                                            el.saveButton.html('Saving..');
                                        },
                                        'saveannotations':function() {
                                            el.saveButton.html('Done!');
                                            w.setTimeout(function() {
                                                el.saveButton.html('Save');
                                                el.saveButton.bind(eventArgs);
                                            }, 1500);
                                        },
                                        'saveannotationserror':function() {
                                            el.saveButton.html('Error!');
                                            w.setTimeout(function() {
                                                el.saveButton.html('Save');
                                                el.saveButton.bind(eventArgs);
                                            }, 2000);
                                        }
                                    });
                                }, 2000);
                            } else if (cfg.permissions == w.DocViewer.WRITE_ANNOTATIONS_PERMS && el.saveButton) {
                                el.saveButton.remove();
                            }

                            setTimeout(function() {
                                me.loadingMessage.stop();
                                me.loadingMessage.animate({
                                    opacity: 0,
                                    top: -40
                                }, 500, function() {
                                    //el.flash.setViewerFocus(true);
                                    me.loadingMessage.html('初始化...');
                                });
                            }, 1200);


                            el.trigger('build');
                        }
                    );
                };

            me.loadingMessage.stop();
            me.loadingMessage.animate({
                opacity: 1,
                top: 0
            }, 500);

            if (cfg.requestDocInfo && me.docId) {
                var params = $.extend({}, cfg.params, {
                    docId:me.docId
                });
                el.trigger('requestdocinfo', [params]);
                me.loadingMessage.html('请求文档信息...');
                timer = setTimeout(function() {
                    me.loadingMessage.html('等待文档转换完成...');
                }, 50000);
                $.ajax({
                    url : w.DocViewer.REQUEST_DOC_INFO_ACTION,
                    type:'get',
                    context : this,
                    data : params,
                    timeout : 60000,
                    error : function(jqXHR, textStatus, errorThrown) {
                        el.trigger('requestdocinfoserror', [params,jqXHR, textStatus, errorThrown]);
                        if (jqXHR.status == 404) {
                            clearTimeout(timer);
                            me.loadingMessage.html('文档未找到!');
                        }
                    },
                    success : function(data, textStatus, jqXHR) {
                        clearTimeout(timer);
                        el.trigger('requestdocinfo', [params,data, textStatus, jqXHR]);
                        cfg.permissions = data.permissions;
                        if (!cfg.permissions || cfg.permissions === w.DocViewer.NONE_PERMS) {
                            var error = {msg:'您无权查看此文档！',error:'arguments.permissions'};
                            me.loadingMessage.html('您无权查看此文档！');
                            return error;
                        }
                        me.docUrl = data.uri;
                        me.deployPermissions();
                        if (data.key) {
                            fa.vars.SecretKey = data.key;
                        }
                        me.loadingMessage.html('加载文档...');
                        build();
                    }
                });
            } else {
                build();
            }
        },
        /**
         * 绑定事件 同jquery bind函数
         */
        bind:function() {
            this.el.bind.apply(this.el, arguments);
        },
        /**
         * 移除事件 同jquery unbind函数
         */
        unbind:function() {
            this.el.unbind.apply(this.el, arguments);
        },
        /**
         * 移除事件 同jquery bind函数
         */
        on:function() {
            this.bind.apply(this, arguments);
        },
        /**
         * 绑定事件 同jquery unbind函数
         */
        un:function() {
            this.unbind.apply(this, arguments);
        },
        /**
         * 部署权限
         */
        deployPermissions:function() {
            var readOnly = true;
            switch (this.config.permissions) {
                case w.DocViewer.READ_ONLY_PERMS:
                    this.config.flash.url = w.DocViewer.SWF_COMMON_URL;
                    readOnly = true;
                    break;
                case w.DocViewer.READ_COPY_PERMS:
                    this.config.flash.url = w.DocViewer.SWF_COMMON_URL;
                    readOnly = false;
                    break;
                case w.DocViewer.WRITE_ANNOTATIONS_PERMS:
                    readOnly = false;
                    $.extend(this, {
                        saveAnnotations:function() {
                            var tel = this.el,
                                params = this.docId && this.config.requestDocInfo ? {docId:this.docId} : {docUrl:this.docUrl};
                            params = $.extend(params, this.config.params, {
                                annotations:$.toJSON(this.getAnnotations())
                            });
                            tel.trigger('beforesaveannotations', [params]);
                            $.ajax({
                                url : w.DocViewer.SAVE_ANNOTATIONS_ACTION,
                                type:'post',
                                context : this,
                                data : params,
                                error : function(jqXHR, textStatus, errorThrown) {
                                    tel.trigger('saveannotationserror', [params,jqXHR, textStatus, errorThrown]);
                                },
                                success : function(data, textStatus, jqXHR) {
                                    tel.trigger('saveannotations', [params,data, textStatus, jqXHR]);
                                }
                            });
                        },
                        clearAnnotations:function() {
                            this.el.flash.clearMarks();
                        },
                        removeAnnotations:function(m) {
                            this.el.flash.removeMark(m);
                        },
                        createAnnotation:function(color) {
                            return this.el.flash.createMark(color);
                        },
                        addAnnotation:function(m) {
                            if (Object.prototype.toString.call(m) === '[object Array]') {
                                this.el.flash.addMarks(m);
                            } else {
                                this.el.flash.addMark(m);
                            }
                        },
                        addAnnotations:function(m) {
                            this.el.flash.addMarks(m);
                        }
                    });
                case w.DocViewer.READ_ANNOTATIONS_PERMS:
                    this.config.flash.url = w.DocViewer.SWF_ANNOTATIONS_URL;
                    readOnly = true;
                    $.extend(this, {
                        scrollToAnnotations:function(m) {
                            this.el.flash.scrollToMark(m);
                        },
                        getAnnotations:function() {
                            return this.el.flash.getMarkList();
                        },
                        loadAnnotations:function() {
                            var me = this,
                                tel = this.el,
                                params = this.docId && this.config.requestDocInfo ? {docId:this.docId} : {docUrl:this.docUrl};
                            params = $.extend(params, this.config.params, {
                                annotations:$.toJSON(this.getAnnotations())
                            });
                            tel.trigger('beforeloadannotations', [params]);
                            $.ajax({
                                url : w.DocViewer.LOAD_ANNOTATIONS_ACTION,
                                type : 'get',
                                context : this,
                                data : params,
                                error : function(jqXHR, textStatus, errorThrown) {
                                    tel.trigger('loadannotationserror', [params,jqXHR, textStatus, errorThrown]);
                                },
                                success : function(data, textStatus, jqXHR) {
                                    tel.trigger('loadannotations', [params,data, textStatus, jqXHR]);
                                    if (!data.error && !data.msg) {
                                        me.addAnnotation(data);
                                    }
                                }
                            });
                        }
                    });
                    if (this.config.permissions == w.DocViewer.READ_ANNOTATIONS_PERMS) {
                        delete this.saveAnnotations;
                    }
                    break;
            }

            this.arguments.flash = this.arguments.flash || {};
            this.config.flash.vars = $.extend({
                SwfFile : this.docUrl,
                Scale : 0.8,
                ZoomTransition : 'easeOut',
                ZoomTime : 0.5,
                ZoomInterval : 0.1,
                FitPageOnLoad : false,
                FitWidthOnLoad : true,
                FullScreenAsMaxWindow : false,
                ProgressiveLoading : true,
                TextSelectEnabled:true,

                ReadOnly:readOnly,

                ViewModeToolsVisible : true,
                ZoomToolsVisible : true,
                FullScreenVisible : true,
                NavToolsVisible : true,
                CursorToolsVisible : true,
                SearchToolsVisible : true,
                LocaleChain : ((w.navigator.userLanguage || w.navigator.language.replace("-", "_")) || '').replace("-", "_")
            }, this.arguments.flash.vars);

            this.config.flash.params = $.extend({
                quality : 'high',
                bgcolor : '#ffffff',
                allowscriptaccess :'sameDomain',
                allowfullscreen : true,
                wmode : 'window'
            }, this.arguments.flash.params);

            this.config.flash.attributes = $.extend({id : this.swfId}, this.arguments.flash.attributes);

            return this;
        },
        /**
         * 设置权限
         * @param perms
         */
        setPermissions:function(perms) {
            if (!perms || this.config.permissions === perms) {
                return;
            }
            this.config.permissions = perms;
            this.deployPermissions();
            this.build();

            return this;
        },
        /**
         * 定位到指定的页面
         * @param pageNumber
         */
        gotoPage:function(pageNumber) {
            this.el.trigger('aftepagechange', pageNumber);
            this.el.flash.gotoPage(pageNumber);
            this.el.trigger('pagechange', pageNumber);
        },
        /**
         * 设置视图到宽度填充模式
         */
        fitWidth:function() {
            this.el.trigger('beforefitwidth');
            this.el.flash.fitWidth();
            this.el.trigger('fitwidth');
        },
        /**
         * 设置视图到高度填充模式
         */
        fitHeight:function() {
            this.el.trigger('beforefitwidth');
            this.el.flash.fitHeight();
            this.el.trigger('fitwidth');
        },
        /**
         * 加载一个新的文件到视图
         * @param url
         */
        loadSwf:function(url) {
            this.el.trigger('beforeloadswf', url);
            this.el.flash.loadSwf(url);
            this.el.trigger('loadswf', url);
        },
        /**
         * 获取当前页码
         */
        getCurrPage:function() {
            return this.el.flash.getCurrPage();
        },
        /**
         * 在加载的文件移动到下一页
         */
        nextPage:function() {
            this.el.trigger('beforepagechange', this.getCurrPage());
            this.el.flash.nextPage();
            this.el.trigger('pagechange', this.getCurrPage());
        },
        /**
         * 在加载的文件移动到前一页
         */
        prevPage:function() {
            this.el.trigger('beforepagechange', this.getCurrPage());
            this.el.flash.prevPage();
            this.el.trigger('pagechange', this.getCurrPage());
        },
        /**
         * 缩放视图到指定的比例
         * @param factor
         */
        setZoom:function(factor) {
            this.el.trigger('beforezoomchange', factor);
            this.el.flash.setZoom(factor);
            this.el.trigger('zoomchange', factor);
        },
        /**
         * 搜索加载文件的指定文本
         * @param text
         */
        searchText:function(text) {
            this.el.trigger('beforesearchtext', text);
            this.el.flash.searchText(text);
            this.el.trigger('searchtext', text);
        },
        /**
         * 切换观看模式。允许的模式是 "Portrait", "Two Page", "Tile"
         * @param mode
         */
        switchMode:function(mode) {
            this.el.trigger('beforeswitchmode', text);
            this.el.flash.switchMode(mode);
            this.el.trigger('switchmode', text);
        },
        /**
         * 打印加载文件
         */
        printPaper:function() {
            this.el.trigger('beforeprintpaper');
            this.el.flash.printPaper();
            this.el.trigger('printpaper');
        },
        /**
         * 根据Adobe的XML规范的突出强调文档中的所有项目
         * @param url
         */
        highlight:function(url) {
            this.el.trigger('beforehighlight', url);
            this.el.flash.highlight(url);
            this.el.trigger('highlight', url);
        },
        /**
         * 提交一个当前文档截图
         * @param url
         */
        postSnapshot:function(url) {
            this.el.trigger('beforepostsnapshot', url);
            this.el.flash.postSnapshot(url);
            this.el.trigger('postsnapshot', url);
        }
    };
    var rootPath = "";
    $.extend(w.DocViewer, {
        NONE_PERMS:0,
        READ_ONLY_PERMS:1,
        READ_COPY_PERMS:2,
        READ_ANNOTATIONS_PERMS:3,
        WRITE_ANNOTATIONS_PERMS:4,
        ALL_PERMS:5,
        WINDOW_CASE:'window',
        NODE_CASE:'node',
        SWF_OBJECT_URL:rootPath + '/scripts/lib/swfobject/swfobject.js',
        SWF_OBJECT_SWF_URL:rootPath + '/scripts/lib/swfobject/expressInstall.swf',
        SWF_COMMON_URL:rootPath + '/resources/flex/docviewer.swf',
        SWF_ANNOTATIONS_URL:rootPath + '/resources/docviewer/docviewer_annotations.swf',
        SAVE_ANNOTATIONS_ACTION:rootPath + '/annotations/save',
        LOAD_ANNOTATIONS_ACTION:rootPath + '/annotations/load',
        REQUEST_DOC_INFO_ACTION:rootPath + '/docviewer/info'
    });
})(window, undefined);