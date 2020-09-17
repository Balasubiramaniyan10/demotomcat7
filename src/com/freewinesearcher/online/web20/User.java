package com.freewinesearcher.online.web20;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.common.Dbutil;

public class User implements Serializable{
	private static final long serialVersionUID = 1L;
	String username;

	public User (HttpServletRequest request) throws NotLoggedInException{
		try {
			username = request.getRemoteUser();
		} catch (NullPointerException ne) {
			throw new NotLoggedInException();

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		if (username==null) throw new NotLoggedInException();

	}
	
	public String getUsername(){
		return username;
	}
	
	public static class NotLoggedInException extends Throwable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
}
