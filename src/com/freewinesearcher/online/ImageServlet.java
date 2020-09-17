package com.freewinesearcher.online;
/*
 * Deze servlet dient om gegevens uit een BLOB terug om te zetten
 * in een JPG of GIF. Eerste toepassing hiervoor is het afbeelden
 * van de posters die in de CLUIF-databank zitten
 */

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import javax.servlet.ServletException;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;

import java.util.*;
import java.io.File;
import java.lang.Exception;
import java.sql.Blob; 
import java.sql.SQLException; 

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;


/**
 * @author Jasper
 *
 */
public class ImageServlet extends HttpServlet {
	static final long serialVersionUID=5719895;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest _request, HttpServletResponse _response) 
	throws ServletException, IOException   
	{
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		byte[] blobBytesArray = null;
		OutputStream stream = _response.getOutputStream(); 
		HttpSession sessie = _request.getSession(true);

		if (sessie != null){
			int imageid=0;
			FileItem fileItem=(FileItem)sessie.getAttribute("image");
			if (fileItem!=null){
				stream.write(fileItem.get());
			} else {
				try{
					Matcher matcher;
					Pattern pattern;
					pattern=Pattern.compile("/images/gen/(\\d+)");
					matcher=pattern.matcher((String)_request.getRequestURI());
					if (matcher.find()){
						imageid=Integer.valueOf(matcher.group(1));
					}
				} catch (Exception e){}

				if (imageid!=0)	{
					String query="Select * from images where id="+imageid+";";
					rs=Dbutil.selectQuery(query, con);
					String contenttype="";
					Blob imageblob=null;
					try {
						if (rs.next()){
							imageblob=rs.getBlob("data");
							contenttype=rs.getString("contenttype");
							if (contenttype!=null&&!contenttype.equals("")&&!contenttype.startsWith("image/")) contenttype="image/"+contenttype;
						}
						if (imageblob!=null){
							if (contenttype!=null) _response.setContentType(contenttype);
							int len = new Integer( new Long( imageblob.length()).toString() ).intValue();
							blobBytesArray = imageblob.getBytes(1,len);
							if (blobBytesArray != null)
							{
								if (blobBytesArray.length > 0 )
								{
									stream.write(blobBytesArray);
								}

							} /* blobBytesArray != null */
						}
					} catch (Exception e)
					{
						String name = e.getClass().getName();
						if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
							// No action
						} else {
							Dbutil.logger.error("Error while retrieving image. ",e);
						} 

					}



				} else {
					try{
						Matcher matcher;
						Pattern pattern;
						pattern=Pattern.compile("/images/gen/winead/(\\d+)");
						matcher=pattern.matcher((String)_request.getRequestURI());
						if (matcher.find()){
							imageid=Integer.valueOf(matcher.group(1));
						}
					} catch (Exception e){}

					if (imageid!=0)	{
						String query="Select * from wineads where wineid="+imageid+";";
						rs=Dbutil.selectQuery(query, con);
						Blob imageblob=null;
						try {
							if (rs.next()){
								imageblob=rs.getBlob("image");

							}
							if (imageblob!=null){

								int len = new Integer( new Long( imageblob.length()).toString() ).intValue();
								blobBytesArray = imageblob.getBytes(1,len);
								if (blobBytesArray != null)
								{
									if (blobBytesArray.length > 0 )
									{
										stream.write(blobBytesArray);
									}

								} /* blobBytesArray != null */
							}
						} catch (Exception e)
						{
							String name = e.getClass().getName();
							if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
								// No action
							} else {
								Dbutil.logger.error("Error while retrieving image. ",e);
							} 

						}



					} else {
						int knownwineid=0;
						try{
							
							Matcher matcher;
							Pattern pattern;
							pattern=Pattern.compile("/labels/thumb/(\\d+)");
							matcher=pattern.matcher((String)_request.getRequestURI());
							if (matcher.find()){
								knownwineid=Integer.valueOf(matcher.group(1));
							}
						} catch (Exception e){}

						File imageFile=null;
						int height=0;
						int width=16;
						String imageOutput = "jpg";
						if (knownwineid!=0)	{
							
							imageFile=new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".jpg");
							if (!imageFile.exists()){
								imageFile=new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".gif");
								imageOutput = "gif";
							}
						}
						
						if (imageFile!=null&&imageFile.exists()){
								BufferedImage bufferedImage = ImageIO.read(imageFile);
								int calcHeight = height > 0 ? height : (width * bufferedImage.getHeight() / bufferedImage.getWidth());
								ImageIO.write(createResizedCopy(bufferedImage, width, calcHeight), imageOutput, _response.getOutputStream());
						} else {
							imageFile=new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+"default.jpg");
							BufferedImage bufferedImage = ImageIO.read(imageFile);
							ImageIO.write(bufferedImage, "jpg", _response.getOutputStream());
						}
					}
				}
			}

		} /* (sessie != null) */ 

		Dbutil.closeConnection(con);	
	} /* public void service() */
	BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight) {
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

} /* public class ImageServlet */
