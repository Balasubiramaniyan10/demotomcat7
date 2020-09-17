package com.freewinesearcher.online;
import java.io.OutputStream;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.sun.mail.iap.ByteArray;
public class BottesterServlet extends HttpServlet {

	
	public void service(HttpServletRequest request, HttpServletResponse response) {
		try{
			Bottester bottester=Bottester.getBottester(request);
			OutputStream os = response.getOutputStream();
			if (request.getParameter("state")!=null) bottester.state=Integer.parseInt(request.getParameter("state"));
			bottester.writeOutput(request,response,os);
			
			os.close();
		} catch (Exception e) {
			response.setStatus(500);
			Dbutil.logger.error("Problem: ", e);
		}
	}
	
	
}

