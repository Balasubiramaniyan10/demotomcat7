package com.freewinesearcher.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.freewinesearcher.batch.Excelreader;
import com.freewinesearcher.batch.Spider;

public class Webpage {

	public String urlstring = "";
	public String encoding = "ISO-8859-1";
	// Variables var=new Variables();
	public String postdata = "";
	public boolean ignorepagenotfound = false;
	public boolean followredirect = true;
	public boolean logging = true;
	public boolean allowcookies = true;
	public String useragent = "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3";
	public String sessionid = null;
	public String html = "";
	public String standardcookie = "";
	ArrayList<FWSCookie> cookie = new ArrayList<FWSCookie>();;
	public java.net.Proxy proxy = null;
	public int maxattempts = 3;
	public int errorpause = 30;
	public String type = "";
	public int responsecode = 0;
	public boolean badurl = false;
	public int maxredirects = 6;
	public int redirects = 0;
	String Page = "";
	boolean succes = false;
	public String headers = "";
	int id = 0;
	String shopName = "";

	public Webpage() {
	}

	public Webpage(String urlstring, String encoding, String postdata, boolean ignorepagenotfound,
			boolean followredirect, int id, String name) {
		super();
		this.urlstring = urlstring;
		this.encoding = encoding;
		this.postdata = postdata;
		this.ignorepagenotfound = ignorepagenotfound;
		this.followredirect = followredirect;
		this.id = id;
		this.shopName = name;
	}

	public Webpage(String urlstring, String encoding, String postdata, boolean ignorepagenotfound,
			boolean followredirect) {
		super();
		this.urlstring = urlstring;
		this.encoding = encoding;
		this.postdata = postdata;
		this.ignorepagenotfound = ignorepagenotfound;
		this.followredirect = followredirect;
	}

	public void readPage() {
		redirects = 0;
		readPageRecursive();
	}

	private void readPageRecursive() {
		String trimmedurl = urlstring;
		// String test = null;
		boolean pdf = false;
		boolean excel = false;
		if (urlstring.startsWith("pdf:")) {
			pdf = true;
			trimmedurl = urlstring.substring(4);
		}
		if (urlstring.toLowerCase().endsWith(".pdf")) {
			pdf = true;
		}
		if (urlstring.startsWith("file://")) {
			readPageFromFile();
		} else {
			if (urlstring.startsWith("https://")) {
				readHttpsFeedData(trimmedurl, pdf, excel);
			} else if (urlstring.startsWith("http://")) {
				readHttpFeedData(trimmedurl, pdf, excel);
			}
		}
	}

	public static String getBaseUrl(String url) {
		String baseurl = "";
		if (url.startsWith("http")) {
			if (url.indexOf("/", 9) > 0) {
				baseurl = url.substring(0, url.indexOf("/", 9));
			} else {
				baseurl = url;
			}
		}
		return baseurl;
	}

