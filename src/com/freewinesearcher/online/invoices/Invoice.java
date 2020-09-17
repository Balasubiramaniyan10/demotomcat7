package com.freewinesearcher.online.invoices;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.text.*;

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import com.freewinesearcher.batch.Emailer;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Auditlogger;
import com.freewinesearcher.online.Partner;
import com.freewinesearcher.online.Webroutines;



/**
 * @author Jasper
 *
 */
public class Invoice {
	public int id=0;
	//private int shopid;
	int partnerid;
	private int clicks;
	private double amountex;
	private double vat;
	private double totalamount;
	private Date since;
	private Date createdate;
	private Date duedate;
	private Date paydate;
	public static enum statusses {open,paid,paidpartially};
	private statusses status;
	private Invoiceactions actions;
	private String lastinvoicefilename;
	public boolean validinvoice=false;
	public Partner partner;
	public static double amountthreshold=10;




	/**
	 * Creates a new invoice for this partner, generates a PDF and sends the
	 * invoice via email
	 * 
	 * @param partnerid
	 */
	public Invoice(int partnerid, boolean force, HttpServletRequest request){
		this.partnerid=partnerid;
		partner=new Partner(partnerid);
		Debitor debitor=new Debitor(partnerid);
		Calendar period=Calendar.getInstance();
		period.add(Calendar.MONTH, -1);
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;
		if (force||(debitor.amount>=10&&debitor.since.before(period))){
			try{
				con.setAutoCommit(false);
				Double vatpercentage=getVatpercentage();
				con.setAutoCommit(false);
				Calendar now=Calendar.getInstance();
				Calendar due=Calendar.getInstance();
				due.add(Calendar.DAY_OF_YEAR, partner.payterm+1);
				since=debitor.since.getTime();
				amountex=debitor.amount;
				//Dbutil.logger.info(debitor.amount*vatpercentage*100+" "+Math.round(debitor.amount*vatpercentage*100)+" "+(double)Math.round(debitor.amount*vatpercentage*100)/100);
				vat=(double)Math.round(debitor.amount*vatpercentage*100)/100;
				totalamount=(double)Math.round(debitor.amount*(1+vatpercentage)*100)/100;
				createdate=now.getTime();
				duedate=due.getTime();
				status=statusses.open;
				Dbutil.executeQuery("Insert into invoices (partnerid,amountex,vat,amount, createdate,duedate,paydate,status) values " +
						"("+partnerid+","+amountex+","+vat+","+totalamount+",'"+new java.sql.Timestamp(createdate.getTime()).toString()+"','"+new java.sql.Timestamp(duedate.getTime()).toString()+"','0000-00-00 00:00:00','open');", con);
				rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
				if (rs.next()){
					id=rs.getInt("id");
					Invoiceaction ia=new Invoiceaction("Create invoice", now.getTime(), due.getTime(), "", "New", "Pending",partnerid,id,true,con);
					ArrayList<Invoiceaction> actions=new ArrayList<Invoiceaction>();
					actions.add(ia);
					setActions(new Invoiceactions(actions));
					addInvoiceAction(ia);
					InvoicePDF pdf=new InvoicePDF(this,ia);
					if (pdf.isValid){
						lastinvoicefilename=ia.filename;
						rs=Dbutil.selectQuery("select group_concat(id) as ids, sum(cpc) as total, count(*) as clicks from billingoverview where invoice=0 and partnerid="+partnerid+" group by invoice;",con);
						if (rs.next()&&Math.abs(rs.getDouble("total")-amountex)<0.001){
							clicks=rs.getInt("clicks");
							if (sendByMail(request.getRemoteUser())){
								if (Dbutil.executeQuery("Update billingoverview set invoice="+id+" where id in ("+rs.getString("ids")+") and partnerid="+partnerid+" and invoice=0;", con)>0){
									validinvoice=true;
									con.commit();
								} else {
									con.rollback();
								}
							} else {
								Dbutil.logger.error("Problem while creating invoice, amount for debitor="+debitor.amount+", total from billingoverview="+rs.getDouble("total")+". Difference="+Math.abs(rs.getDouble("total")-debitor.amount));
								con.rollback();
								amountex=0;
								vat=0;
								totalamount=0;
								validinvoice=false;
							}
						}
					} else {
						Dbutil.logger.error("Could not create invoice PDF for partner "+debitor.partnerid+".");
						con.rollback();
						amountex=0;
						vat=0;
						totalamount=0;
						validinvoice=false;
					}
				}
				con.rollback();
			} catch(Exception e){
				Dbutil.logger.error("Problem while creating invoice",e);
				try {
					con.rollback();
				} catch (SQLException e1) {
					
				}
			}

		}

	}



