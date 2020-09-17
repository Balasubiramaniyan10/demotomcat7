<%@	page %>
<%@page import="java.util.ArrayList"%>
<%@page import="com.freewinesearcher.online.Webroutines"%>

<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.winebottle.Bottle"%>
<%@page import="com.freewinesearcher.online.winebottle.WineBottle"%>

<%@page import="com.freewinesearcher.common.Knownwines"%><html>
<head>
<title>Recommended Wines Ad Editor</title>
</head>
<body>
<jsp:include page="adminlinks.jsp" />
<jsp:useBean id="wb" class="com.freewinesearcher.online.winebottle.WineBottle" scope="session"/>
<jsp:setProperty name="wb" property="url" value=""/>
<jsp:setProperty name="wb" property="region" value=""/>
<jsp:setProperty name="wb" property="*" />


<br><br>
<% 
ArrayList shops = Webroutines.getShopList("");
int shopid=3;
try{shopid=Integer.parseInt(request.getParameter("shopid"));}catch (Exception e){}
String action=request.getParameter("actie");
if (action==null) action="";
Integer wineid=(Integer)session.getAttribute("wineid");
if (wineid==null||wineid==0) wineid=Dbutil.readIntValueFromDB("select * from wines left join wineads on (wines.id=wineads.wineid) where wines.shopid=3 and knownwineid>0 having wineads.imagetype is null order by wineid limit 1;","id");
if (action.equals("Next")){
	Dbutil.executeQuery("Delete from wineads where wineid="+wineid+";");
	Dbutil.executeQuery("Update wineads set wineid="+wineid+" where wineid=1;");
	wineid=Dbutil.readIntValueFromDB("select * from wines left join wineads on (wines.id=wineads.wineid) where wines.shopid=3 and knownwineid>0 and id>"+wineid+" having wineads.imagetype is null order by wineid limit 1;","id");
	wb=new WineBottle(wineid);
	
} else if (action.equals("Skip")){
	wineid=Dbutil.readIntValueFromDB("select * from wines left join wineads on (wines.id=wineads.wineid) where wines.shopid=3 and knownwineid>0 and id>"+wineid+" having wineads.imagetype is null order by wineid limit 1;","id");
	wb=new WineBottle(wineid);
	
}
if (wb.winename==null||wb.winename.equals(""))	wb=new WineBottle(wineid);
session.setAttribute("wineid",wineid);
wb.wineid=0;
Dbutil.executeQuery("Delete from wineads where wineid=1;");
if (action.equals("Test")){
Thread t=new Thread (wb);
t.start();
int counter=0;
boolean ready=false;
while (!ready&&counter<30){
	counter++;
	ready=(Dbutil.readIntValueFromDB("select count(*) as thecount from wineads where wineid=1 and image!='';","thecount")>0);
	if (!ready) Thread.sleep(1000);
}
}
session.setAttribute("wb",wb);
%>
	
<FORM ACTION="recommendedadeditor.jsp" id="formOne">
Select shop to edit<select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((Integer.parseInt(shops.get(i).toString())==(shopid))) out.print(" selected='selected'");%>><%=shops.get(i+1)%>
<%}%>
<input type="hidden" name="actie" value="changeshop"/>
</select>
</FORM>
<img src='/images/gen/winead/1'/>
<form action="recommendedadeditor.jsp" method="post">
<table>
<tr><td>Wine name</td><td><input type="text" size="120" name="winename" value='<%=wb.winename.replaceAll("'","&quot;")%>'/></td></tr>
<tr><td>Vintage</td><td><input type="text" size="120" name="vintage" value='<%=wb.vintage.replaceAll("'","&quot;")%>'/></td></tr>
<tr><td>Region</td><td><input type="text" size="120" name="region" value='<%=wb.region.replaceAll("'","&quot;")%>'/></td></tr>
<tr><td>Price</td><td><input type="text" size="120" name="price" value='<%=wb.price.replaceAll("'","&quot;")%>'/></td></tr>
<tr><td>URL label image</td><td><input type="text" size="120" name="url" value='<%=wb.url%>'/></td></tr>
<tr><td>Wine color</td><td><select name="color" >
<option value='RED' <%=(wb.winecolor.equals(Bottle.winecolors.RED)?"selected=selected ":"") %>>Red</option>
<option value='WHITE' <%=(wb.winecolor.equals(Bottle.winecolors.WHITE)?"selected=selected ":"") %>>White</option>
<option value='WHITESWEET' <%=(wb.winecolor.equals(Bottle.winecolors.WHITESWEET)?"selected=selected ":"") %>>White Sweet</option>
<option value='ROSE' <%=(wb.winecolor.equals(Bottle.winecolors.ROSE)?"selected=selected ":"") %>>Rosé</option>
</select>
</td></tr>
</table>
<input type='hidden' name='wineid' value='<%=wineid %>'/>
<input type='Submit' name='actie' value='Test'/>
<input type='Submit' name='actie' value='Skip'/>
<input type='Submit' name='actie' value='Next'/>
</form>
<a href='http://images.google.com/images?q=<%=Webroutines.URLEncodeUTF8(wb.winename.replaceAll("['\"-]","")) %>&sourceid=navclient-ff&rlz=1B3GGGL_enNL303NL303&um=1&ie=UTF-8&sa=N&hl=en&tab=wi' target='_blank'>Find label on Google</a><br/>
<a href='http://www.cellartracker.com/list.asp?Pending=False&amp;Table=LabelImage&amp;Page=0&amp;szSearch=<%=Webroutines.URLEncodeUTF8((wb.knownwineid>0?Knownwines.getKnownWineName(wb.knownwineid):wb.winename.replaceAll("['\"-]",""))) %>' target='_blank'>Find label on Cellartracker (<%=(wb.knownwineid>0?Knownwines.getKnownWineName(wb.knownwineid):wb.winename.replaceAll("['\"-]","")) %>)</a>
</body>
</html>