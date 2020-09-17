package com.vinopedia.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;

import static junit.framework.Assert.*;

public class BatchTest extends TestCase{
	
	public BatchTest(String test){
		super(test);
	}
	
	public BatchTest(){
		super();
	}
	
	@Test
	public void testSearchWines() {
		Webpage webpage=new Webpage();
		webpage.urlstring="https://localhost.vinopedia.com:6001/wine/Leoville Cases";
		Dbutil.executeQuery("Delete from wines where shopid=4;");
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Wijnzoeker.shoptodebug=4;
		Wijnzoeker.auto="";
		Wijnzoeker.type=1; //Normal shops
		webpage.readPage();
		assertEquals("",Webroutines.getRegexPatternValue("(My wine shop)", webpage.html));
		wijnzoeker.updateSites();
		webpage.readPage();
		assertEquals(Webroutines.getRegexPatternValue("(My wine shop)", webpage.html), "My wine shop");
		
	}

}