	public InputStream getAsInputStream() {
		String trimmedurl = urlstring;
		// String test = null;
		// boolean pdf = false;

		int attempt = 0;
		boolean succes = false;
		if (postdata == null) {
			postdata = "";
		}
		// String exceptionmessage = "";
		// If postdata has the form "GET&param1=Yes, then all GET params in URL will be
		// added to the post parameters.
		if (trimmedurl.indexOf("?", 0) > 0 && postdata != null && postdata.startsWith("GET&")) {
			postdata = trimmedurl.substring(trimmedurl.indexOf("?", 0) + 1) + "&" + postdata.substring(4);
		}
		if (postdata != null && postdata.startsWith("GET&")) {
			postdata = postdata.substring(4);
		}
		// If trimmedurl has the form "...GET&param1=Yes, then all params in URL after
		// GET& will be POSTed
		if (trimmedurl.contains("GET&")) {
			if (!postdata.equals("")) {
				postdata = postdata + "&";
			}
			postdata = postdata + trimmedurl.substring(trimmedurl.indexOf("GET&") + 4);
			trimmedurl = trimmedurl.substring(0, trimmedurl.indexOf("GET&"));
		}

		trimmedurl = Spider.replaceString(trimmedurl, " ", "%20");
		URL url = null;

		HttpURLConnection urlcon = null;
		// long lastmodified;
		// String inputLine;
		// StringBuffer sb = new StringBuffer();
		badurl = false;
		try {
			url = new URL(trimmedurl);
		} catch (Exception exc) {
			Dbutil.logger.warn("Foute URL " + trimmedurl + " Shop ID: " + id + " Shop Name: " + shopName);
			badurl = true;
			// throw new MalformedURLException();
		}

		if (logging) {
			Dbutil.logger.debug("Starting to get web page" + " Shop ID: " + id + " Shop Name: " + shopName);
		}

		if (url != null) {
			while (succes == false && attempt < maxattempts) {
				try {
					attempt++;
					if (proxy == null) {
						urlcon = (HttpURLConnection) url.openConnection();
					} else {
						urlcon = (HttpURLConnection) url.openConnection(proxy);
					}
					urlcon.setConnectTimeout(Configuration.webpagetimeout);
					urlcon.setReadTimeout(Configuration.webpagetimeout);
					urlcon.setRequestProperty("User-Agent", useragent);
					urlcon.setInstanceFollowRedirects(followredirect);
					String cookies = standardcookie + getCookie();
					if (allowcookies && cookies.length() > 0) {
						urlcon.setRequestProperty("Cookie", cookies);
					}
					if (postdata != null && !postdata.equals("")) {
						DataOutputStream printout;
						urlcon.setDoInput(true);
						// Let the RTS know that we want to do output.
						urlcon.setDoOutput(true);
						// No caching, we want the real thing.
						urlcon.setUseCaches(false);
						// Specify the content type.
						urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						// Send POST output.
						printout = new DataOutputStream(urlcon.getOutputStream());

						printout.writeBytes(postdata);
						printout.flush();
						printout.close();

					}

					// lastmodified=urlcon.getLastModified();
					if (encoding == null || encoding.equals("")) {
						encoding = "ISO-8859-1";
					}
					if (!java.nio.charset.Charset.isSupported(encoding)) {
						// Dbutil.logger.info("Charset "+encoding+" is not supported for URL
						// "+trimmedurl+", trying with ISO-8859-1");
						encoding = "ISO-8859-1";
					}
					String[] cookieValarray;
					String cookieval = urlcon.getHeaderField("Set-Cookie");
					if (cookieval != null) {
						// Dbutil.logger.info(urlcon.getHeaderField(i));
						cookieValarray = cookieval.split(";");
						String cookieVal = cookieValarray[0];
						try {
							String key = (cookieVal.split("=")[0]);
							String value = cookieVal.substring(key.length() + 1);
							FWSCookie thiscookie = new FWSCookie(key, value);
							setCookie(thiscookie);
						} catch (Exception e) {
							Dbutil.logger.info(
									"Invalid cookie: " + cookieVal + " Shop ID: " + id + " Shop Name: " + shopName);
						}
					}
					return urlcon.getInputStream();
				} catch (Exception exc) {
					// Dbutil.logger.error("",exc);
					// exceptionmessage = exc.toString();
					if (!ignorepagenotfound) {
						if (logging) {
							Dbutil.logger.debug("Cannot find web page, attempt " + attempt + ", URL = " + trimmedurl
									+ " Shop ID: " + id + " Shop Name: " + shopName, exc);
						}
					}
					try {
						if (attempt < maxattempts) {
							Thread.sleep(errorpause * 1000);
						}
						responsecode = urlcon.getResponseCode();
						urlcon.disconnect();
					} catch (Exception e) {
						if (logging) {
							Dbutil.logger.debug("Web page problem. " + " Shop ID: " + id + " Shop Name: " + shopName,
									e);
						}
					}
				}
			}
		}
		return null;

	}

	public void readPageFromFile() {
		StringBuffer Page = new StringBuffer();
		String file = urlstring.substring(7);
		try {
			FileReader fstream = new FileReader(file);
			BufferedReader in = new BufferedReader(fstream);

			while (in.ready()) {
				Page.append(in.readLine() + "\n");
			}
			in.close();
		} catch (Exception e) {
			Dbutil.logger.error("Problem reading file " + file + " Shop ID: " + id + " Shop Name: " + shopName, e);
		}
		// Close the output stream
		html = Page.toString();
	}
	/*
	 * 
	 * public String PDFReader(InputStream pdf) throws Exception { String html="";
	 * try{ PDFText2HTML pdf2=new PDFText2HTML(); pdf2.setSortByPosition(true);
	 * pdf2.setSuppressParagraphs(false); PDDocument document =
	 * PDDocument.load(pdf); StringWriter out=new StringWriter();
	 * pdf2.setWordSeparator("</td><td>"); pdf2.setLineSeparator("</tr>\n<tr>");
	 * pdf2.writeText(document, out); html+=out.toString();
	 * html.replaceAll("^</tr>$", ""); html.replaceAll("^<tr>$", "");
	 * html.replaceAll("^<tr></td>", "<tr>"); html.replaceAll("<td></tr>", "</tr>");
	 * 
	 * document.close(); } catch (Exception e){
	 * Dbutil.logger.error("Problem while parsing PDF",e); throw(e); } return html;
	 * }
	 */

