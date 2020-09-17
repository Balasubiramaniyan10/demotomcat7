package com.freewinesearcher.online.web20;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.SmbFile;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.web20.Subject.Types;
import com.freewinesearcher.online.web20.User.NotLoggedInException;


public class Publication implements Serializable{

	private static final long serialVersionUID = 1L;
	public long id;
	public User user;
	public Subject subject;
	public long uploadtime=new java.util.Date().getTime();
	public Content content=new Content();

	public Publication(){
		super();
	}

	public static Publication get(int id){
		Publication pub=null;
		try {
			pub= (Publication) Serializer.readJavaObject("publications", 1, id, "publication");
		} catch (Exception e) {
			Dbutil.logger.error("Could not retrieve publication with id="+id+" from the database",e);
		}
		if (pub==null) Dbutil.logger.error("Could not retrieve publication with id="+id+" from the database");
		return pub;
	}

	public void update(HttpServletRequest request){
		uploadtime=new java.util.Date().getTime(); 
		try{
			this.user=new User(request);
		} catch (NotLoggedInException e){
		}
		try{
			this.content.update(request);
		} catch (Exception e){
		}
		try{
			if (request.getParameter("id")!=null){
				int id=Integer.parseInt(request.getParameter("id"));
				Subject.Types type=Subject.Types.valueOf(request.getParameter("type").toUpperCase());
				if (type==Types.WINE){
					int vintage=0;
					try{ 
						vintage=Integer.parseInt(request.getParameter("vintage"));
					} catch (Exception e){}
					subject=new WineSubject(id,vintage);
				} else {
					subject=new Subject(type,id);
				}

			}

		} catch (Exception e){
		}


	}

	public boolean isValid(HttpServletRequest request){
		if (user==null) try{
			this.user=new User(request);
		} catch (NotLoggedInException e){
		}
		return (user!=null&&subject!=null&&hasContent());
	}

	public boolean save(){
		uploadtime=new java.util.Date().getTime(); 
		boolean success=false;
		HashMap<String,Object> map=new HashMap<String, Object>();
		map.put("type", subject.type+"");
		map.put("refid", subject.rowid);
		map.put("userid", user.username);
		map.put("date",new java.sql.Timestamp(new java.util.Date().getTime()));
		map.put("publication", this);
		if (subject.type==Types.WINE){
			map.put("vintage", ((WineSubject)subject).vintage);
		}
		try {
			long rowid=Serializer.writeJavaObject("publications", 1, id, this.serialVersionUID, map);
			if (content.video!=null&&content.video.filename!=null&&content.video.filename.startsWith("t")){
				SmbFile file = new SmbFile(Configuration.videodir+"temp/"+content.video.filename);
				InputStream in=file.getInputStream();
				SmbFile target=new SmbFile(Configuration.videodir+"/"+rowid+"."+content.video.extension);
				Video.writeStreamToSmbFile(in, target);
				in.close();
				file.delete();
				content.video.filename=rowid+"."+content.video.extension;
				this.id=rowid;
				map.put("publication", this);
				if (Serializer.writeJavaObject("publications", 1, this.id, this.serialVersionUID, map)>0) success=true;
			} else if (rowid>0) success=true;
			this.id=rowid;

		} catch (Exception e) {
			Dbutil.logger.error("Could not store publication in database for user "+user.username,e);
			Dbutil.logger.error(map);
			return false;
		}
		return success;

	}

	public boolean hasContent() {
		if (content!=null){
			if (content.comment!=null&&content.comment.hasContent()) return true;
			if (content.video!=null) return true;
			if (content.rating!=null&&content.rating.rating>0) return true;
		}
		return false;
	}

	public String getAllContent(HttpServletRequest request){
		String html="";
		String edithtml="";
		if (user!=null&&user.username!=null&&user.username.equals(request.getRemoteUser())){
			edithtml="&nbsp;<a href='"+request.getRequestURI()+"?editcomment="+id+"#comment'>[Edit]</a>";
		}
		if (content!=null){
			if (content.video!=null) {
				html+="Video uploaded by "+(user!=null?user.username:"")+" on "+DateFormat.getDateTimeInstance().format(uploadtime)+edithtml+"<br/>";
				if (content.comment!=null&&content.comment.hasContent()) html+="\""+content.comment.getComment()+"\"<br/>";
				if (content.rating!=null) html+=content.rating.getHTML(false)+"<br/>";
				html+=content.video.getVideoHTML()+"<br/><br/>";
			} else {
				if (content.comment!=null&&content.comment.hasContent()) {
					html+=(user==null?"Anonymous":user.username)+" said on "+DateFormat.getDateTimeInstance().format(uploadtime)+":"+edithtml;
					if (content.rating!=null) html+=content.rating.getHTML(false)+"";
					html+="\""+content.comment.getComment()+"\"<br/><br/>";				
				} else {
					if (content.rating!=null) {
						html+=(user==null?"Anonymous":user.username)+" said on "+DateFormat.getDateTimeInstance().format(uploadtime)+":"+edithtml;
						html+=content.rating.getHTML(false)+"<br/><br/>";
					}
				}
			}
		}
		return html;
	}

}
