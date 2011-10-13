package com.log4ic.entity;

import com.log4ic.utils.FileUtils;

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

    String name;
    Long contentSize;
    Integer pageCount;
    String digest;
    String fileType;
    InputStream contentStream;

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

    public String getDigest() {
        return digest;
    }

    public String getFileType() {
        return fileType;
    }

    public InputStream getContentStream() {
        return contentStream;
    }
}
