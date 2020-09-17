package com.freewinesearcher.batch.sms;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.Content;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.extensions.EventEntry;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;


public class Sms {
	String userName=Configuration.gmailusername;
	String userPassword=Configuration.gmailpassword;
	String sms;
	int minutesahead=0; // Send directly


	public Sms(){
	}
	public Sms(String message){
		sms=message;
		send();
	}
	
	public Sms(String userName, String userPassword,String sms) {
		this.userName=userName;
		this.userPassword=userPassword;
		this.sms=sms;
	}

	public boolean send(){
		boolean succes=false;
		if (sms.equals("")||sms.contains("Could not send SMS")||sms.contains("Error processing email message.")) {
			succes=true;
		} else {
			try {
				URL feedUrl = new URL("http://www.google.com/calendar/feeds/" + userName + "/private/full");

				CalendarService myService = new CalendarService("FWSSMS");

				// Nos autenticamos en google Calendar
				myService.setUserCredentials(userName, userPassword);

				// Creamos el evento
				EventEntry myEntry = new EventEntry();
				myEntry.setTitle(new PlainTextConstruct(sms));
				if (sms.length()>57){
					myEntry.addLocation(new Where("","",sms.substring(57)));
				}

				Person author = new Person(userName, null, userName);
				myEntry.getAuthors().add(author);

				// Definimos la zona horaria
				TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

				Calendar cal = GregorianCalendar.getInstance();
				cal.add(Calendar.MINUTE, minutesahead+5);
				DateTime startTime = new DateTime(cal.getTime(), tz); //8m

				cal.add(Calendar.MINUTE, 1);
				DateTime endTime = new DateTime(cal.getTime(), tz); //9m 

				// Definimos la hora de comienzo y de fin del evento         
				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				eventTimes.setEndTime(endTime);
				myEntry.addTime(eventTimes);

				// A�adimos el recordatorio s�lo como SMS y que avise 5 minutos antes
				Reminder reminder = new Reminder();
				reminder.setMinutes(new Integer(5));
				reminder.setMethod(Reminder.Method.SMS);
				myEntry.getReminder().add(reminder);

				// Enviamos la petici�n para insertar el evento en el calendario
				EventEntry insertedEntry = (EventEntry)myService.insert(feedUrl, myEntry);
				succes=true;
				Dbutil.logger.info("Sent SMS for time "+startTime.toStringRfc822());
			} catch (Exception e) {
				Dbutil.logger.error("Could not send SMS. ",e);
			}
		}
		return succes;
	}

	public int getMinutesahead() {
		return minutesahead;
	}

	public void setMinutesahead(int minutesahead) {
		this.minutesahead = minutesahead;
	}

	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

}