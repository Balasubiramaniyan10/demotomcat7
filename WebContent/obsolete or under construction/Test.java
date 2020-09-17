package com.vinopedia.htmlunit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.w3c.dom.Node;

import com.freewinesearcher.common.Dbutil;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.searchasaservice.parser.xpathparser.Analyzer;



public class Test {

	public void xpath() throws Exception {
	    final WebClient webClient = new WebClient();
	    final HtmlPage page = webClient.getPage("http://www.christophercreek.com/wine/");
	    //Dbutil.logger.info(Analyzer.getNodeAsXML(((Node)page).getOwnerDocument()));
	    //get list of all divs
	    final List<?> divs = page.getByXPath("//div");
	    Dbutil.logger.info(divs.size());

	    //get div which has a 'name' attribute of 'John'
	    final HtmlDivision div = (HtmlDivision) page.getByXPath("//div[@name='John']").get(0);
	    System.out.println(div.asText());
	    webClient.closeAllWindows();
	}
	
	public static void main(String[] args){
		try {
			HtmlUnitParser h=new HtmlUnitParser("http://www.christophercreek.com/wine/");
			Dbutil.logger.info(Analyzer.getNodeAsXML(h.document));
		} catch (FailingHttpStatusCodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Test hup = new Test();
		try {
			hup.xpath();
		} catch (Exception e) {
			System.out.print("Problem: ");
			e.printStackTrace();
		}
	}
}
