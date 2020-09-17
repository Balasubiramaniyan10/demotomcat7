package com.searchasaservice.parser;

import java.net.MalformedURLException;

import javax.swing.JFrame;
import org.lobobrowser.html.domimpl.NodeImpl;
import org.lobobrowser.html.gui.*;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.test.*;
import org.w3c.dom.Document;

import com.freewinesearcher.common.Dbutil;
import com.searchasaservice.parser.xpathparser.Analyzer;

public class LoboBrowser {
	String url;
	Document document;

	public LoboBrowser(String url){
		this.url=url;
	    //JFrame window = new JFrame();
	    HtmlPanel panel = new HtmlPanel();
	    //window.getContentPane().add(panel);
	    //window.setSize(600, 400);
	    //window.setVisible(true);
	    SimpleHtmlRendererContext p = new SimpleHtmlRendererContext(panel, new SimpleUserAgentContext());
	    
	    try {
			p.navigate(url);
			
		 Thread.sleep(3000);
			NodeImpl n = panel.getRootNode();
		    document=n.getOwnerDocument();
		 //Dbutil.logger.info(Analyzer.getNodeAsXML(doc));
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}
	
	public static void main(String[] args){
		new LoboBrowser("https://test.vinopedia.com/rayastestpage.html");
	}
}
