package com.vinopedia.affiliates;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import javax.servlet.ServletException;

import java.io.*;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Shop;
import com.freewinesearcher.online.Webroutines;

import java.lang.Exception;
import java.sql.*;


/**
 * @author Jasper
 *
 */


public class Affiliatetracker extends HttpServlet {
	static final long serialVersionUID=5719895;

	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException   	{
		OutputStream stream = response.getOutputStream(); 
		HttpSession sessie = request.getSession(true);
		String url=(String)request.getRequestURL().toString()+"?"+(String)request.getAttribute("originalQueryString");
		if (sessie != null&&url!=null){
			try{
				int shopid=0;
				String store=request.getParameter("store");
				if (store!=null&&!store.equals(""))	try{shopid=Integer.parseInt(store);}catch(Exception e){}
				if (shopid>0){
					Cookie[] cookies = request.getCookies();
					String cookiedate="";
					for (Cookie c:cookies){
						if (shopid>0&&c.getName().equals("S"+store)){
							cookiedate=Webroutines.URLDecodeUTF8(c.getValue());

						}
					}
					if (!cookiedate.equals("")){
						Dbutil.logger.info(cookiedate);
						Shop shop=new Shop(shopid);
						if (shop.costperclick>0){
							Sale sale=new Sale();
							sale.setShopid(shopid);
							sale.setAmount(request.getParameter("amount"));
							sale.setReference(request.getParameter("reference"));
							sale.setCurrency(request.getParameter("currency"));
							sale.setCommissionpercentage(shop.costperclick);
							sale.setCookiedate(Timestamp.valueOf(cookiedate));
							sale.setIp(request.getRemoteAddr());
							sale.setHostcountry(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
							sale.setRequesturl(url);
							sale.add();
						}
					}	

				}
			} catch (Exception e){
				String name = e.getClass().getName();
				if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
					// No action
				} else {
					Dbutil.logger.error("Error while saving sale with url "+url,e);
				} 

			}
		}
		stream.write(getPixel());
	}

	public static byte[] getPixel() {
		String s="47494638396101000100800000FFFFFF00000021F90401000000002C00000000010001000002024401003B";
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}
