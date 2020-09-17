package com.vinopedia.htmlunit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.freewinesearcher.common.Dbutil;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.searchasaservice.parser.xpathparser.Parser;

public class HtmlUnitParser implements Parser {
	Document document;
	public HtmlPage page;
	
	public HtmlUnitParser(){
		document=null;
		
	}
	
	public HtmlUnitParser(String url, String postparams) throws FailingHttpStatusCodeException, MalformedURLException, IOException{
		final WebClient webClient = new WebClient();
		webClient.setJavaScriptEnabled(true);
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setThrowExceptionOnFailingStatusCode(false);
		webClient.setCssEnabled(false);
		if (postparams!=null&&postparams.length()>0){
			// Instead of requesting the page directly we create a WebRequestSettings object
			WebRequest request = new WebRequest(
			  new URL(url), HttpMethod.POST);

			// Then we set the request parameters
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			for (String p:postparams.split("&")){
				params.add(new NameValuePair(p.split("=")[0], p.split("=")[1]));
			}
			request.setRequestParameters(params);
			
			// Finally, we can get the page
			page = webClient.getPage(request);
			
		} else {
		
			page = webClient.getPage(url);
		}
		//List<NameValuePair> params = page.getWebResponse().getWebRequest().getRequestParameters();
	    //Dbutil.logger.info(params.size()+" parameters");
	    //for (NameValuePair param:params) Dbutil.logger.info(param.getName()+":"+param.getValue());
	    document=(Document)page;
	}

	public Document getDocument() {
		return document;
	}
}
