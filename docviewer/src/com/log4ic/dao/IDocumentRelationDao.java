package com.log4ic.dao;

import com.log4ic.entity.DocumentRelation;

import javax.naming.NamingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-23
 * @time: 上午2:36
 */
public interface IDocumentRelationDao {
    void save(DocumentRelation relation) throws NamingException, SQLException;
    List<DocumentRelation> getAllRelation() throws NamingException, SQLException;
    DocumentRelation getRelation(int id) throws NamingException, SQLException;
    DocumentRelation getRelationByLocation(String location) throws NamingException, SQLException ;
}
