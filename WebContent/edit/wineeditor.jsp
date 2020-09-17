<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.HashSet"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.freewinesearcher.online.KnownwineEditor"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="com.freewinesearcher.online.web20.CommunityUpdater"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<jsp:useBean id="kwe" class="com.freewinesearcher.online.KnownwineEditor" scope="request"/>
<jsp:setProperty property="*" name="kwe"/>

<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%	String action=request.getParameter("action");
	if (action==null) action="";
	if (action.equals("get")) kwe=new KnownwineEditor(kwe.getKnownwineid(),kwe.getCode());
	kwe.check();

	Auditlogger al=new Auditlogger(request);
	al.userid=request.getRemoteUser();
	if (al.userid==null) al.userid="Winery "+kwe.getProducerid();
	al.setAction("Editing Knownwine");
	al.setObjecttype("Knownwine");
	al.setObjectid(kwe.getKnownwineid()+"");
	
	cu.setAl(al);
	cu.setId(kwe.getProducerid());
	cu.setTablename("kbproducers");
	cu.setIdcolumn("id");
	cu.setAccesscode(kwe.getCode());
	boolean edit=false;
	if (request.isUserInRole("admin")||cu.validAccessCode()) {
		edit=true;
		kwe.setOverridesecurity(request);
		
	}
	if (edit){
		HashMap<Integer,String> grapes=KnownwineEditor.getOptions("grapes","id","grapename");
		HashMap<Integer,String> appellations=KnownwineEditor.getOptions("kbregionhierarchy","id","region");
		HashSet<String> types=KnownwineEditor.getOptions("knownwines","type");

%>
<%@page import="com.freewinesearcher.online.Producer"%>
<%@page import="com.freewinesearcher.online.Producerinfo"%>
<head>
<title>
Edit wines from winery <%=(kwe.getProducer().replaceAll("&","&amp;"))%>
</title>
<% PageHandler p=PageHandler.getInstance(request,response,"Winery");%>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head> 
<body>
<%@ include file="/snippets/topbar.jsp" %>
<%@ include file="/snippets/logoandsearch.jsp" %>
<div class='main'>
<br/>
<% if (kwe.getKnownwineid()>0){
		out.write("You are editing wine "+kwe.getWine()+"<br/><br/>");
	} else {
		out.write("You are entering a new wine.<br/><br/>");
	}
if (action.equals("Save")){
	kwe.check();
	if (kwe.isValid()) {
		if (kwe.save(request)){
			out.write("<span style='color:green'>Changes were saved. It may take up to 48 hours for all changes to show up on the web site.</span> ");%><a href='/winery/<%=(Webroutines.URLEncodeUTF8Normalized(kwe.getProducer()).replaceAll("%2F", "/").replace("&", "&amp;")+(kwe.getCode()!=null?"&amp;accesscode="+kwe.getCode():""))%>#wines'>Back to winery overview</a><% 
			al.setInfo("Save successful");
		} else {
			if (kwe.errormessage!=null&&kwe.errormessage.length()>0){
				out.write("<span style='color:red'>"+kwe.errormessage+"</span>");
				al.setInfo("Save failed");
			} else {
				out.write("<span style='color:red'>A system error occurred during the processing of the changes. Our staff was notified of this fact, please try again later.</span>");
				al.setInfo("Save failed");
			}
		}
		al.logaction();
	} else {
		out.write(kwe.check()+"<br/>");
	}
}

%><form action="wineeditor.jsp" method="post" autocomplete="off"">
<input type='hidden' name='knownwineid' value='<%=kwe.getKnownwineid() %>'/>
<input type='hidden' name='producerid' value='<%=kwe.getProducerid() %>'/>
<input type='hidden' name='code' value='<%=kwe.getCode() %>'/>

<table>
<tr><td>Winery:</td><td><%=kwe.getProducer()%></td></tr>
<tr><td>Wine name:</td><td><input type='text' name='wine' value='<%=kwe.getWine().replaceAll("'","&apos;") %>' size='50'/>Please enter a wine name that completely identifies the wine, starting with the name of the winery (<%=kwe.getProducer()%>). This is how this wine will be displayed to the public.</td></tr>
<tr><td>Region or appellation:</td><td><select name='regionid'><option value="0">Please select...</option><%for (int regionid:appellations.keySet()) {if (regionid>100) out.write("<option value='"+regionid+"' "+(regionid==kwe.getRegionid()?" selected=\"selected\"":"")+">"+appellations.get(regionid).replaceAll("&","&amp;")+"</option>");} %></select></td></tr>
<tr><td>Grapes used in this wine:</td><td><select name='grapeid'><option value="0">Please select...</option><%for (int grapeid:grapes.keySet()) out.write("<option value='"+grapeid+"' "+(grapeid==kwe.getGrapeid()?" selected='selected'":"")+">"+grapes.get(grapeid)+"</option>"); %></select></td></tr>
<tr><td>Name of the cuv&eacute;e:</td><td><input type='text' name='cuvee' value='<%=kwe.getCuvee().replaceAll("'","&apos;") %>' size='50'/></td></tr>
<tr><td>Name of the vineyard:</td><td><input type='text' name='vineyard' value='<%=kwe.getVineyard().replaceAll("'","&apos;") %>' size='50'/></td></tr>
<tr><td>Type of wine:</td><td><select name='type'><%for (String type:types) out.write("<option value='"+type+"' "+(type.equals(kwe.getType())?" selected='selected'":"")+">"+type+"</option>"); %></select></td></tr>
<tr><td>Wine description:</td><td><textarea name='winerynote'  rows='10' cols='80'/><%=kwe.getWinerynote() %></textarea></td></tr>
<tr><td></td><td>Things you may want to mention in the wine description: The exact blend, yield, average number of bottles made in a vintage, average age of the vines, vinification details (duration of fermentation, use of oak or steel barrels, filtration, destemming). Also, a tasting note can be added.</td></tr>
<tr><td>Disabled (not a valid wine):</td><td><input type="checkbox" name="disabled" <%if (kwe.isDisabled()) out.write("checked='checked' "); %>/></td></tr>

</table><br/><input type="submit" value="Save" name="action"/> <br/><a href='/winery/<%=(Webroutines.URLEncodeUTF8Normalized(kwe.getProducer()).replaceAll("%2F", "/").replace("&", "&amp;")+(kwe.getCode()!=null?"?accesscode="+kwe.getCode():""))%>#wines'>Back to winery overview</a>
</form>


<%
		
	
}%>
<%@ include file="/snippets/footer.jsp" %>
<% } else {
out.write("Invalid access code");
}%>
	
</div>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>