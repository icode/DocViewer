package com.log4ic.dao.impl;

import com.log4ic.dao.IDocumentRelationDao;
import com.log4ic.entity.DocumentRelation;
import com.log4ic.utils.dao.DocViewerJdbcSupport;
import javolution.util.FastList;
import javolution.util.FastMap;

import javax.naming.NamingException;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-23
 * @time: 上午2:37
 */
public class DocumentRelationDao extends DocViewerJdbcSupport implements IDocumentRelationDao {
    public void save(DocumentRelation relation) throws NamingException, SQLException {

        Map<Integer, Object> params = new FastMap<Integer, Object>();
        params.put(1, relation.getFileName());
        params.put(2, relation.getLocation());
        params.put(3, relation.getCreateDate());

        this.executeUpdate("insert into documentrelation(filename,location,createdate) values (?,?,?)", params);

    }

    public List<DocumentRelation> getAllRelation() throws NamingException, SQLException {
        Connection conn = this.getDocviewerConnection();
        PreparedStatement stmt = null;
        List<DocumentRelation> relationList = new FastList<DocumentRelation>();
        try {
            stmt = conn.prepareStatement("select * from documentrelation order by createdate desc");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                com.log4ic.entity.DocumentRelation relation = new DocumentRelation();
                relation.setFileName(resultSet.getString("filename"));
                relation.setLocation(resultSet.getString("location"));
                relation.setId(resultSet.getInt("id"));
                relation.setCreateDate(resultSet.getTimestamp("createDate"));
                relationList.add(relation);
            }

        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return relationList;
    }

    public DocumentRelation getRelation(int id) throws NamingException, SQLException {
        Connection conn = this.getDocviewerConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("select * from documentrelation where id = ?");
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                com.log4ic.entity.DocumentRelation relation = new DocumentRelation();
                relation.setFileName(resultSet.getString("filename"));
                relation.setLocation(resultSet.getString("location"));
                relation.setId(resultSet.getInt("id"));
                relation.setCreateDate(resultSet.getTimestamp("createDate"));
                return relation;
            }

        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.close();
        }

        return null;
    }

    public DocumentRelation getRelationByLocation(String location) throws NamingException, SQLException {
        Connection conn = this.getDocviewerConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("select * from documentrelation where location = ?");
            stmt.setString(1, location);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                com.log4ic.entity.DocumentRelation relation = new com.log4ic.entity.DocumentRelation();
                relation.setFileName(resultSet.getString("filename"));
                relation.setLocation(resultSet.getString("location"));
                relation.setId(resultSet.getInt("id"));
                relation.setCreateDate(resultSet.getTimestamp("createDate"));
                return relation;
            }

        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.close();
        }
        return null;
    }
}
