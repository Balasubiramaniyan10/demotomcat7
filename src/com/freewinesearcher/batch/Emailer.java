/*
 * Created on 15-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.batch;

import java.util.*;
import java.io.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;

import javax.mail.internet.*;

import jcifs.smb.SmbFile;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.invoices.SmbDataSource;

import de.agitos.dkim.*;

/**
 * Simple use case for the javax.mail API.
 */
public final class Emailer {
	public String replyto = "";
	String aFromEmailAddr;
	String aToEmailAddr;
	String aSubject;
	String aBody;
	public ArrayList<String> cc = new ArrayList<String>();
	public ArrayList<String> bcc = new ArrayList<String>();
	SmbFile fileAttachment;
	public Map<String, DataSource> images = null;

	/**
	 * Send a single email with html body.
	 */
	public boolean sendEmail(String aFromEmailAddr, String aToEmailAddr, String aSubject, String aBody) {
		this.aFromEmailAddr = aFromEmailAddr;
		this.aToEmailAddr = aToEmailAddr;
		this.aSubject = aSubject;
		this.aBody = aBody;
		return sendEmail();
	}

	/**
	 * Send a single email with attachment.
	 */
	public boolean sendEmail(String aFromEmailAddr, String aToEmailAddr, String aSubject, String aBody,
			SmbFile fileAttachment) {
		this.aFromEmailAddr = aFromEmailAddr;
		this.aToEmailAddr = aToEmailAddr;
		this.aSubject = aSubject;
		this.aBody = aBody;
		this.fileAttachment = fileAttachment;
		return sendEmail();
	}