	public String newPDFReader(InputStream pdf) throws Exception {
		String html = "";
		try {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setWordSeparator("</td><td>");
			stripper.setLineSeparator("</tr>\n<tr>");
			stripper.setSortByPosition(true);
			PDDocument document = PDDocument.load(pdf);
			StringWriter out = new StringWriter();
			stripper.writeText(document, out);
			html += out.toString();
			html.replaceAll("^</tr>$", "");
			html.replaceAll("^<tr>$", "");
			html.replaceAll("^<tr></td>", "<tr>");
			html.replaceAll("<td></tr>", "</tr>");

			document.close();
		} catch (Exception e) {
			Dbutil.logger.error("Problem while parsing PDF" + " Shop ID: " + id + " Shop Name: " + shopName, e);
			throw (e);
		}
		return html;
	}

	public String getCookie() {
		String cookiestring = "";
		try {
			for (FWSCookie c : cookie) {
				cookiestring += c.getKey() + "=" + c.getValue() + ";";
			}
		} catch (Exception e) {
			Dbutil.logger.error("Shop ID: " + id + " Shop Name: " + shopName, e);
		}
		return cookiestring;
	}

	public void setCookie(String cookie) {
		if (cookie == null) {
			cookie = "";
		}
		String[] cookieValarray = cookie.split(";");
		for (String cookieVal : cookieValarray) {
			try {
				if (cookieVal.contains("=")) {
					FWSCookie newcookie = new FWSCookie(cookieVal.split("=")[0],
							cookieVal.substring(cookieVal.split("=")[0].length() + 1));
					setCookie(newcookie);
				}
			} catch (Exception e) {
			}
		}
	}

	public void setCookie(FWSCookie newcookie) {
		try {
			for (int j = 0; j < cookie.size();) {
				if (cookie.get(j).getKey().equals(newcookie.getKey())) {
					cookie.remove(j);
				} else {
					j++;
				}
			}
			cookie.add(newcookie);
		} catch (Exception e) {
			Dbutil.logger.error("Error adding cookie: " + " Shop ID: " + id + " Shop Name: " + shopName, e);

		}
	}

	public static void main(String[] a) {
		Webpage webpage = new Webpage();
		webpage.urlstring = "http://www.wineoutlet.com/InventorySearch.aspx";
		webpage.readPage();
	}

	public void readHttpFeedData(String trimmedurl, boolean pdf, boolean excel) {

		int attempt = 0;
		succes = false;
		if (postdata == null) {
			postdata = "";
		}
		String exceptionmessage = "";
		// If postdata has the form "GET&param1=Yes, then all GET params in URL will be
		// added to the post parameters.
		if (trimmedurl.indexOf("?", 0) > 0 && postdata != null && postdata.startsWith("GET&")) {
			postdata = trimmedurl.substring(trimmedurl.indexOf("?", 0) + 1) + "&" + postdata.substring(4);
		}
		if (postdata != null && postdata.startsWith("GET&")) {
			postdata = postdata.substring(4);
		}
		// If trimmedurl has the form "...GET&param1=Yes, then all params in URL after
		// GET& will be POSTed
		if (trimmedurl.contains("GET&")) {
			if (!postdata.equals("")) {
				postdata = postdata + "&";
			}
			postdata = postdata + trimmedurl.substring(trimmedurl.indexOf("GET&") + 4);
			trimmedurl = trimmedurl.substring(0, trimmedurl.indexOf("GET&"));
		}

		if (trimmedurl.startsWith("xls:")) {
			type = "application/vnd.ms-excel";
			trimmedurl = trimmedurl.substring(4);
			excel = true;
			// Page=Excelreader.ReadUrl(trimmedurl.substring(4));
		} else if (trimmedurl.toLowerCase().endsWith("xls")) {
			type = "application/vnd.ms-excel";
			excel = true;
			// Page=Excelreader.ReadUrl(trimmedurl);
		}
		trimmedurl = Spider.replaceString(trimmedurl, " ", "%20");
		URL url = null;

		HttpURLConnection urlcon = null;
		// long lastmodified;
		String inputLine;
		StringBuffer sb = new StringBuffer();
		badurl = false;
		try {
			System.setProperty("https.protocols", "TLSv1.2");
			System.setProperty("http.agent", "");
			// System.setProperty("http.agent", "Chrome");

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, null, null);
			SSLContext.setDefault(context);

			url = new URL(trimmedurl);
		} catch (Exception exc) {
			Dbutil.logger.warn("Foute URL " + trimmedurl + " Shop ID: " + id + " Shop Name: " + shopName);
			badurl = true;
			// throw new MalformedURLException();
		}

