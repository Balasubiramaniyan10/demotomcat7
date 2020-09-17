package com.freewinesearcher.online.invoices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import jcifs.smb.SmbFile;

public class SmbDataSource implements DataSource {

	SmbFile input;
	
	public SmbDataSource(SmbFile input){
		this.input=input;
	}
	public String getContentType() {
		return "application/pdf";
	}

	public InputStream getInputStream() throws IOException {
		return input.getInputStream();
	}

	public String getName() {
		return input.getName();
	}

	public OutputStream getOutputStream() throws IOException {
		return input.getOutputStream();
	}

}
