<%@page import="com.freewinesearcher.common.Configuration"%><%
String ipaddress;
boolean ok=true;
ipaddress = request.getRemoteAddr();
if (ipaddress.contains(Configuration.devpcip)||ipaddress.contains("127.0.0.1")){
	ok=false;
}
if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
	ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
	if (ipaddress.contains(Configuration.devpcip)||ipaddress.contains("127.0.0.1")){
		ok=false;
	}
}
	if (request.getHeader("X-Forwarded-For") != null) {
		ipaddress = request.getHeader("X-Forwarded-For");
		if (ipaddress.contains(Configuration.devpcip)||ipaddress.contains("127.0.0.1")){
			ok=false;
		}
	}

if (ok) {
	out.print("OK:"+ipaddress);
} else {
	out.print(ipaddress);
	System.out.println("IP: "+ipaddress);
	System.out.println(request.getHeader("X-Forwarded-For"));

}

%>