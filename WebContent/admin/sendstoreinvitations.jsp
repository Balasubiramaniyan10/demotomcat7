
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.batch.Spider"%><html><body>
<% int skip =1;
	try{skip=Integer.parseInt(request.getParameter("skip"));}catch(Exception e){}
	int wsid=Shopapplication.getnextid(skip);
	String action=request.getParameter("actie");
	String comment=request.getParameter("comment");
	if (comment==null) comment="";
	if (comment.length()>0) Dbutil.executeQuery("update wsshops set comment='"+Spider.SQLEscape(comment)+"' where wsid="+wsid);
	if (action!=null&&action.equals("Send")){
		if (request.getParameter("email")!=null&&!request.getParameter("email").equals("")){
		if (Shopapplication.mailRequest(wsid,request.getParameter("email"),comment)){
			out.write("Email sent.");
			skip++;
		} else {
			out.write("Problem while sending email to "+request.getParameter("email"));
		}
		} else {
			out.write("Empty email adress.");
		}
		
	} else if (action!=null&&action.equals("Messagebox")){
		out.write(Shopapplication.sentwithtextbox(wsid,comment));
			skip++;
		} else{
		skip++;
	}
	wsid=Shopapplication.getnextid(skip);
	Shopapplication sa=Shopapplication.generate(wsid);
	
%>


<%@page import="com.freewinesearcher.online.Shopapplication"%>
<form action='sendstoreinvitations.jsp'>Store email address: <input type="text" name="email" value="<%=sa.getStoreemailaddressforvp() %>"><input type="hidden" name="skip" value="<%=skip %>"/><input type="hidden" name="version" value="1"/><br/>Comment:     <textarea id='comment' name='comment'></textarea><br/><input type='submit' name='actie' value='Send'/><input type='submit' name='actie' value='Messagebox'/><input type='submit' name='actie' value='Skip'/></form>
<jsp:include page="/getstoreinvitation.jsp" flush="true" >
<jsp:param name="id" value="<%=wsid %>" />
<jsp:param name="version" value="1" />
<jsp:param name="password" value="<%=sa.showPassword() %>" />

</jsp:include>
