package com.freewinesearcher.online.shoppingcart;

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

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Partner;

public class CartSerializer {

	static final String INSERT_OBJECT_SQL = "INSERT INTO shoppingcarts(tenant,customeruserid,shopid,cartnumber,version,actorrole,actoruserid, status, cart,classname, classversion,date) VALUES (?,?,?,?,?,?,?,?,?,?,?,now())";
	static final String UPDATE_OBJECT_SQL = "UPDATE shoppingcarts set status=? where tenant=? and id=? and shopid=? and classname=? and cartnumber=?";
	static final String READ_OBJECT_SQL = "SELECT * FROM shoppingcarts WHERE id = ? and shopid = ? and tenant = ? and cartnumber=? order by version desc;";
	public static class CartAlreadyExistedException extends Exception{
		CartAlreadyExistedException(){
			super("Could not create a new cart because it conflicts with an existing cart. ");
		}
	}
	public static class ConcurrentEditConflictException extends Exception{
		ConcurrentEditConflictException(){
			super("Could not update cart because it was concurrently edited by another user. ");
		}
	}
	public static class OrderNotVerifiedException extends Exception{
		OrderNotVerifiedException(){
			super("Could not place order because it contains unconfirmed items. ");
		}
	}
	public static class UnknownException extends Exception{}

	public static long writeCart(Context c,Shoppingcart cart) throws Exception,CartAlreadyExistedException,ConcurrentEditConflictException,UnknownError {
		if (c.userid!=null&&!c.userid.equals("")){
			long resultid=0;
			Connection con=Dbutil.openNewConnection();
			con.setAutoCommit(false);
			ResultSet rs=null;
			PreparedStatement pstmt;

			try {
				if (cart.customeruserid==null) cart.customeruserid=c.userid;
				CartManager cm=new CartManager();
				if (cart.cartnumber==0&&cm.getLatestCart(c, 0, cart.shopid)!=null){
					throw new CartAlreadyExistedException();
				} else {
					con.setAutoCommit(false);
					pstmt = con.prepareStatement(INSERT_OBJECT_SQL);
					// set input parameters
					//tenant,customeruserid,shopid,cartnumber,version,actorrole,actoruserid, status, cart,classname, classversion,date
					pstmt.setInt(1, c.tenant);
					pstmt.setString(2, cart.customeruserid);
					pstmt.setInt(3, cart.shopid);
					if (cart.cartnumber==0){
						cart.cartnumber=Dbutil.readIntValueFromDB("select max(cartnumber) as cartnumber from shoppingcarts;", "cartnumber")+1;
					}
					pstmt.setInt(4, cart.cartnumber);
					int lastversion=Dbutil.readIntValueFromDB("select max(version) as version from shoppingcarts where cartnumber="+cart.cartnumber+";", "version");
					cart.version=cart.version+1;
					pstmt.setInt(5, cart.version);
					if (lastversion==cart.version){
						throw new ConcurrentEditConflictException();
					} else {
						pstmt.setString(6, cart.role.toString());
						pstmt.setString(7, c.userid);
						pstmt.setString(8, cart.status.toString());
						pstmt.setString(10, cart.getClass().getName());
						pstmt.setLong(11, cart.serialVersionUID);
						ByteArrayOutputStream baos;
						ObjectOutputStream out;
						baos = new ByteArrayOutputStream();
						try {
							out = new ObjectOutputStream(baos);
							out.writeObject(cart);
							out.close();
						} catch (Exception e) {
							Dbutil.logger.error("Problem:",e);
						}
						byte[] byteObject = baos.toByteArray();
						pstmt.setObject(9, byteObject);
						pstmt.executeUpdate();
						// get the generated key for the id
						rs = pstmt.getGeneratedKeys();
						resultid = -1;
						if (rs.next()) {
							resultid = rs.getLong(1);
							if (cart.status==Shoppingcart.statuses.Ordered){
								int partnerid=Partner.getIDFromShopId(cart.shopid+"");
								if (partnerid==0){
									Dbutil.logger.error("Could not find partnerid for shopid "+cart.shopid);
									throw new Exception();
								} else {
									if (Dbutil.executeQuery("Insert into billingoverview (date,partnerid,shopid,type,referenceid,cpc,invoice) values (now(),"+partnerid+","+cart.shopid+",'orders',"+resultid+","+cart.commission.amount+",0);",con)==0) {
										Dbutil.logger.error("Could not add line for billing of shoppingcart "+cart.cartnumber);
										throw new Exception();
									}
									if (Dbutil.executeQuery("update shoppingcarts set status='Ordered' where tenant=1 and cartnumber="+cart.cartnumber+" and customeruserid='"+Spider.SQLEscape(cart.customeruserid)+"';", con)==0) {
										Dbutil.logger.error("Could not update shoppingcarts");
										throw new Exception();
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
				con.rollback();
				throw e;
			} finally{
				Dbutil.closeRs(rs);
				con.commit();
				Dbutil.closeConnection(con);
			}
			if (resultid==0) throw new UnknownError();
			return resultid;
		}
		return 0;
	}

	public static Shoppingcart readCart(long id, int shopid, int tenant, int cartnumber) throws Exception {
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;
		Shoppingcart object=null;
		byte[] byteObject=null;
		try{
			PreparedStatement pstmt = con.prepareStatement(READ_OBJECT_SQL);
			pstmt.setLong(1, id);
			pstmt.setLong(2, shopid);
			pstmt.setLong(3, tenant);
			pstmt.setLong(4, cartnumber);
			rs = pstmt.executeQuery();
			if (rs.next()){
				byteObject = rs.getBytes("cart");
				ByteArrayInputStream bais;
				ObjectInputStream in;
				bais = new ByteArrayInputStream(byteObject);
				in = new ObjectInputStream(bais);
				object = (Shoppingcart)(in.readObject());
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


