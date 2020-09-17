package com.freewinesearcher.online.invoices;
import java.io.FileOutputStream;
import java.io.IOException;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Partner;
import com.freewinesearcher.online.Webroutines;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import com.lowagie.text.pdf.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Locale;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.io.*;
import java.net.URL;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.netbios.NbtAddress;


public class InvoicePDF  extends PdfPageEventHelper {

	String filename;
	String font=BaseFont.HELVETICA;
	Font fontsmall = FontFactory.getFont(font, 8);
	Font linkfont  = FontFactory.getFont(font, 10,Font.UNDERLINE,new Color(0, 0, 255));
	boolean SumIsOK=false;
	boolean isValid=false;

	public InvoicePDF() {
	}	
	
	public InvoicePDF(Invoice invoice, Invoiceaction ia) throws Exception {
		Format format=new SimpleDateFormat("dd-MM-yyyy",Locale.UK);

		// step 1: creation of a document-object
		Document document = new Document();
		document.setMargins((float)40,(float)40,(float)40,(float)90);

		try {
			BaseFont bf = BaseFont.createFont(font, "Cp1252", false);

			// step 2:
			// we create a writer that listens to the document
			// and directs a PDF-stream to a file
			
			filename=ia.getFilename();	
			String dirname=Configuration.invoicedir;
			SmbFile outputdir = new SmbFile(Configuration.invoicedir);
			
			SmbFile unsigned = new SmbFile(outputdir,filename+".unsigned");
			SmbFileOutputStream SOSunsigned = new SmbFileOutputStream(unsigned);
	        //PdfWriter writer = PdfWriter.getInstance(document,
			//		new FileOutputStream(Wijnzoeker.invoicedir+filename+".unsigned"));
	        PdfWriter writer = PdfWriter.getInstance(document,SOSunsigned);
			writer.setPageEvent(new InvoicePDF());

			// step 3: we open the document
			document.open();
			float width = document.getPageSize().getWidth();
			float height = document.getPageSize().getHeight();

			// step 4: add content

			// Logo
			URL url=new URL("https://www.vinopedia.com/images/logohuge.gif");
			Image img = Image.getInstance(url);
			//Image img = Image.getInstance("css/logo.png");
			img.scaleToFit((float)200,(float)70);
			document.add(img);

			// Invoice
			Paragraph p=new Paragraph("INVOICE     ");
			p.setAlignment(p.ALIGN_RIGHT);
			document.add(p);

			// Letter head
			document.add(new Paragraph("Search as a Service"));
			document.add(new Paragraph("Pieter Nieuwlandstraat 57"));
			document.add(new Paragraph("3514 HD Utrecht"));
			document.add(new Paragraph("The Netherlands"));
			document.add(new Paragraph("\n"));

			// Partner data
			Partner partner=new Partner(invoice.getPartnerid());
			document.add(new Paragraph("To: "+partner.name));
			//document.add(new Paragraph(partner.representative));
			document.add(new Paragraph(partner.address));
			document.add(new Paragraph("Invoice sent to: "+partner.email));
			document.add(new Paragraph(" "));
			document.add(new Paragraph(" "));


			// Invoice data
			InvoiceDetails details=new InvoiceDetails(invoice.partnerid);
			document.add(new Paragraph("Invoice date: "+format.format(invoice.getCreatedate())));
			document.add(new Paragraph("Invoice number: "+invoice.id));
			document.add(new Paragraph("Concerns: Referral fees and commission for Vinopedia.com"));
			document.add(new Paragraph("Period concerned: "+format.format(details.fromdate)+" until "+format.format(details.todate)));
			document.add(new Paragraph(" "));
			PdfPTable table = null;
			float[] columnDefinitionSize = { 50F, 15F,15F,15F};
			table = new PdfPTable(columnDefinitionSize);
			table.getDefaultCell().setBorder(0);
			table.setHorizontalAlignment(0);
			table.setTotalWidth(width - 72);
			table.setLockedWidth(true);

			Double totallineitems=0.0;
			table.addCell(new Phrase("Description"));
			table.addCell(new Phrase("# items"));
			table.addCell(new Phrase("Amount per item"));
			table.addCell(new Phrase("Total"));
			for (int i=0;i<details.details.size();i++){
				table.addCell(new Phrase(details.details.get(i).description));
				table.addCell(new Phrase(details.details.get(i).clicks+""));
				if (!details.details.get(i).description.equals("orders")){
					table.addCell(new Phrase("� "+Webroutines.formatPrice(details.details.get(i).cpc)));
				} else {
					table.addCell(new Phrase(""));
				}
				table.addCell(new Phrase("� "+Webroutines.formatPrice(details.details.get(i).amount)));
				totallineitems+=details.details.get(i).amount;
			}
			if (invoice.getAmountex()==totallineitems){
				SumIsOK=true;
				table.addCell(new Phrase("Total"));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(invoice.getFormattedAmount(invoice.getAmountex())));
				table.addCell(new Phrase("VAT ("+(invoice.getVatpercentage()*100)+"%)"));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(invoice.getFormattedAmount(invoice.getVat())));
				table.addCell(new Phrase("Total amount to pay"));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(""));
				table.addCell(new Phrase(invoice.getFormattedAmount(invoice.getTotalamount())));
				document.add(table);
				Anchor anchor = new Anchor("Click to see details of this invoice.",linkfont);
				anchor.setReference("https://www.vinopedia.com/shops/showinvoice.jsp?invoiceid="+invoice.getId());
				anchor.setName("Click to see details of this invoice.");
				document.add(anchor);
				document.add(new Paragraph("Payment conditions: this invoice must be paid within "+partner.payterm+" days from the invoice date."));

			} else {
				Dbutil.logger.error("Problem while creating invoice with id="+invoice.getId());
				Dbutil.logger.error("The total amount (ex vat) on the invoice ("+invoice.getAmountex()+") is not equal to the sum of al line items ("+totallineitems+").");
				throw (new Exception());
			}



			// step 5: we close the document
			document.close();
			SOSunsigned.close();

			// step 6: sign it and remove original
			//signPDF(Wijnzoeker.invoicedir+filename+".unsigned",Wijnzoeker.invoicedir+filename,Wijnzoeker.SSLcertificate,Wijnzoeker.SSLpassword);
			signPDF(Configuration.invoicedir+filename+".unsigned",Configuration.invoicedir+filename,Configuration.SSLcertificate,Configuration.SSLpassword);
			new File(Configuration.invoicedir+filename+".unsigned").delete();
			isValid=true;
		} catch (Exception e) {
			Dbutil.logger.error("Problem while creating PDF for invoice with id="+invoice.getId(),e);
			throw (e);
		}

	}

	public void onEndPage(PdfWriter writer, Document document) {
		try {
			Rectangle page = document.getPageSize();
			float[] columnDefinitionSize = { 15F, 85F};
			PdfPTable foot = new PdfPTable(columnDefinitionSize);
			foot.addCell(footerCell("Search as a Service"));
			foot.addCell(footerCell(""));
			foot.addCell(footerCell("Contact"));
			foot.addCell(footerCell("Email: Jasper.Hammink@vinopedia.com Phone: +31306668268"));
			foot.addCell(footerCell("Bank"));
			foot.addCell(footerCell("Account number 3528637. IBAN: NL 14 PSTB 0003 5286 37. BIC: PSTBNL21"));
			foot.addCell(footerCell("Legal"));
			foot.addCell(footerCell("Chamber of Commerce Utrecht, the Netherlands, number 30226759. VAT number NL1424.50.406.B02"));
			foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
			foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(),
					writer.getDirectContent());

		}
		catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}


	public PdfPCell footerCell(String text) {
		PdfPCell cell=new PdfPCell();
		try {
			cell.setBorder(0);
			cell.addElement(new Phrase(text,fontsmall));

		}
		catch (Exception e) {
			throw new ExceptionConverter(e);
		}
		return cell;
	}

	public static void signPDF(String filenamein, String filenameout, String certificate, String password) throws Exception{
		try{
			//KeyStore ks = KeyStore.getInstance("pkcs12");
			password="changeit";
			KeyStore ks = KeyStore.getInstance("jks");
			ks.load(new FileInputStream(certificate), password.toCharArray());
			String alias = (String)ks.aliases().nextElement();
			alias="www.vinopedia.com";
			PrivateKey key = (PrivateKey)ks.getKey(alias, password.toCharArray());
			Certificate[] chain = ks.getCertificateChain(alias);
			SmbFile outputdir = new SmbFile(Configuration.invoicedir);
			SmbFile signed = new SmbFile(outputdir,filenameout);
			SmbFile unsigned = new SmbFile(outputdir,filenamein);
			SmbFileInputStream fin = new SmbFileInputStream(unsigned);
			SmbFileOutputStream fout = new SmbFileOutputStream(signed);
			PdfReader reader = new PdfReader(fin);
			//FileOutputStream fout = new FileOutputStream(filenameout);
			PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
			PdfSignatureAppearance sap = stp.getSignatureAppearance();
			sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
			sap.setReason("Verification of the author");
			sap.setLocation("Utrecht, the Netherlands");
			//			comment next line to have an invisible signature
			//sap.setVisibleSignature(new Rectangle(480, 680, 560, 760), 1, null);
			
			stp.close();
			fin.close();
			fout.close();
		} catch (Exception e){
			Dbutil.logger.error("Problem signing PDF: ",e);
			throw (e);
		}
	}

}