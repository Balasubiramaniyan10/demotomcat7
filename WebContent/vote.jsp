<%@page import="com.freewinesearcher.common.Dbutil"
import = "com.freewinesearcher.online.web20.*"
%>
<%	Publication publication=(Publication)session.getAttribute("publication");
	String result="Problem";
	if (publication!=null){
		publication.update(request);
		session.setAttribute("publication",publication);
		int vote=0;
		try{vote=Integer.parseInt(request.getParameter("vote"));}catch (Exception e){}
		if (vote>0) {
			Dbutil.logger.info(vote);
			publication.content.rating=new Rating(vote+"");
			if (publication.isValid(request)){
				boolean saveok=publication.save();
				session.setAttribute("publication",publication);
				result="Updated";
			}
		}
	}
	out.write(result);
%>