	public boolean sendEmail() {
		// BUG WORKAROUND: prd can't send mail
		// javax.mail.NoSuchProviderException: Invalid protocol: null at
		// com.freewinesearcher.batch.Emailer.sendEmail(Emailer.java:98)
		// Here, no Authenticator argument is used (it is null).
		// Authenticators are used to prompt the user for user
		// name and password.
		boolean success = false;
		if (true || !Configuration.serverrole.equals("DEV")) {
			refreshConfig();
			Properties p = (Properties) fMailServerConfig.clone();
			// if (!replyto.equals("")) {
			// p.put("mail.smtp.from", aFromEmailAddr);
			// }
			final String username = fMailServerConfig.getProperty("mail.smtp.user");
			final String password = fMailServerConfig.getProperty("mail.smtp.pwd");
			Session session = Session.getDefaultInstance(p, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			try {
				Message message = null;
				// System.out.print("Emailer properties:
				// "+fMailServerConfig.toString());
				if (aFromEmailAddr != null && aFromEmailAddr.endsWith("@vinopedia.com")) {
					// DKIMSigner dkimSigner = new
					// DKIMSigner(fMailServerConfig.getProperty("mail.smtp.dkim.signingdomain"),fMailServerConfig.getProperty("mail.smtp.dkim.selector"),fMailServerConfig.getProperty("mail.smtp.dkim.privatekey")
					// );
					// dkimSigner.setIdentity(aFromEmailAddr);
					// message = new SMTPDKIMMessage(session, dkimSigner);
					message = new MimeMessage(session);
				} else {
					message = new MimeMessage(session);
				}
				// MimeMessage message = new MimeMessage( session );
				// Dbutil.logger.info("Host in properties during send:
				// "+fMailServerConfig.getProperty("mail.host"));

				// the "from" address may be set in code, or set in the
				// config file under "mail.from" ; here, the latter style is
				// used
				// message.setFrom( new InternetAddress(aFromEmailAddr) );
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
				if (cc != null && cc.size() > 0) {
					for (String rec : cc) {
						message.addRecipient(Message.RecipientType.CC, new InternetAddress(rec));
					}
				}
				if (bcc != null && bcc.size() > 0) {
					for (String rec : bcc) {
						message.addRecipient(Message.RecipientType.BCC, new InternetAddress(rec));
					}
				}
				message.setSubject(aSubject);
				message.setHeader("Sender", aFromEmailAddr);
				message.setFrom(new InternetAddress(aFromEmailAddr, aFromEmailAddr));
				if (!replyto.equals("")) {
					message.setFrom(new InternetAddress(replyto, replyto));
					message.setReplyTo(new Address[] { new InternetAddress(replyto, replyto) });
				}

				// Now the message body.
				Multipart mp = new MimeMultipart();

				BodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(aBody, "text/html; charset=iso-8859-1");
				// Collect the Parts into the MultiPart
				mp.addBodyPart(htmlPart);

				if (fileAttachment != null) {
					// Part two is attachment
					DataSource source = new SmbDataSource(fileAttachment);
					BodyPart att = new MimeBodyPart();
					att.setHeader("Content-Type", "application/pdf");
					att.setHeader("Content-Transfer-Encoding", "base64");
					// specify the binary object as an attachment
					att.setHeader("Content-Disposition", "attachment");
					// define the name of the file--this is what the filename
					// will be in the e-mail client

					att.setDataHandler(new DataHandler(source));
					att.setFileName(fileAttachment.getName());
					mp.addBodyPart(att);
					message.setContent(mp);
				} else {
					if (images == null) {
						message.setContent(aBody, "text/html; charset=iso-8859-1");
					} else {
						MimeMultipart multipart = new MimeMultipart("related");

						// first part (the html)
						BodyPart messageBodyPart = new MimeBodyPart();
						messageBodyPart.setContent(aBody, "text/html; charset=utf-8");
						multipart.addBodyPart(messageBodyPart);

						// add images
						for (String cid : images.keySet()) {
							messageBodyPart = new MimeBodyPart();
							messageBodyPart.setDataHandler(new DataHandler(images.get(cid)));
							messageBodyPart.addHeader("Content-ID", "<" + cid + ">");
							// add it
							multipart.addBodyPart(messageBodyPart);
						}
						message.setContent(multipart);
					}
				}

				// Put the MultiPart into the Message

				// Transport transport = session.getTransport();
				// transport.connect(fMailServerConfig.getProperty("mail.smtps.host"),
				// Integer.parseInt(fMailServerConfig.getProperty("mail.smtps.port")),
				// fMailServerConfig.getProperty("mail.from"),
				// fMailServerConfig.getProperty("mail.password"));
				Transport.send(message);
				// transport.sendMessage(message,
				// message.getRecipients(Message.RecipientType.TO));
				// transport.close();
				success = true;

			} catch (Exception ex) {
				Dbutil.logger.error("Cannot send email to " + aToEmailAddr, ex);
				Dbutil.logger.error("Email subject: " + aSubject);
				Dbutil.logger.error("Email body: " + aBody);
				Dbutil.logger.info(fMailServerConfig);
			}
		}
		// success=true;
		return success;
	}

	/**
	 * Allows the config to be refreshed at runtime, instead of requiring a
	 * restart.
	 */
	public static void refreshConfig() {
		fMailServerConfig.clear();
		fMailServerConfig = new Properties();
		fetchConfig();

	}
	// PRIVATE //

	public static Properties fMailServerConfig = new Properties();

	static {
		fetchConfig();

	}

	/**
	 * Open a specific text file containing mail server parameters, and populate
	 * a corresponding Properties object.
	 */
	private static void fetchConfig() {
		InputStream input = null;
		try {
			// If possible, one should try to avoid hard-coding a path in this
			// manner; in a web application, one should place such a file in
			// WEB-INF, and access it using ServletContext.getResourceAsStream.
			// Another alternative is Class.getResourceAsStream.
			// This file contains the javax.mail config properties mentioned
			// above.
			String configfile = Configuration.basedir + "WEB-INF" + System.getProperty("file.separator")
					+ "classes" +  System.getProperty("file.separator");
			
			if (Configuration.serverrole.equals("DEV")) {
				configfile += "Emailer.DEV.properties";
			} else {
				configfile += "Emailer.properties";

			}
			// Dbutil.logger.info("email configfile: "+configfile); We cannot
			// use this yet: the logger itself needs these properties
			input = new FileInputStream(configfile);
			fMailServerConfig.load(input);
			// Dbutil.logger.info("host in properties:
			// "+fMailServerConfig.getProperty("mail.host"));

		} catch (Exception ex) {
			Dbutil.logger.error("Cannot open and load mail server properties file.", ex);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ex) {
				Dbutil.logger.error("Cannot close mail server properties file.");
			}
		}
	}

	static class MyPasswordAuthenticator extends Authenticator {
		String user;
		String pw;

		public MyPasswordAuthenticator(String username, String password) {
			super();
			this.user = username;
			this.pw = password;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pw);
		}
	}
}