		if (logging) {
			Dbutil.logger.debug("Starting to get web page for" + " Shop ID: " + id + " Shop Name: " + shopName);
		}
		if (url != null) {
			while (succes == false && attempt < maxattempts) {
				try {
					attempt++;
					if (trimmedurl.substring(0, 8).equals("http://")) {
						System.out.println("http");
					} else if (trimmedurl.substring(0, 8).equals("https://")) {
						System.out.println("https");
					}

					if (proxy == null) {
						urlcon = (HttpURLConnection) url.openConnection();
					} else {
						urlcon = (HttpURLConnection) url.openConnection(proxy);
					}
					String redirect = urlcon.getHeaderField("Location");
					if (redirect != null) {
						if (redirect.startsWith("file://")) {
							urlstring = redirect;
							readPageFromFile();
						} else {
							if (redirect.startsWith("https://")) {
								readHttpsFeedData(redirect, pdf, excel);
							} else if (redirect.startsWith("http://")) {
								readHttpFeedData(redirect, pdf, excel);
							} else {
								throw new Exception("Invalid URL: " + redirect);
							}
						}
					} else {
						urlcon.addRequestProperty("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

						urlcon.setConnectTimeout(Configuration.webpagetimeout);
						urlcon.setReadTimeout(Configuration.webpagetimeout);
						// urlcon.setRequestProperty("User-Agent",useragent);
						urlcon.setInstanceFollowRedirects(followredirect);
						urlcon.setInstanceFollowRedirects(false);
						if (headers != null && headers.contains("=")) {
							for (String h : headers.split(";")) {
								urlcon.setRequestProperty(h.split("=")[0], h.split("=")[1]);

							}
						}
						urlcon.setRequestProperty("Accept-Language", "en-us");
						String cookies = standardcookie + getCookie();
						if (allowcookies && cookies.length() > 0) {
							urlcon.setRequestProperty("Cookie", cookies);
						}
						if (postdata != null && !postdata.equals("")) {
							DataOutputStream printout;
							urlcon.setDoInput(true);
							// Let the RTS know that we want to do output.
							urlcon.setDoOutput(true);
							// No caching, we want the real thing.
							urlcon.setUseCaches(false);
							// Specify the content type.
							urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							// Send POST output.
							printout = new DataOutputStream(urlcon.getOutputStream());

							printout.writeBytes(postdata);
							printout.flush();
							printout.close();

						}

						// lastmodified=urlcon.getLastModified();
						if (encoding == null || encoding.equals("")) {
							encoding = "ISO-8859-1";
						}
						if (!java.nio.charset.Charset.isSupported(encoding)) {
							// Dbutil.logger.info("Charset "+encoding+" is not supported for URL
							// "+trimmedurl+", trying with ISO-8859-1");
							encoding = "ISO-8859-1";
						}
						String[] cookieValarray;
						List<String> cookieval = urlcon.getHeaderFields().get("Set-Cookie");
						if (cookieval != null) {
							// Dbutil.logger.info(urlcon.getHeaderField(i));
							Iterator<String> cookieI = cookieval.iterator();
							while (cookieI.hasNext()) {
								cookieValarray = cookieI.next().split(";");
								String cookieVal = cookieValarray[0];
								try {
									String key = (cookieVal.split("=")[0]);
									String value = cookieVal.substring(key.length() + 1);
									FWSCookie thiscookie = new FWSCookie(key, value);
									setCookie(thiscookie);
								} catch (Exception e) {
									Dbutil.logger.info("Invalid cookie: " + cookieVal + " Shop ID: " + id
											+ " Shop Name: " + shopName);
								}
							}
						}

						if (pdf) {
							sb.append(newPDFReader(urlcon.getInputStream()));
							succes = true;
							Page = sb.toString();
							sb = null;
						} else if (urlcon.getResponseCode() >= 300 && urlcon.getResponseCode() < 400
								&& followredirect) {
							redirects++;
							if (redirects < maxredirects) {
								String newloc = urlcon.getHeaderField("Location");
								postdata = "";
								Thread.sleep(5000);
								urlstring = Spider.padUrl(newloc, urlstring, getBaseUrl(urlstring), "n.a.");
								urlcon.disconnect();
								readPageRecursive();

							} else {
								Dbutil.logger.error("Too many redirects for url " + urlstring + " Shop ID: " + id
										+ " Shop Name: " + shopName);
							}

						} else {
							type = urlcon.getContentType();
							if (type != null && type
									.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
								excel = true;
							}

							if (type != null && type.contains("application/vnd.ms-excel")) {
								excel = true;
							}

							InputStream inpstr = null;
							try {
								inpstr = urlcon.getInputStream();
							} catch (Exception e) {
								inpstr = urlcon.getErrorStream();
								sb.append("Webpage generated an error");
							}
							if (excel) {
								Page = Excelreader.ReadUrl(inpstr, trimmedurl);
								responsecode = urlcon.getResponseCode();
								if (Page != null && Page.length() > 100 && !Page.startsWith("Webpage")) {
									succes = true;
								}
								inpstr.close();
							} else {
								BufferedReader in = new BufferedReader(new InputStreamReader(inpstr, encoding));
								while ((inputLine = in.readLine()) != null) {
									sb.append(inputLine);
									sb.append("\n");
								}
								inpstr.close();
								in.close();
								responsecode = urlcon.getResponseCode();
								urlcon.disconnect();
								Page = sb.toString();

							}

							sb = null;
							if (logging) {
								Dbutil.logger.debug("Page retrieved from url " + trimmedurl + " Shop ID: " + id
										+ " Shop Name: " + shopName);
							}
							succes = true;
						}
					}
				} catch (Exception exc) {
					// Dbutil.logger.error("",exc);
					exceptionmessage = exc.toString();
					if (!ignorepagenotfound) {
						if (logging)
							Dbutil.logger.debug("Cannot find web page, attempt " + attempt + ", URL = " + trimmedurl
									+ " Shop ID: " + id + " Shop Name: " + shopName, exc);
					}
					try {
						if (attempt < maxattempts) {
							Thread.sleep(errorpause * 1000);
						}
						responsecode = urlcon.getResponseCode();
						urlcon.disconnect();
					} catch (Exception e) {
						if (logging)
							Dbutil.logger.debug("Web page problem. " + " Shop ID: " + id + " Shop Name: " + shopName,
									e);
					}
				}
			}
		}
		if (succes == false) {
			try {
				responsecode = urlcon.getResponseCode();
			} catch (Exception e) {
			}
			if (responsecode == 404 || ignorepagenotfound) {
				Page = "Webpage can be ignored";
			} else {
				if (logging)
					Dbutil.logger.warn("Cannot find web page after " + (attempt) + " tries. "
							+ (responsecode > 0 ? "Response code is " + responsecode + ". " : "") + "Problem url= "
							+ trimmedurl + ", error: " + exceptionmessage + " Shop ID: " + id + " Shop Name: "
							+ shopName);
				if (Page.startsWith("Webpage")) {
					Page = "Webpage unavailable, response code=" + responsecode;
				} else {
					Page = "Webpage unavailable, response code=" + responsecode + " " + Page;
				}
			}
		}

		html = Page;

	}

