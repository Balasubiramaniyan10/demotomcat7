package com.freewinesearcher.batch;

import org.apache.commons.lang.StringUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;

public class NetworkTool  extends Thread {



	Socket socket;
	InputStream in;
	OutputStream out;
	request req;
	String host;
	int port;
	String oldip;

	long count;
	boolean status;
	boolean ready=false;
	String auth;

	public static final int WILL = 251;
	public static final int WONT = 252;
	public static final int DO = 253;
	public static final int DONT = 254;
	public static final int IAC = 255;

	public NetworkTool(String host, int port, String auth) {
		socket = null;
		in = null;
		out = null;
		req = null;
		this.host = null;
		this.host = host;
		this.port = port;
		this.auth = auth;
	}

	public void run() {
		ready=false;
		while (!ready){
			try {
				String newip=getIP();
				while(newip.equals(oldip)||getIP().equals("")){
					ready=false;
					socket = new Socket(host, port);
					String command = "";
					in = socket.getInputStream();
					out = socket.getOutputStream();
					//Dbutil.logger.info(socket.toString());
					req = new request(in, this);
					req.start();

					count = 0;
					status = false;

					command = "AUTHENTICATE";
					if(StringUtils.isNotEmpty(auth))
						command += " " + auth;
					//Dbutil.logger.info(command);
					byte binary[] = (command + "\r\n").getBytes();
					out.write(binary);
					out.flush();
					

					while(!status && count<1000){
						Thread.sleep(100);
						count++;
					}

					count = 0;
					status = false;

					command = "SIGNAL NEWNYM";
					//Dbutil.logger.info(command);
					binary = (command + "\r\n").getBytes();
					out.write(binary);
					out.flush();

					while(!status && count<1000){
						Thread.sleep(100);
						count++;
					}

					command = "quit";
					//Dbutil.logger.info(command);
					binary = (command + "\r").getBytes();
					out.write(binary);
					out.flush();

					//Dbutil.logger.info("NetworkTool: Closed the channel ");
					in.close();
					out.close();
					socket.close();

					Thread.sleep(1000);

					newip=getIP();
					
				}
				ready=true;
			} catch (Exception e) {
				Dbutil.logger.info("NetworkTool Error: " + e.getMessage());
			} finally {
				try {
					socket.close();
				} catch (Exception e) {
					Dbutil.logger.info("NetworkTool Exception:" + e.getMessage());
				}
			}
		}
	}

	//main(): invoking example

	public static void requestNewIdenty(String ip){
		Dbutil.logger.info("New Identity requested...");
		NetworkTool t = new NetworkTool("127.0.0.1", 9051, "\"ditismijntorpassword\"");
		t.oldip=ip;
		t.start();
		while (!t.ready){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}

		}
		Dbutil.logger.info("New Identity established..." +getIP());

	}

	public static String getIP(){
		Webpage webpage=new Webpage();
		webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
		webpage.maxattempts=1;
		webpage.urlstring="https://test.vinopedia.com/myipis.jsp";
		webpage.readPage();
		if (webpage.responsecode!=200)
			try {
				Thread.sleep(1000*5*60);
				return "";
			} catch (InterruptedException e) {
				
			}
		if (webpage.html.length()>20) return "";
		return webpage.html.replace("\n", "");
	}



	//Inner class: request
	class request extends Thread {

		InputStream rin;
		NetworkTool telnet;

		public request(InputStream in, NetworkTool t) {
			rin = null;
			telnet = null;
			rin = in;
			telnet = t;
		}

		public void run() {
			try {
				int totalBinary = -1;
				String line = "";
				InputStreamReader r = new InputStreamReader(in);
				while (!telnet.socket.isClosed()) {
					if ((totalBinary = in.available()) > 0) {
						byte binary[] = new byte[totalBinary];
						int size = in.read(binary);
						line = new String(binary);
						if (!line.contains("250 OK")) Dbutil.logger.info("Request::run(): " + line.trim());

						if(line.toUpperCase().indexOf("OK") > 0)
							status = true;

						if (binary[0] == (byte) IAC) {
							switch (binary[1]) {
							case(byte) WILL:
								binary[1] = (byte) DONT;
							break;
							case(byte) DO:
								binary[1] = (byte) WONT;
							break;
							}
							telnet.out.write(binary);
						}

					}
					synchronized (this) {
						wait(20);
					}
				}
				//Dbutil.logger.info("Request::run(): Socket connection is detached.");
			}
			catch (Exception e) {
				Dbutil.logger.info("Request::run() Error: " + e.getMessage());
			}
		}
	}

}
