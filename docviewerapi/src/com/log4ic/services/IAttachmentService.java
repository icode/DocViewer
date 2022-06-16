package com.log4ic.services;

import com.log4ic.entity.IDocAttachment;
import com.log4ic.enums.Permissions;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-29 上午10:34
 */
public interface IAttachmentService {

    /**
     * 获取附件
     *
     * @param id 附件ID
     * @return
     */
    public IDocAttachment getDocAttachmentById(int id);

    /**
     * 获取用户附件权限
     *
     * @param id      附件ID
     * @param request
     * @return
     */
    public Permissions getDocPermissionsById(int id, HttpServletRequest request);

}
