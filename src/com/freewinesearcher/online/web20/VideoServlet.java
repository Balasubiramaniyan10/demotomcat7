package com.freewinesearcher.online.web20;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jcifs.smb.SmbFile;

import com.freewinesearcher.common.Configuration;

public class VideoServlet extends HttpServlet {
	static final long serialVersionUID=5719895;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest _request, HttpServletResponse _response) 
	throws ServletException, IOException   
	{
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile("/video/([^.]+)(\\.|$)");
		matcher=pattern.matcher((String)_request.getRequestURI());
		if (matcher.find()){
			String tempfilename=matcher.group(1);
			if (tempfilename.startsWith("t")) {
				_response.setContentType(_request.getParameter("content-type"));
				tempfilename=Configuration.videodir+"temp/"+tempfilename;
			} else {
				Publication pub=Publication.get(Integer.parseInt(tempfilename));
				tempfilename=Configuration.videodir+"/"+pub.content.video.filename;
				_response.setContentType(pub.content.video.contenttype);
			}
				
			OutputStream stream = _response.getOutputStream(); 
			SmbFile file=new SmbFile(tempfilename);
			InputStream in=file.getInputStream();
			byte buf[]=new byte[1024];
			int len;
			while((len=in.read(buf))>0)
				stream.write(buf);
			in.close();
		}
	}
}
