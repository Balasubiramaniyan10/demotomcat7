<%@page import="com.searchasaservice.parser.xpathparser.Result"%><%@ page 

	import="com.freewinesearcher.common.Context"
	import="com.freewinesearcher.common.Dbutil"
	%><%
	Context c = (Context) session.getAttribute("context");
	if (c == null) {
		c = new Context(request);
		session.setAttribute("context", c);
	}
	//Dbutil.logger.info(session.getId());
	String counter = request.getParameter("counter");
	String action = request.getParameter("action");
	String fieldstr = request.getParameter("field");
	int field=0;
	try{field=Integer.parseInt(fieldstr);}catch(Exception e){}
	if (field<100){
	if(c.an!=null){
		if ("newPath".equals(action)) {
			session.setAttribute("oldpath",c.an.config.get(field).xpath);
			session.setAttribute("field",field);
			out.write(c.an.newPath(counter,field));
			
		}
		if ("getColors".equals(action)) {
			out.write("{\"color\":[");
			for (int i=1;i<c.an.config.size();i++){
				if(i>1) out.write(",");
				out.write("\""+c.an.currentcolors[i]+"\"");
			}
			out.write("]}");
		}
		
		if ("savexp".equals(action)) {
			if(c.an.shopid>0){
				long id=c.an.save(c);
				if (id>0) {
					out.write("saved");
					c.an=null;
				} else {
					out.write("error");
				}
			} else {
				session.setAttribute("savexp",true);
				out.write("notsaved");
				//Dbutil.logger.info("notsaved");
				
			}
		}
		if ("clearAppendWineryField".equals(action)){
			c.an.config.appendwineryname=false;
			
		}
		if ("setAppendWineryField".equals(action)){
			c.an.config.appendwineryname=true;
			
		}
		if ("undonewPath".equals(action)) {
			int oldfield=(Integer)session.getAttribute("field");
			c.an.config.get(oldfield).xpath=(String)(session.getAttribute("oldpath"));
			session.removeAttribute("oldpath");
			session.removeAttribute("field");
			c.an.tagDocument(c.an.getOriginalDocument());
			c.an.highlightnodes(c.an.result);
			out.write(c.an.getTaggedDocumentAsString());
		}
		if ("savenewPath".equals(action)) {
			session.removeAttribute("oldpath");
			session.removeAttribute("field");
			//c.an.taggeddocument=c.an.taggedDocument(c.an.getOriginalDocument());
			c.an.result=c.an.parse();
			c.an.highlightnodes(c.an.result);
			out.write(c.an.getTaggedDocumentAsString());
		}
			/*
	if ("getSeriesPath".equals(action)) {
		out.write(c.an.getSeriesPath(new String[0]));
	}
	*/
	if ("clearPaths".equals(action)) {
		c.an.clearPaths();
	}
	if ("progress".equals(action)) {
		out.write(c.an.progress);
	}
	if ("clearHighlights".equals(action)) {
		c.an.tagDocument(c.an.getOriginalDocument());
		out.write(c.an.getTaggedDocumentAsString());
	}
	
	/*
	if ("gethighlights".equals(action)) {
		out.write(c.an
				.getHighlightedHTML(c.an.getSeriesPath(new String[0])));
	}
	*/
	if ("analyze".equals(action)) {
		Result result=c.an.analyseDocument(39, 1);
		if (result==null||result.size()==0){
			out.write("Error");
			if (result!=null) Dbutil.logger.info(result.size());
			if (result!=null) Dbutil.logger.info(result.toArray());
		} else {
			c.an.highlightnodes(c.an.result);
			
		}
		
	}
	}
	} else {
		out.write(c.an.newNextPage(counter,field));
	}
%>