	public Invoice(int id, int partnerid){
		getInvoice(id,partnerid);
		actions=new Invoiceactions(this);
	}
	
	public static double getVatpercentage() throws Exception{
		double vat=0;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select vat from vat where country='Netherlands';", con);
			if (rs.next()){
				vat=(rs.getDouble("vat")/100);
			}
		}catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (vat<0.01){
			 Dbutil.logger.error("Could not retrieve VAT for the Netherlands!");
			 throw (new Exception());
			
		}
		return vat;
	}

	public boolean sendByMail(String adminuserid){
		boolean success=false;
		
		try {
			String messagetext = "";
			SimpleDateFormat dueformat = new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.UK);
			SimpleDateFormat dateformat = new SimpleDateFormat("d MMMM yyyy",Locale.UK);
			messagetext += "Dear customer,<br/><br/>";
			messagetext += "Thank you for advertising on Vinopedia.com! We have directed "
					+ clicks + " wine buyers to your web site.<br/><br/>";
			messagetext += "Attached you find the invoice for referral fees and/or commision on Vinopedia.com in the period "
					+ dateformat.format(since)
					+ " until "
					+ dateformat.format(createdate) + ".<br/>";
			messagetext += "The payment term is " + partner.payterm
					+ " days, which means this invoice is due on "
					+ dueformat.format(duedate) + ". <br/>";
			messagetext += "In case of questions, please reply to this email.<br/>";
			messagetext += "Thank you for doing business with us! <br/>";
			messagetext += "<br/>Kind regards,<br/>";
			messagetext += "Jasper Hammink,<br/>";
			messagetext += "Vinopedia.com<br/><br/>";
			messagetext += "jasper.hammink@vinopedia.com<br/>";
			messagetext += "Pieter Nieuwlandstraat 57<br/>";
			messagetext += "3514 HD Utrecht, the Netherlands<br/>";
			SmbFile outputdir = new SmbFile(Configuration.invoicedir);
			SmbFile signed = new SmbFile(outputdir, lastinvoicefilename);
			//SmbFileInputStream fin = new SmbFileInputStream(signed);
			Emailer e = new Emailer();
			success = e.sendEmail(Configuration.invoiceaccount,
					Configuration.invoiceaccount, "Invoice Vinopedia",
					messagetext, signed);
			Auditlogger al = new Auditlogger();
			al.setPartnerid(partnerid);
			al.setUserid(adminuserid);
			if (success) {
				al.setAction("Successfully sent invoice " + id + " to "
						+ Configuration.invoiceaccount);
			} else {
				al.setAction("Could not send invoice " + id + " to "
						+ Configuration.invoiceaccount);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem sending invoice by email: ", e);
			success=false;
		}
		return success;
	}


	public String getLastinvoicefilename() {
		return lastinvoicefilename;
	}

	public void setLastinvoicefilename(String lastinvoicefilename) {
		this.lastinvoicefilename = lastinvoicefilename;
	}

	private void getInvoice(int id, int partnerid){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select * from invoices where id=" + id + " and partnerid="+partnerid+";";
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()) {
				this.id=id;
				//shopid=rs.getInt("shopid");
				this.partnerid=rs.getInt("partnerid");
				amountex=rs.getDouble("amount");
				vat=rs.getDouble("vat");
				totalamount=amountex+vat;
				createdate=rs.getDate("createdate");
				duedate=rs.getDate("duedate");
				try{
					paydate=rs.getDate("paydate");
				} catch (Exception e){}
				status=statusses.valueOf(rs.getString("status"));
				validinvoice=true;
			} else {
				Dbutil.logger.error("Could not retrieve invoice with id="+id);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoice with id="+id, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
	}

	public static ArrayList<Invoice> getInvoices(int partnerid){
		ArrayList<Invoice> invoices=new ArrayList<Invoice>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select * from invoices where partnerid=" + partnerid + " order by id desc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				invoices.add(new Invoice(rs.getInt("id"),partnerid));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoice list with partnerid="+partnerid, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);	
		return invoices;
	}

	public static ArrayList<Debitor> getDuePartners(boolean due){
		ArrayList<Debitor> debitors=new ArrayList<Debitor>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			if (due){
				query = "Select sum(cpc) as amount, min(date) as since,partnerid from billingoverview where invoice=0 group by partnerid having sum(cpc)>="+amountthreshold+" and (NOW() - INTERVAL 1 MONTH) >= min(date);";
			} else {
				query = "Select sum(cpc) as amount, min(date) as since,partnerid from billingoverview where invoice=0 group by partnerid having sum(cpc)<"+amountthreshold+" or (NOW() - INTERVAL 1 MONTH) < min(date);";
			}
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				Debitor deb=new Debitor(rs.getInt("partnerid"));
				debitors.add(deb);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving partners with accounts that are due.", e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);	
		return debitors;
	}

	public static class Debitor{
		public int partnerid;
		public double amount;
		public Calendar since;

		public Debitor(int partnerid){
			ResultSet rs=null;
			Connection con=Dbutil.openNewConnection();
			String query;
			try {
				query = "Select sum(cpc) as amount, min(date) as since,partnerid from billingoverview where invoice=0 and partnerid="+partnerid+" group by partnerid;";
				rs = Dbutil.selectQuery(query, con);
				while (rs.next()) {
					this.partnerid=rs.getInt("partnerid");
					amount=rs.getDouble("amount");
					Date date=rs.getDate("since");
					since=Calendar.getInstance();
					since.setTime(date);
				}
			} catch (Exception e) {
				Dbutil.logger.error("Problem retrieving partners with accounts that are due.", e);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);	
			}
		}
	}



	public void addInvoiceAction(Invoiceaction ia){
		// TO DO: insert into DB

	}

	public ArrayList<String> getInvoiceDetails(){
		NumberFormat format  = new DecimalFormat("#,##0.00");	
		ArrayList<String> details=new ArrayList<String>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select date(min(date)) as mindate, date(max(date)) as maxdate from billingoverview join invoices on (billingoverview.invoice=invoices.id) where invoice=" + id + ";";
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()){
				details.add(rs.getString("mindate"));
				details.add(rs.getString("maxdate"));
			}
			query = "Select type, cpc, count(*) as thecount, sum(cpc) as total from billingoverview join invoices on (billingoverview.invoice=invoices.id) where invoice=" + id + " group by type, cpc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				if (rs.getString("type").equalsIgnoreCase("banners")) {
					details.add("Banner clicks");
				} else if (rs.getString("type").equalsIgnoreCase("sponsoredlinks")) {
					details.add("Sponsored results");
				} else {
					Dbutil.logger.error("unknown type in table billingoverview");
					details.add("");
				}
				details.add(Webroutines.formatPrice(rs.getDouble("payperclick")));
				details.add(Webroutines.formatPrice(rs.getDouble("total")));
				details.add((rs.getString("thecount")));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoice list with partnerid="+partnerid, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);	


		return details;
	}


	public Invoiceaction getLastAction(){
		return actions.actions.get(actions.actions.size()-1);
	}

	public Invoiceactions getActions() {
		return actions;
	}
	public void setActions(Invoiceactions actions) {
		this.actions = actions;
	}
	public String getFormattedAmount(double amount) {
		NumberFormat format  = new DecimalFormat("#,##0.00");	
		return "� "+format.format(amount);
	}
	public Date getCreatedate() {
		return createdate;
	}
	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}
	public Date getDuedate() {
		return duedate;
	}
	public void setDuedate(Date duedate) {
		this.duedate = duedate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(int partnerid) {
		this.partnerid = partnerid;
	}
	public Date getPaydate() {
		return paydate;
	}
	public void setPaydate(Date paydate) {
		this.paydate = paydate;
	}
	/*
	public int getShopid() {
		return shopid;
	}
	public void setShopid(int shopid) {
		this.shopid = shopid;
	}
	*/
	public statusses getStatus() {
		return status;
	}
	public void setStatus(statusses status) {
		this.status = status;
	}



	public double getAmountex() {
		return amountex;
	}



	public void setAmountex(double amountex) {
		this.amountex = amountex;
	}



	public double getVat() {
		return vat;
	}



	public void setVat(double vat) {
		this.vat = vat;
	}



	public double getTotalamount() {
		return totalamount;
	}



	public void setTotalamount(double totalamount) {
		this.totalamount = totalamount;
	}

}