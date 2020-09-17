<%@ page
import = "java.util.ArrayList"
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" 
import="com.freewinesearcher.common.Configuration"%>
<% 
	String refineHTML=Webroutines.getRefineResultsHTML(PageHandler.getInstance(request,response).searchpage,PageHandler.getInstance(request,response).s.wineset,PageHandler.getInstance(request,response).t,PageHandler.getInstance(request,response).searchdata,20);
 	String suggestionHTML=""; //Performance issue: Webroutines.getSuggestionHTML(PageHandler.getInstance(request,response).s.wineset.bestknownwineid,PageHandler.getInstance(request,response).searchdata,PageHandler.getInstance(request,response).searchpage);
	ArrayList<String> countries = Webroutines.getCountries();
	if (PageHandler.getInstance(request,response).s.wineset.records>0){
 	%><div class='refineheader'><%=PageHandler.getInstance(request,response).t.get("refineresults")%></div>
	
	<div class='refine spriter-refine spriter'>
	<div class='refinel sprite-refinel sprite'></div>
	<div class='refinewines' onclick='javascript:show("refinelist");' onmouseout='javascript:hide("refinelist");'>
		<div id='refinelist' class='refinelist' onmouseover='show("refinelist");' onmouseout='hide("refinelist");'><%=refineHTML %></div>
		<%if(!"".equals(refineHTML)) out.write(PageHandler.getInstance(request,response).t.get("wines")+"<img src='"+Configuration.cdnprefix+"/css2/arrowgreen.jpg' alt='refine'/>");%></div>
	<%if(!"".equals(suggestionHTML)){%>
	<div class='suggestions' onclick='javascript:show("suggestionlist");' onmouseout='javascript:hide("suggestionlist");'>
		<div id='suggestionlist' class='suggestionlist' onmouseover='show("suggestionlist");' onmouseout='hide("suggestionlist");'><%=suggestionHTML %></div>
		<%=PageHandler.getInstance(request,response).t.get("suggestions")+"<img src='"+Configuration.cdnprefix+"/css2/arrowgreen.jpg' alt='suggestions'/>"%></div><%} %>
	<form id='refineform' action='<%=PageHandler.getInstance(request,response).searchpage%>' method='post'>	
		<div class='text'><%=PageHandler.getInstance(request,response).t.get("country")%></div>
			<select name="country" onchange='javascript:document.getElementById("refineform").submit();'>
				<option value="All"<%if (PageHandler.getInstance(request,response).searchdata.getCountry().equals("All")) out.print(" selected=\"selected\"");%>><%=PageHandler.getInstance(request,response).t.get("all")%></option>
				<option value="EU"<%if (PageHandler.getInstance(request,response).searchdata.getCountry().equals("EU")) out.print(" selected=\"selected\"");%>>Europe</option>
				<%for (int i=0;i<countries.size();i=i+2){
					%><option value="<%=countries.get(i)%>"<%if (PageHandler.getInstance(request,response).searchdata.getCountry().equals(countries.get(i))) out.print(" selected=\"selected\"");%>><%=countries.get(i+1)%></option><%
				}%>
		</select>
		<div class='text'><%=PageHandler.getInstance(request,response).t.get("vintage")%></div>
		<select name='vintage'  onchange='javascript:document.getElementById("refineform").submit();'>
				<option value="All"<%if (PageHandler.getInstance(request,response).searchdata.getVintage().equals("All")||PageHandler.getInstance(request,response).searchdata.getVintage().equals("")) out.print(" selected=\"selected\"");%>><%=PageHandler.getInstance(request,response).t.get("all")%></option>
				<%for (int vintage:PageHandler.getInstance(request,response).s.wineset.vintages){
					%><option value="<%=vintage%>"<%if (PageHandler.getInstance(request,response).searchdata.getVintage().equals(vintage+"")) out.print(" selected=\"selected\"");%>><%=vintage%></option><%
				}%>
		</select>
		<div class='text'><%=PageHandler.getInstance(request,response).t.get("size")%></div>
		<select name='size'  onchange='javascript:document.getElementById("refineform").submit();'>
				<option value="All"<%if (PageHandler.getInstance(request,response).searchdata.getSize()==null||PageHandler.getInstance(request,response).searchdata.getSize()==(float)0) out.print(" selected=\"selected\"");%>><%=PageHandler.getInstance(request,response).t.get("all")%></option>
				<%for (Float size:PageHandler.getInstance(request,response).s.wineset.sizes){
					%><option value="<%=size%>"<%if (PageHandler.getInstance(request,response).searchdata.getSize().equals(size)) out.print(" selected=\"selected\"");%>><%=Webroutines.formatSize(size)%></option><%
				}%>
		</select>
		<div class='currency'><%=PageHandler.getInstance(request,response).t.get("displaycurrency")%>
		<input type="radio" name="currency" value="EUR"  onchange='javascript:document.getElementById("refineform").submit();' <%if (PageHandler.getInstance(request,response).searchdata.getCurrency().equals("EUR")||PageHandler.getInstance(request,response).searchdata.getCurrency().equals("")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio" name="currency" value="GBP"  onchange='javascript:document.getElementById("refineform").submit();' <%if (PageHandler.getInstance(request,response).searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="USD"  onchange='javascript:document.getElementById("refineform").submit();' <%if (PageHandler.getInstance(request,response).searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$&nbsp;<input type="radio" name="currency" value="CHF"  onchange='javascript:document.getElementById("refineform").submit();' <%if (PageHandler.getInstance(request,response).searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>
		</div>
		<input type='hidden' name='keepdata' value='true'/>	
	</form>					
	<div class='refiner sprite-refiner sprite'></div>
	<img class='greengo' src='<%=Configuration.cdnprefix%>/css2/greengo.jpg' onclick='document.getElementById("refineform").submit()' alt='Go'/>
</div>
<%} %>
		