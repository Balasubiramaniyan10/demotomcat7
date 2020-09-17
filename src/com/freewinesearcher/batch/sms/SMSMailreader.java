package com.freewinesearcher.batch.sms;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.internet.*;

import com.freewinesearcher.batch.MailProcessor;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;



public class SMSMailreader implements Runnable {
	private static SMSMailreader instance=null;
	private static boolean muststop=false;

	private SMSMailreader(){
		ThreadGroup threadgroup = new ThreadGroup("SMSMailreader");
		new Thread(threadgroup,this).start();
	}

	public static SMSMailreader getInstance(){
		if(instance == null) {
			instance = new SMSMailreader();
		}
		return instance;
	}

	public void run(){
		Dbutil.logger.info("Starting SMS mail reader");
		MailProcessor mp=null;
		while (!muststop){
			try	{
				mp=MailProcessor.getMail(Configuration.smsmailaccount);
				if (mp!=null){
					for (Message msg:mp.msgs)
					{
						String body=getMessageasString(msg);
						Sms smssubject=null;
						if (!msg.getSubject().equals("")) {
							smssubject=new Sms(Configuration.gmailusername,Configuration.gmailpassword,msg.getSubject());
						}
						Sms smsbody=new Sms(Configuration.gmailusername,Configuration.gmailpassword,body);
						if (true){
							try{
								if ((smssubject==null||smssubject.send())&&smsbody.send())	{
									Dbutil.logger.info("SMS message sent. ");
									mp.deleteMessage(msg);
								} else {
									Dbutil.logger.error("Error processing email message. ");
									mp.deleteMessage(msg);						}
							}catch (Exception e){
								Dbutil.logger.error("Error processing email message. ");
								mp.deleteMessage(msg);

							}
						}
					}
				}

			}
			catch (Exception exc)
			{
				Dbutil.logger.error("Error processing email messages: ",exc);
			} finally {
				if (mp!=null) mp.close();
			}

			try{
				Thread.sleep(60*1000);
			} catch (Exception e){}
		}
	}

	public void stop(){
		muststop=true;
	}

	public String getMessageasString( Message pMessage ) {                                 
		String body="";  
		// There exists some parameter pMessage                                     
		// which is a javax.mail.Message                                            
		try {                                                                       
			// Try and grab the unknown content                                       
			Object content = pMessage.getContent();                                   

			// Grab the body content text                                             
			if ( content instanceof String ) {                                        
				body=(String)content;                                        
			} else if ( content instanceof javax.mail.Multipart ) {                              
				// Make sure to cast to it's Multipart derivative                       
				body+=parseMultipart( (Multipart) content );                                  
			}                                                                         
		}catch (Exception e ) {                                           
			Dbutil.logger.warn(" ",e);                                                      
		}  
		return body;
	}                                                                             

	//		 Parse the Multipart to find the body                                       
	public String parseMultipart( Multipart mPart ) {                               
		String text="";
		try{
			// Loop through all of the BodyPart's                                       
			for ( int i = 0; i < mPart.getCount(); i++ ) {                              
				// Grab the body part                                                     
				BodyPart bp = mPart.getBodyPart( i );                                     
				// Grab the disposition for attachments                                   
				String disposition = bp.getDisposition();                              

				// It's not an attachment                                                 
				if ( disposition == null && bp instanceof MimeBodyPart ){                 
					MimeBodyPart mbp = (MimeBodyPart) bp;                                   

					// Check to see if we're in the screwy situation where                  
					// the message text is buried in another Multipart                      
					if ( mbp.getContent() instanceof Multipart ) {                          
						// Use recursion to parse the sub-Multipart                           
						text+=parseMultipart( (Multipart) mbp.getContent() );                       
					} else {                                                                
						// Time to grab and edit the body                                     
						if ( mbp.isMimeType( "text/plain" )) {                                 
							// Grab the body containing the text version                        
							text+=(String) mbp.getContent();                            

						} else if ( mbp.isMimeType( "text/html" )) {                           
							// Grab the body containing the HTML version                        
							text+=(String) mbp.getContent();                            

						}                                                                     
					}                                                                       
				}                                                                         
			}                                                                           
		} catch (Exception e){}
		return text;
	}


}