	public void readHttpsFeedData(String trimmedurl, boolean pdf, boolean excel) {

		int attempt = 0;
		succes = false;
		if (postdata == null) {
			postdata = "";
		}
		String exceptionmessage = "";
		// If postdata has the form "GET&param1=Yes, then all GET params in URL will be
		// added to the post parameters.
		if (trimmedurl.indexOf("?", 0) > 0 && postdata != null && postdata.startsWith("GET&")) {
			postdata = trimmedurl.substring(trimmedurl.indexOf("?", 0) + 1) + "&" + postdata.substring(4);
		}
		if (postdata != null && postdata.startsWith("GET&")) {
			postdata = postdata.substring(4);
		}
		// If trimmedurl has the form "...GET&param1=Yes, then all params in URL after
		// GET& will be POSTed
		if (trimmedurl.contains("GET&")) {
			if (!postdata.equals("")) {
				postdata = postdata + "&";
			}
			postdata = postdata + trimmedurl.substring(trimmedurl.indexOf("GET&") + 4);
			trimmedurl = trimmedurl.substring(0, trimmedurl.indexOf("GET&"));
		}

		if (trimmedurl.startsWith("xls:")) {
			type = "application/vnd.ms-excel";
			trimmedurl = trimmedurl.substring(4);
			excel = true;
			// Page=Excelreader.ReadUrl(trimmedurl.substring(4));
		} else if (trimmedurl.toLowerCase().endsWith("xls")) {
			type = "application/vnd.ms-excel";
			excel = true;
			// Page=Excelreader.ReadUrl(trimmedurl);
		}
		trimmedurl = Spider.replaceString(trimmedurl, " ", "%20");
		URL url = null;

		HttpsURLConnection urlcon = null;
		// long lastmodified;
		String inputLine;
		StringBuffer sb = new StringBuffer();
		badurl = false;
		try {
			System.setProperty("https.protocols", "TLSv1.2");
			System.setProperty("http.agent", "");
			// System.setProperty("http.agent", "Chrome");

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, null, null);
			SSLContext.setDefault(context);

			url = new URL(trimmedurl);
		} catch (Exception exc) {
			Dbutil.logger.warn("Foute URL " + trimmedurl + " Shop ID: " + id + " Shop Name: " + shopName);
			badurl = true;
			// throw new MalformedURLException();
		}

