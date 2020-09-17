package com.freewinesearcher.online.web20;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.freewinesearcher.common.Dbutil;

public class Publications extends ArrayList<Publication>{
	
	public Publications(Subject subject, int tenant){
		super();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from publications where tenant="+tenant+" and type='"+subject.type+"' and refid="+subject.rowid+" order by id desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				add((Publication)Serializer.readJavaObject("publications", tenant, rs.getInt("id"), "publication"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
}
