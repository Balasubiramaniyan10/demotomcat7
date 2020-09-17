package com.freewinesearcher.online.web20;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.SmbFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.freewinesearcher.common.Configuration;


public class Video  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Exception IllegalVideoFormatException=new Exception();
	public static Exception NoFileException=new Exception();

	String extension;
	String contenttype;
	public String filename;
	public String tempfilename;
	public static final String videodir="/video/";
	public static final String tempvideodir="temp/";


	public Video(FileItem fileItem,String tempfilename) throws Exception{
		// If a file was uploaded, save it
		try{
			if (fileItem.getName()!=null){
				extension="";
				try {extension=fileItem.getName().substring(fileItem.getName().lastIndexOf(".")+1);} catch (Exception e){};
				contenttype=fileItem.getContentType();
				if (contenttype.toLowerCase().startsWith("video")){
					filename="t"+tempfilename;
					tempfilename=Configuration.videodir+tempvideodir+filename;
					SmbFile file=new SmbFile(tempfilename);
					InputStream in=fileItem.getInputStream();
					writeStreamToSmbFile(in, file);
					in.close();
				} else {
					throw IllegalVideoFormatException;
				}
			}
		} catch (Exception e){
			throw IllegalVideoFormatException;
		}
	}

	public static void writeStreamToSmbFile(InputStream in,SmbFile file) throws IOException{
		OutputStream out = file.getOutputStream();
		byte buf[]=new byte[1024];
		int len;
		while((len=in.read(buf))>0)
			out.write(buf,0,len);
		out.close();
	}

	public String getVideoHTML(){
		String html="";
		/*
		html+="<a href='http://www.macromedia.com/go/getflashplayer'>Get the Flash Player</a> to see this player.</div><script type='text/javascript' src='swfobject.js'></script><script type='text/javascript'>var s1 = new SWFObject('mediaplayer.swf','mediaplayer','500','450','8');s1.addParam('allowfullscreen','true');s1.addVariable('width','500');s1.addVariable('height','450');";
		if (filename!=null) {
			html+="s1.addVariable('file','"+videodir+filename+"');";
		} else {
			html+="s1.addVariable('file','"+tempvideodir+tempfilename+"');";
		}
		html+="s1.write('container');</script>";
		html="<OBJECT id='VIDEO' WIDTH='720' HEIGHT='480' style='position:absolute; left:0;top:0;'	CLASSID='CLSID:6BF52A52-394A-11d3-B153-00C04F79FAA6' type='application/x-oleobject'> <PARAM NAME='URL' VALUE='"+videodir+filename+"'><PARAM NAME='SendPlayStateChangeEvents' VALUE='True'><PARAM NAME='AutoStart' VALUE='True'><PARAM name='uiMode' value='none'><PARAM name='PlayCount' value='9999'></OBJECT>";
		html="<embed src='"+videodir+filename+"?content-type="+contenttype+"' autostart='false' />";
		 */

		html+="<object type='"+contenttype+"' width='640' height='480'><param name='src' value='"+videodir+filename+"?content-type="+contenttype+"'><param name='autoplay' value='false'><param name='autoStart' value='0'></object>";

		return html;
	}


}
