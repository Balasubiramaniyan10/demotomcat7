package com.freewinesearcher.online.web20;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.freewinesearcher.common.Dbutil;

public class Content implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Comment comment=new Comment();
	public Rating rating=new Rating(0);;
	public Video video;
	
	public Content(){
		super();
	}

	public void update(HttpServletRequest request){
		if (ServletFileUpload.isMultipartContent(request)){
			if (ServletFileUpload.isMultipartContent(request)){
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				try {
					List<FileItem> fileItemsList = servletFileUpload.parseRequest(request);
					if (fileItemsList.size()>0){
						for (FileItem fileItem:fileItemsList){
							if (fileItem.getFieldName().equals("comment")){
								comment=new Comment(fileItem.getString());
							} else if (fileItem.getName()!=null&&fileItem.getSize()>100){
								video=new Video(fileItem,request.getSession(true).getId());
							}
						}
					}
				} catch (Exception e) {
					Dbutil.logger.error("Error parsing comments: ",e);
				}
			}
		}
		if (request.getParameter("rating")!=null){
			try {
				rating=new Rating(request.getParameter("rating"));
			} catch (Exception e) {
			}
		}

	}




}
