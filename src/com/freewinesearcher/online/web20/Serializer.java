package com.freewinesearcher.online.web20;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.freewinesearcher.common.Dbutil;

public class Serializer {


	public static long writeJavaObject(String table, int tenant, long id, long versionid, HashMap<String,Object> values) throws Exception {


		long resultid=0;
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;

		String className;
		PreparedStatement pstmt;

		try {
			con.setAutoCommit(false);
			if (id>0){
				String sql = "UPDATE "+table+" set ";
				for (String key:values.keySet()){
					sql+=key+"=?, ";
				}
				sql=sql.substring(0,sql.length()-2);
				sql+=" where tenant=? and id=?;";
				pstmt = con.prepareStatement(sql);
				setParameters(pstmt, values);
				int i=values.keySet().size();
				i++;
				pstmt.setInt(i, tenant);
				i++;
				pstmt.setLong(i, id);
				resultid=pstmt.executeUpdate();
				if (resultid==0) {
					Dbutil.logger.error("Problem: Could not update object with id "+id);
				} else {
				        resultid=id;
				}
			} else {
				String sql="insert into "+table+" (";
				for (String key:values.keySet()){
					sql+=key+",";
				}
				sql+="tenant) values(";
				for (String key:values.keySet()){
					sql+="?, ";
				}
				sql+="?);";
				pstmt = con.prepareStatement(sql);
				setParameters(pstmt, values);
				int i=values.keySet().size();
				i++;
				pstmt.setInt(i, tenant);
				pstmt.executeUpdate();
				// get the generated key for the id
				rs = pstmt.getGeneratedKeys();
				resultid = -1;
				if (rs.next()) {
					resultid = rs.getLong(1);
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally{
			Dbutil.closeRs(rs);
			con.commit();
			Dbutil.closeConnection(con);
		}
		return resultid;
	}
	
	public static PreparedStatement setParameters(PreparedStatement pstmt,HashMap<String,Object> values) throws SQLException{
		int i=0;
		for (String key:values.keySet()){
			i++;
			if (values.get(key) instanceof java.lang.String){
				pstmt.setString(i, (String)values.get(key));
			} else if (values.get(key) instanceof java.lang.Integer){
				pstmt.setInt(i, (Integer)values.get(key));
			} else if (values.get(key) instanceof java.sql.Timestamp){
				pstmt.setTimestamp(i, (java.sql.Timestamp)values.get(key));
			} else if (values.get(key) instanceof java.lang.Long){
				pstmt.setLong(i, (Long)values.get(key));
			} else {
				ByteArrayOutputStream baos;
				ObjectOutputStream out;
				baos = new ByteArrayOutputStream();
				try {
					out = new ObjectOutputStream(baos);
					out.writeObject(values.get(key));
					out.close();
				} catch (Exception e) {
					Dbutil.logger.error("Problem:",e);
				}
				byte[] byteObject = baos.toByteArray();
				pstmt.setObject(i, byteObject);
			}
		}
		return pstmt;
	}

	public static Object readJavaObject(String table, int tenant, long id, String objectfield) throws Exception {
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;
		Object object=null;
		byte[] byteObject=null;
		try{
			PreparedStatement pstmt = con.prepareStatement("select * from "+table+" where tenant=? and id=?;");
			pstmt.setLong(1, (long)tenant);
			pstmt.setLong(2, id);
			rs = pstmt.executeQuery();
			if (rs.next()){
				byteObject = rs.getBytes(objectfield);
				ByteArrayInputStream bais;
				ObjectInputStream in;
				bais = new ByteArrayInputStream(byteObject);
				in = new ObjectInputStream(bais);
				object=in.readObject();
				in.close();
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return object;
	}
}

