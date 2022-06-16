package com.log4ic.entity;


import com.log4ic.enums.Permissions;

import java.io.InputStream;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-29 上午10:55
 */
public interface IDocAttachment {
    /**
     * 获得附件名称属性值
     *
     * @return 附件名称属性值
     */
    public String getName();

    /**
     * 获得附件大小属性值
     *
     * @return 附件大小属性值
     */
    public Long getContentSize();

    /**
     * 获得页数属性值
     *
     * @return 页数属性值
     */
    public Integer getPageCount();

    /**
     * 获得文件类型属性值
     *
     * @return 文件类型属性值
     */
    public String getFileType();

    /**
     * 获得附件内容属性值
     *
     * @return 附件内容属性值
     */
    public InputStream getContentStream();

    /**
     * 获取该文档权限
     *
     * @return
     */
    public Permissions getPermissions();
}
