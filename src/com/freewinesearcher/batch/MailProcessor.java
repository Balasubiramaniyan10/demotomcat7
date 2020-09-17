package com.freewinesearcher.batch;
import javax.mail.*;
import javax.mail.internet.*;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sun.mail.pop3.POP3SSLStore;

public class MailProcessor {
	static HashMap<String,MailProcessor> accounts=new HashMap<String,MailProcessor>();
	String account;
	Session session;
	Store store;
	Folder folder;
	public Message[] msgs;

	public static void receiveUploadMessages()	{
		MailProcessor mp=MailProcessor.getMail(Configuration.uploadaccount);
		if (mp!=null) try {
			for (Message msg : mp.msgs) {
				saveAttachments(msg);
				mp.deleteMessage(msg);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally {
			mp.close();
		}
		
	}
	
	private void connect() throws MessagingException{
		
			if (!store.isConnected()) store.connect();
			if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
		
	}
	
	public void deleteMessage(Message message){
		try {
			message.setFlag(Flags.Flag.DELETED, true);
		} catch (Exception e) {
			Dbutil.logger.error("Could not delete message", e);
		}
	}
	
	public static MailProcessor getMail(String account){
		MailProcessor mp=null;
		try {
			if (accounts.get(account) != null) {
				mp = accounts.get(account);
			} else {
				mp = new MailProcessor(account);
				MailProcessor.accounts.put(account, mp);
			}
			mp.connect();
			mp.msgs = mp.folder.getMessages();
		} catch (Exception e) {
			Dbutil.logger.warn("Problem getting email connection for account "+account, e);
		}
		return mp;
	}
	
	public void close(){
		try
		{
			if (folder.isOpen()) folder.close(true);
			if (store.isConnected()) store.close();
			
		}
		catch (Exception ex2) 
		{
			Dbutil.logger.error("Error closing email folders and stores: ",ex2);
		}
	}

	private MailProcessor(String account) throws Exception{
		this.account=account;
		// -- Get hold of the default session --
		Properties props = System.getProperties();
		session = Session.getDefaultInstance(props, null);
		// -- Get hold of a POP3 message store, and connect to it --
		URLName urlname=new URLName("pop3", Configuration.emailhost, 995, "", account,Configuration.emailpassword);
		store = new POP3SSLStore(session, urlname);
        store.connect();
     // -- Try to get hold of the default folder --
		folder = store.getDefaultFolder();
		if (folder == null) throw new Exception("No default folder");
		// -- ...and its INBOX --
		folder = folder.getFolder("INBOX");
		if (folder == null) throw new Exception("No POP3 INBOX");
		Dbutil.logger.info("Connected to mailbox for "+account);
		this.close();
		
		
	}

	public static void oldreceiveMessages()
	{
		Store store=null;
		Folder folder=null;
		try
		{
			// -- Get hold of the default session --
			Properties props = System.getProperties();
			Session session = Session.getDefaultInstance(props, null);
			// -- Get hold of a POP3 message store, and connect to it --
			URLName urlname=new URLName("pop3", Configuration.emailhost, 995, "", Configuration.uploadaccount,Configuration.emailpassword);
			store = new POP3SSLStore(session, urlname);
	        store.connect();

			// -- Try to get hold of the default folder --
			folder = store.getDefaultFolder();
			if (folder == null) throw new Exception("No default folder");
			// -- ...and its INBOX --
			folder = folder.getFolder("INBOX");
			if (folder == null) throw new Exception("No POP3 INBOX");
			// -- Open the folder for read only --
			folder.open(Folder.READ_WRITE);
			// -- Get the message wrappers and process them --
			Message[] msgs = folder.getMessages();
			Dbutil.logger.info(msgs.length);
			
		}
		catch (Exception exc)
		{
			Dbutil.logger.error("Error processing email messages: ",exc);
		}
		finally
		{
			// -- Close down nicely --
			try
			{
				if (folder!=null) folder.close(true);
				if (store!=null) store.close();
			}
			catch (Exception ex2) 
			{
				Dbutil.logger.error("Error closing email folders and stores: ",ex2);
			}
			
		}
		
	}
	
	
	public static void saveAttachments(Message message){
		try{
			if (message!=null){
				String subject=message.getSubject();
				String shopid="";
				String code="";
				boolean authorized=false;
				int hash=0;
				if (subject.contains("Shop=")){
					shopid=subject.substring(subject.indexOf("Shop=")+5);
					if (shopid.contains("&")){
						shopid=shopid.substring(0,shopid.indexOf("&"));
					} else{
						shopid="";
					}
					if (subject.contains("Code=")){
						code=subject.substring(subject.indexOf("Code=")+5);
					}
					hash=Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode();
					if (code.equals(Integer.toString(hash))) {
						authorized=true;
					} else {
						shopid="";
					}
				}
				String domain=Webroutines.getRegexPatternValue("@(.*)>", message.getFrom()[0].toString());
				int id=0;
				id=Dbutil.readIntValueFromDB("Select * from shops where shopurl like 'http://"+domain+"' or shopurl like 'http://www."+domain+"';", "id");
				if (id>0){
					shopid=id+"";
					authorized=true;
				}
				
				if (authorized){

					Multipart mp = (Multipart)message.getContent();

					for (int i=0, n=mp.getCount(); i<n; i++) {
						Part part = mp.getBodyPart(i);

						String disposition = part.getDisposition();

						if ((disposition != null) && 
								((disposition.equals(Part.ATTACHMENT) || 
										(disposition.equals(Part.INLINE))))) {
							String filename=part.getFileName();
							if (filename.contains("\\")){
								filename=filename.substring(filename.lastIndexOf("\\")+1);
							}
							if (filename.contains("/")){
								filename=filename.substring(filename.lastIndexOf("/")+1);
							}
							if (saveFile(Configuration.emaildir+shopid+"\\"+filename, part.getInputStream())){
								// Succesfully saved all attachments, now delete the message
								message.setFlag(Flags.Flag.DELETED, true);
							}
						}
					}
				} else {
					Dbutil.logger.error("Received an email with non matching shop info and hash code: "+subject);
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Error while saving attachment. ",exc);
		}
	}

	public static boolean saveFile(String filename, InputStream is){
		File file = new File(filename);
		File dir = new File(file.getParent());
		if(!dir.exists()){
			boolean success = dir.mkdir();
			if (!success) {
				Dbutil.logger.error("Could not create directory "+file.getParent());
				return false;
			}
		}file = new File(filename);
		BufferedOutputStream fOut = null;
		try
		{
			fOut = new BufferedOutputStream(new FileOutputStream(file));
			byte[] buffer = new byte[32 * 1024];
			int bytesRead = 0;
			while ((bytesRead = is.read(buffer)) != -1)
			{
				fOut.write(buffer, 0, bytesRead);

			}
			fOut.close();
		}
		catch (Exception e)
		{
			Dbutil.logger.error("Error saving to file "+filename,e);
			return false;
		}
		
		return true;
	}

	public static void notifyToUpload(){
		int numberofdays=Configuration.graceperiod-7;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		
		try {
			String query="select * from shops where urltype='Email' and succes="+numberofdays+";";
			//query="select * from shops where id=4";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int shopid=rs.getInt("id");
				String mailto=rs.getString("email");
				//shopid=4;
				//mailto="jasper.hammink@freewinesearcher.com";
				String message="<html><body><div style='font-family:Arial;font-size:10pt;color:blue;'>Hi, <br/><br/>It has been "+numberofdays+" days since you have uploaded a price list of your wines to Vinopedia. "+
				"In order to keep our prices up to date, we require you to send an up-to-date price list at least every "+Configuration.graceperiod+" days. If an update is not received within a week, all wines are being removed from our database.<br/>" +
				"To prevent removal please send an updated wine list to <a href='mailto:upload@vinopedia.com?subject=Shop="+shopid+"%26Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"'>upload@vinopedia.com</a> with subject \"Shop="+shopid+"&amp;Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"\". This is an automatically generated message, do not reply. For inquiries or to report problems please contact jasper@vinopedia.com<br/><br/>Kind regards,<br/><br/>Jasper Hammink,<br/>Vinopedia.com</div></html></body>";
				String subject="Reminder: Upload price list to Vinopedia";
				Dbutil.logger.info("Sending reminder for update of winelist for shop "+shopid+" to "+mailto);
				Emailer emailer=new Emailer();
				emailer.sendEmail("\"Vinopedia\" <do_not_reply@vinopedia.com>",mailto, subject, message);

				
			}
			query="select * from shops where urltype='Email' and succes="+(Configuration.graceperiod+1)+";";
			//query="select * from shops where id=4";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int shopid=rs.getInt("id");
				String mailto=rs.getString("email");
				//shopid=4;
				//mailto="jasper.hammink@freewinesearcher.com";
				String message="<html><body><div style='font-family:Arial;font-size:10pt;color:blue;'>Hi, <br/><br/>It has been "+Configuration.graceperiod+" days since you have uploaded a price list of your wines to Vinopedia. "+
				"In order to keep our prices up to date, we require you to send an up-to-date price list at least every "+Configuration.graceperiod+" days. As a result, all your wines have been removed from our database.<br/>" +
				"To add your wines again, please send an updated wine list to <a href='mailto:upload@vinopedia.com?subject=Shop="+shopid+"%26Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"'>upload@vinopedia.com</a> with subject \"Shop="+shopid+"&amp;Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"\". This is an automatically generated message, do not reply. For inquiries or to report problems please contact jasper@vinopedia.com<br/><br/>Kind regards,<br/><br/>Jasper Hammink,<br/>Vinopedia.com</div></html></body>";
				String subject="Notification: Wines removed from Vinopedia.com";
				Dbutil.logger.info("Sending notification for removal of winelist for shop "+shopid+" to "+mailto);
				Emailer emailer=new Emailer();
				emailer.sendEmail("\"Vinopedia\" <do_not_reply@vinopedia.com>",mailto, subject, message);

				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Dbutil.logger.error("Problem while notifying seller of outdated wine list.", e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		
	}

}
