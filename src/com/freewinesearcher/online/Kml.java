package com.freewinesearcher.online;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public class Kml  extends HttpServlet {
	
	//private static Map<String,Producers> data=new HashMap<String,Producers>();
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException   {
		String output=request.getParameter("output");
		//if (output!=null&&output.equals("list")){
			//response.setContentType("text/html");
			
			//Producers producers=data.get(request.getSession().getId());
			//if (producers!=null){
				//Dbutil.logger.info("Read: "+producers.producer.size());
				
				//response.getOutputStream().write(producers.getAsHtml().toString().getBytes("UTF-8"));
				//data.put(request.getSession().getId(),null);
		//	}
		//} else {
			Bounds bounds=new Bounds();
			HttpSession session = request.getSession();
			String param=(String)request.getParameter("BBOX");
			if (param!=null){
				String[] params = param.split(",");
				bounds.latmin=Double.parseDouble(params[1]);
				bounds.latmax=Double.parseDouble(params[3]);
				bounds.lonmin=Double.parseDouble(params[0]);
				bounds.lonmax=Double.parseDouble(params[2]);
				session.setAttribute("latmin", bounds.latmin);
				session.setAttribute("latmax", bounds.latmax);
				session.setAttribute("lonmin", bounds.lonmin);
				session.setAttribute("lonmax", bounds.lonmax);
			} 
			Producers producers=new Producers(bounds);
			Regionpois regionpois=new Regionpois(bounds,25);
			//data.put(Webroutines.getRegexPatternValue("/KML/(.*)", (String)request.getAttribute("originalURL")), producers);
			//Dbutil.logger.info("Write: "+producers.producer.size()+" "+(bounds.latmax-bounds.latmin));
			//Dbutil.logger.info(Webroutines.getRegexPatternValue("/KML/(.*)", (String)request.getAttribute("originalURL"))+data.get(Webroutines.getRegexPatternValue("/KML/(.*)", (String)request.getAttribute("originalURL"))));
			//Dbutil.logger.info(Webroutines.getRegexPatternValue("/KML/(.*)", (String)request.getAttribute("originalURL"))+producers+" "+producers.numberofproducers+": "+bounds.latmin+": "+bounds.latmax+": "+bounds.lonmin+": "+bounds.lonmax);

			if (false){
				response.setContentType("application/vnd.google-earth.kmz");
				ZipEntry entry = new ZipEntry("data.kml");
				ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
				zipOutputStream.setLevel(9);
				zipOutputStream.putNextEntry(entry);
				zipOutputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>".getBytes("UTF-8"));
				//zipOutputStream.write(("<Style id=\"noPlacemark\"><IconStyle><Icon><href>"+Configuration.staticprefix+"/images/smallgreendot32.gif</href></Icon></IconStyle></Style>").getBytes("UTF-8"));
				zipOutputStream.write(("<Style id=\"normalPlacemark\"><IconStyle><Icon><href>"+Configuration.staticprefix+"/images/smallreddot32.gif</href></Icon></IconStyle></Style>").getBytes("UTF-8"));
				zipOutputStream.write(producers.getAsKml().toString().getBytes("UTF-8"));
				//zipOutputStream.write(regionpois.getAsKml().toString().getBytes("UTF-8"));
				zipOutputStream.write("</Document></kml>".getBytes("UTF-8"));
				zipOutputStream.closeEntry();
				zipOutputStream.close();

			} else {
				//response.setContentType("application/vnd.google-earth.kml+xml");
				//response.getOutputStream().write(producers.getAsKml().toString().getBytes("UTF-8"));
				response.setContentType("application/json");
				response.getOutputStream().write("[".getBytes("UTF-8"));
				response.getOutputStream().write(regionpois.getAsJSON().getBytes("UTF-8"));
				response.getOutputStream().write((",\"producers\":"+producers.getAsJSON()).getBytes("UTF-8"));
				response.getOutputStream().write("}]".getBytes("UTF-8"));
				
			}
		}
	//}
}
