package com.log4ic.service;

import com.log4ic.dao.impl.DocumentRelationDao;
import com.log4ic.entity.DocAttachment;
import com.log4ic.entity.DocumentRelation;
import com.log4ic.entity.IDocAttachment;
import com.log4ic.enums.Permissions;
import com.log4ic.services.IAttachmentService;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.sql.SQLException;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-10-12 下午7:08
 */
public class DocAttachmentService implements IAttachmentService {

    private DocumentRelationDao relationDao = new DocumentRelationDao();

    public IDocAttachment getDocAttachmentById(int id) {
        //TODO  获取文档
        try {
            DocumentRelation relation = relationDao.getRelation(id);
            DocAttachment docAttachment = new DocAttachment(new File(relation.getLocation()));
            docAttachment.setName(relation.getFileName());
            return docAttachment;
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Permissions getDocPermissionsById(int id, HttpServletRequest request) {
        //TODO 检查权限
        return Permissions.READ_AND_COPY;
    }

}