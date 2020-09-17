<% try{request.setCharacterEncoding("UTF-8");%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page   
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.WineAdvice"
	import = "com.freewinesearcher.online.ShopAdvice"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Region"
	import="com.freewinesearcher.online.Guidedsearch"
	import="com.google.gdata.data.DateTime"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Wine"%>
<% 	PageHandler p=PageHandler.getInstance(request,response);
	String beantype= "com.freewinesearcher.online.WineAdvice";
	if (request.getParameter("shopid")!=null) beantype="com.freewinesearcher.online.ShopAdvice";
	%>

<jsp:useBean id="advice" beanName="<%=beantype%>" type="com.freewinesearcher.online.WineAdvice" scope="request" />
<%	
	for (Object o:request.getParameterMap().keySet()) try{if (request.getParameter((String)o).equals("undefined")) Dbutil.logger.info(o+": "+(String)(request.getParameter((String)o)));}catch(Exception e){}	 %>
<%try{ %>
<jsp:setProperty name="advice" property="*"/>
<%	}catch(Exception e){
	Dbutil.logger.warn("AdviceHTML recieved a non integer value where it expected an integer! ");
	Dbutil.logger.warn("IP:"+request.getRemoteAddr());
	Dbutil.logger.warn("Browser info"+request.getHeader("user-agent"));
	for (Object o:request.getParameterMap().keySet()){
		String val=request.getParameter(o+"");
		Dbutil.logger.info("Parameter "+o+" has value "+val);
	}
	}

	long start=DateTime.now().getValue();
	boolean log=false;
	String section=request.getParameter("section");
	if (section==null) section="";
	boolean newsearch=false;
	try{newsearch=Boolean.parseBoolean(request.getParameter("newsearch"));}catch(Exception e){};
	if (!section.equals("getwinehtml")&&!section.equals("getwinejson")&&!section.equals("gs")) advice.getAdvice();
	String guidedsearch="";
	if (newsearch||section.equals("gs")){
		Guidedsearch gs=new Guidedsearch();
	    gs.advice=advice;
	    if (request.getParameter("shopid")!=null) gs.bycountryofseller=false;
	    //session.setAttribute("guidedsearch",gs);
	    guidedsearch=gs.getSearchHtml(); 
	    if (newsearch){
	    	p.getLogger().type=gs.advice.loggerinfo;   	
    		p.getLogger().name=gs.advice.searchinfo;
    		if (gs.advice.shopid>0) p.getLogger().shopid=gs.advice.shopid+"";
        	
    		log=true;
	    }
	    
	}
    if (section.equals("")){
		out.write("{\"facets\":\""+guidedsearch+"\"");
    	if (!newsearch) {
    		if (true) try{
    			int pagenr=advice.getPage();
    			String advstr="";
    			advstr=advice.getAdviceHTML();
    			if (advstr!=null&&advstr.length()>1) advstr=advstr.substring(1);
    			out.write(",\"page\":\"");
    			out.write(pagenr+"\",");
    			out.write(advstr);
			}catch (Exception e){
				Dbutil.logger.error("Advice:"+advice.hashString());
				Dbutil.logger.error("Exception in this advice:",e);
    			
    		}
    		
    		//out.write(",\"page\":\""+advice.getPage()+"\","+advice.getAdviceHTML().substring(1));
    	} else {
    		out.write("}");
    	}
    	p.getLogger().type=advice.loggerinfo;
    	p.getLogger().name=advice.searchinfo;
    	if (advice.shopid>0) p.getLogger().shopid=advice.shopid+"";
    	log=true;
    } else {
    	if (section.equals("results")) out.write(advice.getAdviceHTML());
    	if (section.equals("gs")) out.write(guidedsearch);
    	if (section.equals("getwinejson")) {
    		Wine w=new Wine(request.getParameter("wineid"));
    		p.getLogger().type="Store wineinfo";
    		p.getLogger().name=w.Name;
        	p.getLogger().shopid=w.ShopId+"";
        	p.getLogger().knownwineid=w.Knownwineid;
        	p.getLogger().wineid=w.Id+"";
        	p.getLogger().price=w.PriceEuroEx.floatValue();
        	p.getLogger().logaction();    
        	log=false;
    		if (session.getAttribute("lasturl")!=null) session.setAttribute("lasturl",((String)session.getAttribute("lasturl")).replaceAll("[?&]wineid=.*","")+"&wineid="+w.Id);
    		out.write(advice.getWineJson(request.getParameter("wineid"),p));
    		session.setAttribute("lasturl","/store/?wineid="+w.Id);
    	}
    	if (section.equals("getadjson")) {
    		Wine w=new Wine(request.getParameter("wineid"));
    		out.write(advice.getAdJson(request.getParameter("wineid"),p));
    	}
    	if (section.equals("getwinehtml")) out.write(advice.getWineHtml(request.getParameter("wineid")));
    	
    	
    }
	if (p.getLogger().type.length()>0) {
		if (log) {
			p.getLogger().logaction();    
			
		}
	}
   	if ((DateTime.now().getValue()-start)>3000){
    	Dbutil.logger.info("Wineadvice total:"+(DateTime.now().getValue()-start)+" ms. "+advice.timerlog);
   	}
 
} catch (IllegalStateException e){
	Dbutil.logger.info("advicehtml.jsp: request object had been recycled");
}

 %>