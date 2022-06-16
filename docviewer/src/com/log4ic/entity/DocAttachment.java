package com.log4ic.entity;

import com.log4ic.enums.Permissions;
import com.log4ic.utils.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-10-13 上午10:10
 */
public class DocAttachment implements IDocAttachment {

    private String name;
    private Long contentSize;
    private Integer pageCount;
    private String fileType;
    private InputStream contentStream;
    private Permissions permissions;

    public DocAttachment(File file) {
        this.name = FileUtils.getFilePrefix(file);
        this.contentSize = file.length();
        this.fileType = FileUtils.getFileSuffix(file);
        try {
            this.contentStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getName() {
        return name;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getFileType() {
        return fileType;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }
}
