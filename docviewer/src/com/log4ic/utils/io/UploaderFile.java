package com.log4ic.utils.io;

import java.io.File;
import java.net.URI;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-21
 * @time: 下午9:00
 */
public class UploaderFile extends File {
    protected String uploadName;
    protected String id;

    public UploaderFile(String pathname, String uploadName, String id) {
        super(pathname);
        this.uploadName = uploadName;
        this.id = id;
    }

    public UploaderFile(String parent, String child, String uploadName, String id) {
        super(parent, child);
        this.uploadName = uploadName;
        this.id = id;
    }

    public UploaderFile(File parent, String child, String uploadName, String id) {
        super(parent, child);
        this.uploadName = uploadName;
        this.id = id;
    }

    public UploaderFile(URI uri, String uploadName, String id) {
        super(uri);
        this.uploadName = uploadName;
        this.id = id;
    }

    public String getUploadName() {
        return this.uploadName;
    }

    public String getId() {
        return this.id;
    }

}
