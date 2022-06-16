(function (w) {
    w.docviewer = $.extend(w.docviewer, {
        isArray:function (arr) {
            return Object.prototype.toString.call(arr) == '[object Array]';
        },
        isString:function (str) {
            return typeof str == 'string';
        },
        isObject:function (obj) {
            return obj && typeof obj == 'object' && !docviewer.isArray(obj);
        },
        isNumber:function (num) {
            return (num instanceof Number || typeof num == "number");
        }
    });
})(window);
/**
 * file uploader
 */
(function (w) {
    if (w.plupload) {
        plupload.guidPrefix = 'docviewer-';
    }
    var contextPath = '/';
    w.docviewer || (w.docviewer = {});
    var uploaderTpl = [
        '<div id="${containerId}" class="uploader-box">',
        '<input type="hidden" name="uploader" value="${baseId}"/>',
        '<div class="clearfix upload-btn">',
        '<a id="${buttonId}" href="#"><b class="icon-add"></b>新增文件</a>',
        '</div>',
        '<ul>',
        '</ul>',
        '</div>'
    ].join('');
    var fileItemTpl = [
        '<li id="${id}" class="fileitem clearfix">',
        '<b class="icon-att"></b>',
        '<span class="filename">${name}</span>',
        '<span class="filesize">(${size})</span>',
        '<span class="pro-bar">',
        '<span class="pro-inner" style="width:0%"></span>',
        '</span>',
        '<a href="#">删除</a>',
        '</li>'
    ].join('');
    var hiddenItemTpl = '<input type="hidden" name="${fileIdName}" value="${fileId}"/><input type="hidden" name="${fileId}" value="${fileName}"/>';
    w.docviewer.io = {};
    w.docviewer.io.uploader = function (cfg) {
        if (docviewer.isString(cfg.renderTo)) {
            var el = $(cfg.renderTo);
        }
        var uploaderId = cfg.uploaderId,
            params = cfg.params,
            maxSize = cfg.maxSize,
            chunkSize = cfg.chunkSize || 1024;
        var baseId = uploaderId || plupload.guid(),
            boxId = 'uploader-' + baseId,
            complete = true,
            buttonId = 'uploader-browse-button-' + baseId,
            uploaderEl = el.append(
                uploaderTpl.replace('${containerId}', boxId)
                    .replace('${buttonId}', buttonId)
                    .replace('${baseId}', baseId)
            ),
            progressBars = {},
            getProgressBar = function (id) {
                return progressBars[id] || (progressBars[id] = uploaderEl.find('#fileItem-' + id + ' .pro-inner'));
            },
            getProgressBarWrap = function (id) {
                var bar = getProgressBar(id);
                var bp = bar && bar.parent && bar.parent();
                return bp;
            },
            uploader = new plupload.Uploader($.extend({
                runtimes:'gears,flash,silverlight,html5,html4',
                url:contextPath + 'upload',
                max_file_size:maxSize || '1024mb',
                chunk_size:(chunkSize / 1024) + 'mb',
                unique_names:true,
                multiple_queues:true,
                multipart:true,
                flash_swf_url:contextPath + 'scripts/lib/plupload/js/plupload.flash.swf',
                silverlight_xap_url:contextPath + 'scripts/lib/plupload/js/plupload.silverlight.xap',
                browse_button:buttonId,
                container:boxId,
                drop_element:boxId,
                multipart_params:$.extend(params, {uploaderId:baseId}),
                file_data_name:'upload',
                headers:{
                    chunkSize:chunkSize
                }
            }, cfg)),
            fileList = uploaderEl.find('ul');
        var filters = uploader.settings.filters;
        // delete action
        fileList.delegate('a', 'click', function () {
            var fileItem = this.parentNode;
            var fileId = fileItem.id.replace('fileItem-', '');
            var file = uploader.getFile(fileId);
            if (file) {
                uploader.removeFile(file);
            } else {
                uploader.trigger('FilesRemoved', [
                    {id:fileId}
                ]);
            }
            return false;
        });
        //bind event
        uploader.bind('FilesAdded', function (up, files) {
            if (files.length > 0) {

                if (this.isComplete()) {
                    complete = false;
                }

                uploader.addDisplayFile(files);

                //auto start upload
                doStart(1400);
            }
        });

        var doStart = function (dely) {
            if (uploader.state === plupload.STOPPED) {
                setTimeout(function () {
                    if (uploader.state === plupload.STOPPED) {
                        uploader.start();
                    }
                }, dely);
            }
        };

        var onRemoved = function () {
            if (uploader.total.queued > 0) {
                doStart(140);
            } else {
                uploader.trigger('UploadComplete', uploader, uploader.files);
            }
        };
        var doStop = function () {
            if (uploader.state === plupload.STARTED || uploader.state === plupload.UPLOADING) {
                uploader.stop();
            }
        };

        var getHiddenItemTpl = function (file) {
            return hiddenItemTpl.replace('${fileIdName}', baseId).replace(/\$\{fileId\}/g, file.id).replace('${fileName}', file.name);
        };

        uploader.bind('FilesRemoved', function (up, files) {
            if (files.length === uploader.total.queued) {
                doStop();
            }
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                if (progressBars[file.id]) {
                    doStop();
                }
                var fileItem = fileList.find('#fileItem-' + file.id);
                fileItem.animate({
                    height:0,
                    opacity:0
                }, 500, function () {
                    fileItem.remove();
                    delete progressBars[file.id];
                    if (i === files.length) {
                        onRemoved();
                    }
                });
            }
        });

        uploader.bind('UploadProgress', function (up, file) {
            var bar = getProgressBar(file.id);
            bar && bar.css('width', file.percent + '%');
        });
        var err = function (bp, e) {
            bp.html(e.code === plupload.FILE_SIZE_ERROR ? e.message + plupload.formatSize(uploader.settings.max_file_size) : e.message).removeClass('pro-bar').addClass('failure');
        };
        uploader.bind('FileUploaded', function (up, file, res) {
            var bp = getProgressBarWrap(file.id);
            try {
                var fileJson = $.parseJSON(res.response);
                bp.animate({
                    width:0,
                    opacity:0
                }, 500, function () {
                    $(getHiddenItemTpl(file)).replaceAll(bp);
                });
            } catch (e) {
                err(bp);
            }
        });

        uploader.bind('BeforeUpload', function () {
            complete = false;
        });

        uploader.bind('UploadComplete', function () {
            complete = true;
        });

        uploader.bind('Error', function (up, e) {
            var bp = getProgressBarWrap(e.file.id);
            err(bp, e);
        });

        uploader.init();

        uploader.isStop = function () {
            return uploader.state === plupload.STOPPED;
        };

        uploader.isComplete = function () {
            return complete;
        };

        uploader.clear = function () {
            uploader.splice();
            fileList.empty();
        };

        uploader.addDisplayFile = function (files) {
            if (docviewer.isObject(files)) {
                files = [files];
            }
            if (docviewer.isArray(files)) {
                var html = [];
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    var tpl = fileItemTpl.replace('${id}', 'fileItem-' + file.id)
                        .replace('${name}', file.name)
                        .replace('${size}', plupload.formatSize(file.size));
                    if (file.canDelete === false) {
                        tpl = tpl.replace('<a href="#">删除</a>', '');
                    }
                    if (!docviewer.isNumber(file.loaded) || file.loaded === file.size) {
                        tpl = tpl.replace(/<span class="pro-bar">[\s\S]*?<\/span>/, getHiddenItemTpl(file));
                    }
                    html[html.length] = tpl;
                }
                html = $(html.join(''));
                html.css('display', 'none');
                fileList.append(html);
                html.fadeIn(500);
            }
        };

        var addFileButton = document.getElementById(buttonId),
            addFileButtonParent = addFileButton.parentNode;

        uploader.disableAddFile = function (bool) {
            var parentNode = addFileButton.parentNode;
            if (bool) {
                if (parentNode) {
                    parentNode.removeChild(addFileButton);
                }
            } else {
                if (parentNode !== addFileButtonParent) {
                    addFileButtonParent.appendChild(addFileButton);
                }
            }
        };

        return uploader;
    };
})(window);