		if (logging)
			Dbutil.logger.debug("Starting to get web page for" + " Shop ID: " + id + " Shop Name: " + shopName);

		if (url != null)
			while (succes == false && attempt < maxattempts) {
				try {
					attempt++;
					if (trimmedurl.substring(0, 8).equals("http://")) {
						System.out.println("http");
					} else if (trimmedurl.substring(0, 8).equals("https://")) {
						System.out.println("https");
					}

					if (proxy == null) {
						urlcon = (HttpsURLConnection) url.openConnection();
					} else {
						urlcon = (HttpsURLConnection) url.openConnection(proxy);
					}
					urlcon.addRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
					urlcon.setConnectTimeout(Configuration.webpagetimeout);
					urlcon.setReadTimeout(Configuration.webpagetimeout);
					// urlcon.setRequestProperty("User-Agent",useragent);
					// urlcon.setInstanceFollowRedirects(followredirect);
					urlcon.setInstanceFollowRedirects(false);
					if (headers != null && headers.contains("=")) {
						for (String h : headers.split(";")) {
							urlcon.setRequestProperty(h.split("=")[0], h.split("=")[1]);

						}
					}
					urlcon.setRequestProperty("Accept-Language", "en-us");
					String cookies = standardcookie + getCookie();
					if (allowcookies && cookies.length() > 0) {
						urlcon.setRequestProperty("Cookie", cookies);
					}
					if (postdata != null && !postdata.equals("")) {
						DataOutputStream printout;
						urlcon.setDoInput(true);
						// Let the RTS know that we want to do output.
						urlcon.setDoOutput(true);
						// No caching, we want the real thing.
						urlcon.setUseCaches(false);
						// Specify the content type.
						urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						// Send POST output.
						printout = new DataOutputStream(urlcon.getOutputStream());

						printout.writeBytes(postdata);
						printout.flush();
						printout.close();

					}

					// lastmodified=urlcon.getLastModified();
					if (encoding == null || encoding.equals("")) {
						encoding = "ISO-8859-1";
					}
					if (!java.nio.charset.Charset.isSupported(encoding)) {
						// Dbutil.logger.info("Charset "+encoding+" is not supported for URL
						// "+trimmedurl+", trying with ISO-8859-1");
						encoding = "ISO-8859-1";
					}
					String[] cookieValarray;
					List<String> cookieval = urlcon.getHeaderFields().get("Set-Cookie");
					if (cookieval != null) {
						// Dbutil.logger.info(urlcon.getHeaderField(i));
						Iterator<String> cookieI = cookieval.iterator();
						while (cookieI.hasNext()) {
							cookieValarray = cookieI.next().split(";");
							String cookieVal = cookieValarray[0];
							try {
								String key = (cookieVal.split("=")[0]);
								String value = cookieVal.substring(key.length() + 1);
								FWSCookie thiscookie = new FWSCookie(key, value);
								setCookie(thiscookie);
							} catch (Exception e) {
								Dbutil.logger.info(
										"Invalid cookie: " + cookieVal + " Shop ID: " + id + " Shop Name: " + shopName);
							}
						}
					}

					if (pdf) {
						sb.append(newPDFReader(urlcon.getInputStream()));
						succes = true;
						Page = sb.toString();
						sb = null;
					} else if (urlcon.getResponseCode() >= 300 && urlcon.getResponseCode() < 400 && followredirect) {
						redirects++;
						if (redirects < maxredirects) {
							String newloc = urlcon.getHeaderField("Location");
							postdata = "";
							Thread.sleep(5000);
							urlstring = Spider.padUrl(newloc, urlstring, getBaseUrl(urlstring), "n.a.");
							urlcon.disconnect();
							readPageRecursive();

						} else {
							Dbutil.logger.error("Too many redirects for url " + urlstring + " Shop ID: " + id
									+ " Shop Name: " + shopName);
						}

					} else {
						type = urlcon.getContentType();
						if (type != null
								&& type.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
							excel = true;
						if (type != null && type.contains("application/vnd.ms-excel"))
							excel = true;
						InputStream inpstr = null;
						try {
							inpstr = urlcon.getInputStream();
						} catch (Exception e) {
							inpstr = urlcon.getErrorStream();
							sb.append("Webpage generated an error");
						}
						if (excel) {
							Page = Excelreader.ReadUrl(inpstr, trimmedurl);
							responsecode = urlcon.getResponseCode();
							if (Page != null && Page.length() > 100 && !Page.startsWith("Webpage"))
								succes = true;
							inpstr.close();
						} else {
							BufferedReader in = new BufferedReader(new InputStreamReader(inpstr, encoding));
							while ((inputLine = in.readLine()) != null) {
								sb.append(inputLine);
								sb.append("\n");
							}
							inpstr.close();
							in.close();
							responsecode = urlcon.getResponseCode();
							urlcon.disconnect();
							Page = sb.toString();

						}

						sb = null;
						if (logging)
							Dbutil.logger.debug("Page retrieved from url " + trimmedurl + " Shop ID: " + id
									+ " Shop Name: " + shopName);
						succes = true;
					}

				} catch (Exception exc) {
					Dbutil.logger.error("", exc);
					exceptionmessage = exc.toString();
					if (!ignorepagenotfound) {
						if (logging)
							Dbutil.logger.debug("Cannot find web page, attempt " + attempt + ", URL = " + trimmedurl
									+ " Shop ID: " + id + " Shop Name: " + shopName, exc);
					}
					try {
						if (attempt < maxattempts) {
							Thread.sleep(errorpause * 1000);
						}
						responsecode = urlcon.getResponseCode();
						urlcon.disconnect();
					} catch (Exception e) {
						if (logging)
							Dbutil.logger.debug("Web page problem. " + " Shop ID: " + id + " Shop Name: " + shopName,
									e);
					}
				}
			}
		if (succes == false) {
			try {
				responsecode = urlcon.getResponseCode();
			} catch (Exception e) {
			}
			if (responsecode == 404 || ignorepagenotfound) {
				Page = "Webpage can be ignored";
			} else {
				if (logging)
					Dbutil.logger.warn("Cannot find web page after " + (attempt) + " tries. "
							+ (responsecode > 0 ? "Response code is " + responsecode + ". " : "") + "Problem url= "
							+ trimmedurl + ", error: " + exceptionmessage + " Shop ID: " + id + " Shop Name: "
							+ shopName);
				if (Page.startsWith("Webpage")) {
					Page = "Webpage unavailable, response code=" + responsecode;
				} else {
					Page = "Webpage unavailable, response code=" + responsecode + " " + Page;
				}
			}
		}

		html = Page;

	}

}