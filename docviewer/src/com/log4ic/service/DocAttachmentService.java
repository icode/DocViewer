package com.log4ic.service;

import com.log4ic.entity.DocAttachment;
import com.log4ic.entity.IDocAttachment;
import com.log4ic.enums.Permissions;
import com.log4ic.services.IAttachmentService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-10-12 下午7:08
 */
public class DocAttachmentService implements IAttachmentService {
    public IDocAttachment getDocAttachmentById(int id) {
        //TODO  获取文档
        File f = new File("/home/icode/Tools/resources/基于oracle的全文检索的方案.doc");//new File(this.getClass().getClassLoader().getResource("test.pdf").getPath());
        return new DocAttachment(f);
    }

    public Permissions getDocPermissionsById(int id, HttpServletRequest request) {
        //TODO 检查权限
        return Permissions.READ_AND_COPY;
    }

}