<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.freewinesearcher.online.Webroutines"%>
<%@page import="com.vinopedia.paypal.Button"%>
<jsp:useBean id="button" beanName="com.vinopedia.paypal.Button" type="com.vinopedia.paypal.Button" scope="request" />
<jsp:setProperty name="button" property="*"/>
<!DOCTYPE HTML><html><body>

<form action="" method="post">
Store<select name="store" >
<% ArrayList<String> stores=Webroutines.getShopList(""); 

for (int i=0;i<stores.size();i=i+2){
%>
<option value="<%=stores.get(i)%>"<%if ((stores.get(i).equals(String.valueOf(button.getStore())))) out.print(" selected='selected'");%>><%=stores.get(i+1)%>
<%
}


%></select><br/>
Amount<input type="text" name="amount" value="<%=button.getAmountstr() %>"/><br/>
Currency<select name="currency" >
<% ArrayList<String> currencies=Webroutines.getCurrency(); 
for (String cur:currencies){
	out.write("<option value='"+cur+"' >"+cur+"</option>");
}

%></select><br/>
Invoice number<input type="text" name="invoice" value='<%=button.getInvoice() %>'/>
<input type="hidden" name="generate" value="true"/>
<br/>
<input type="submit" value="Create payment link"/>
</form>

<%
if (button.isValid()){
button.createEncryptedButton();

if ("".equals(button.message)){
out.write("<br/><br/>Button:<br/>"+button.getWebsitecode()); 
out.write(Spider.escape(button.getWebsitecode())+"<br/><br/>");
out.write("Email link:<br/><a href='"+button.getEmaillink()+"' alt='Secure payment'>Click here</a><br/>"); 
out.write("Link to payment for email:<br/>"+Spider.escape(button.getEmaillink())+"<br/><br/>");
} else {
	out.write("Problem:<br/>"+button.message); 
}

}

%>
</body></html>