package com.freewinesearcher.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Serializer {

		static final String INSERT_OBJECT_SQL = "INSERT INTO [tablename](tenant,shopid,classname, versionid, objectvalue) VALUES (?,?,?,?,?)";
		static final String UPDATE_OBJECT_SQL = "UPDATE [tablename] set objectvalue=?, versionid=? where tenant=? and id=? and shopid=? and classname=?";
		static final String READ_OBJECT_SQL = "SELECT * FROM [tablename] WHERE id = ? and shopid = ? and tenant = ?";
	  

	  public static long writeJavaObject(String table, int tenant, int shopid, long id, long versionid, Object object) throws Exception {
		  ByteArrayOutputStream baos;
		  ObjectOutputStream out;
		  baos = new ByteArrayOutputStream();
		  try {
		  out = new ObjectOutputStream(baos);
		  out.writeObject(object);
		  out.close();
		  } catch (Exception e) {
		  Dbutil.logger.error("Problem:",e);
		  }
		  byte[] byteObject = baos.toByteArray();
		  
		  
		  
		long resultid=0;
		  Connection con=Dbutil.openNewConnection();
	    ResultSet rs=null;
	    
	    String className;
	    PreparedStatement pstmt;

	    try {
			con.setAutoCommit(false);
			className = object.getClass().getName();
			if (id>0){
				pstmt = con.prepareStatement(UPDATE_OBJECT_SQL.replace("[tablename]", table));
				// set input parameters
				pstmt.setObject(1, byteObject);
				pstmt.setLong(2, versionid);
				pstmt.setInt(3, tenant);
				pstmt.setLong(4, id);
				pstmt.setInt(5, shopid);
				pstmt.setString(6, className);
				resultid=pstmt.executeUpdate();
				if (resultid==0) Dbutil.logger.error("Problem: Could not update object with id "+id);

				
			} else {
			pstmt = con.prepareStatement(INSERT_OBJECT_SQL.replace("[tablename]", table),PreparedStatement.RETURN_GENERATED_KEYS);
			// set input parameters
			pstmt.setInt(1, tenant);
			pstmt.setInt(2, shopid);
			pstmt.setString(3, className);
			pstmt.setLong(4, versionid);
			pstmt.setObject(5, byteObject);
			
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

	  public static Object readJavaObject(String table, int tenant, int shopid, long id) throws Exception {
	    Connection con=Dbutil.openNewConnection();
	    ResultSet rs=null;
	    Object object=null;
	    byte[] byteObject=null;
	    try{
	    PreparedStatement pstmt = con.prepareStatement(READ_OBJECT_SQL.replace("[tablename]", table));
	    pstmt.setLong(1, id);
	    pstmt.setLong(2, shopid);
	    pstmt.setLong(3, tenant);
	    rs = pstmt.executeQuery();
	    if (rs.next()){
	    	byteObject = rs.getBytes("objectvalue");
	    	ByteArrayInputStream bais;
	    	ObjectInputStream in;
	    	bais = new ByteArrayInputStream(byteObject);
	    	in = new ObjectInputStream(bais);
	    	object = (Class.forName(rs.getString("classname")).cast(in.readObject()));
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

