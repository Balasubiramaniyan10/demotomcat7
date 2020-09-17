<%@page import="com.freewinesearcher.online.Webroutines"%><%@page import="com.freewinesearcher.batch.Spider"%><%@page import="com.freewinesearcher.common.Dbutil"%><%// called from /snippets/map.jsp to change coordinates
try{
int regionid=Integer.parseInt(request.getParameter("regionid"));
double lat=Double.parseDouble(request.getParameter("latitude"));
double lon=Double.parseDouble(request.getParameter("longitude"));
int result=Webroutines.updateCoordinates(request,regionid,lat,lon);
if (result>0) {
	out.write("OK");
} else {
	Dbutil.logger.info("Could not save location");
	out.write("NOK");
}
}catch(Exception e){out.write("Error!");}
%>