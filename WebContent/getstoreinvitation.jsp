<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><%@ page 
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Shopapplication"
%>
<jsp:useBean id="shopapplication" class="com.freewinesearcher.online.Shopapplication" scope="request"/>
<jsp:setProperty name="shopapplication" property="*" />

<% 	int id=shopapplication.getId();
//Dbutil.logger.info("id="+id);
	if (shopapplication.passwordOK()) {shopapplication=Shopapplication.retrieve(shopapplication.getId()); 
	if (shopapplication==null) shopapplication=Shopapplication.generate(id);
	int version=0;
	try{version=Integer.parseInt(request.getParameter("version"));}catch(Exception e){}
	if (version==0) {
		version=shopapplication.getVersion();
	}
%>
<jsp:setProperty name="shopapplication" property="*" />
<head>
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
<!--  <script charset="utf-8" id="injection_graph_func" src="Store%20draft%20form_bestanden/injection_graph_func.js"></script><link href="Store%20draft%20form_bestanden/injection_graph.css" type="text/css" rel="stylesheet" charset="utf-8" id="injection_graph_css">-->
</head>
<body><form method="get" action="https://www.vinopedia.com/addstore.jsp">
<font face="Georgia, Arial">
<%if (version==0){ %>
Dear Sir/Madam,
<br>
<br><a href='https://www.vinopedia.com'>Vinopedia.com</a> is a search engine for wine lovers. On this website wine
buyers can find out where they can buy the wine they are looking
for. Our primary focus in the past years has been on the
European market (with over a million unique visitors). On request of
many US wine lovers we now plan to go live in the USA. We would love to
include your store in our index. Getting your store and wines
listed is completely free. By getting listed many new customers will be able to find your store! 
<br>
<br>
<b>Please provide us with the following information and your store will be on Vinopedia within a week</b><br>
<i>For your convenience some of the information is pre-filled, please check if this information is correct. After filling out all information, you can use the send button at the bottom of this form to send the information to Vinopedia.</i><br/>
<b>Note: If your email client does not support HTML or none of the information is pre-filled, you can <a href="https://www.vinopedia.com/getstoreinvitation.jsp?id=<%=shopapplication.getId() %>&password=<%=shopapplication.getPassword() %>"  target="_blank">click here</a> to show this form in a web browser.</b>
<hr>
<b>Contact details:</b><br>
<ul><li>Store name: 									<br> <input size="70" maxlength="60" name="shopname" type="text" value="<%=shopapplication.getShopname() %>"> <br>
</li><li>Store address (street, zipcode, city, state):	<br> <input size="70" maxlength="60" name="address" type="text" value="<%=shopapplication.getAddress() %>"> <br>
</li><li>E-mail address for www.vinopedia.com: 			<br> <input size="70" maxlength="60" name="storeemailaddressforvp" type="text" value="<%=shopapplication.getStoreemailaddressforvp() %>"> <br>
</li><li>E-mail for customers: 							<br> <input size="70" maxlength="60" name="storeemailaddressforcustomers" type="text" value="<%=shopapplication.getStoreemailaddressforcustomers() %>"> <br>
</li><li>Name of contact person:						<br> <input size="70" maxlength="60" name="contactname" type="text" value=""> <br>
</li><li>Phone number : 									<br> <input size="70" maxlength="60" name="storephonenumber" type="text" value="<%=shopapplication.getStorephonenumber() %>"> <br>
</li><li>General description of the store : 			<br> <textarea rows="5" cols="80" wrap="physical" name="storegeneraldescription"><%=shopapplication.getStoregeneraldescription() %></textarea>
</li></ul>
<hr/><br/>
<b>Data required for the search engine:</b><br>
<ul><li>URL of homepage:								<br> <input size="70" maxlength="60" name="urlhomepage" type="text" value="<%=shopapplication.getUrlhomepage() %>"> <br>
</li><li>Does your store have a <a href="https://www.vinopedia.com/datafeed.jsp" target="_blank">data feed </a>, if so what is the URL? (<I> a data feed can be an excel file, csv file, Google Base, etc</I>)   	
												<br> <input size="70" maxlength="60" name="urldatafeed" type="text" value="<%=shopapplication.getUrldatafeed() %>"><br>
</li><li>If not, is there perhaps a single page where we can find the complete price list or a browse through page by which we can browse through your price list, could you please provide us the URL?
												<br><input size="70" maxlength="60" name="urlbrowsethrough" type="text" value="<%=shopapplication.getUrlbrowsethrough() %>"><br>

</li><li>Please acknowledge you will accept our terms and conditions:  <a href="https://www.vinopedia.com/siteindexationagreement.jsp" target="_blank">Terms and conditions </a> 

					<input name="acknowledgeTandCY" type="checkbox" <%=(shopapplication.isAcknowledgeTandCY()?"checked='checked'":"") %>>Accept
					
<br>
</li><li>We will be placing as many links towards your site as you have wines on your site. In return we ask at least one link back. Please provide us the URL on which you will put the link back towards Vinopedia (<i>see questions below why this is requested</i>)
<br>					<input size="70" maxlength="60" name="urlforvplink" type="text" value="<%=shopapplication.getUrlforvplink() %>"><br>
</li></ul><!=====================================================================================================================================================================================================>
<hr/><br/><b>Questions about your store:</b><br>
<ul><li>
Does your website support secure online ordering? Yes <input type="checkbox" name="onlineordering" <%=(shopapplication.isOnlineordering()?"checked='checked'":"") %>/> (if you do not flag a Yes box we consider your answer to the question is no) <br>
</li><li>To what states does your store ship in the USA? <br/>
					All <input type="checkbox" name="shiptostates" value="All" <%=(shopapplication.shipstostate("All")?"checked=\"checked\"":"") %>/> <B><br/>
					Or : <br /></B>
					<table>
					<tr>	
							<td><input type="checkbox" name="shiptostates" value="AL" <%=(shopapplication.shipstostate("AL")?"checked=\"checked\"":"") %>/>Alabama </td>
							<td><input type="checkbox" name="shiptostates" value="AK" <%=(shopapplication.shipstostate("AK")?"checked=\"checked\"":"") %>/>Alaska </td>
							<td><input type="checkbox" name="shiptostates" value="AZ" <%=(shopapplication.shipstostate("AZ")?"checked=\"checked\"":"") %>/>Arizona</td>
							<td><input type="checkbox" name="shiptostates" value="AR" <%=(shopapplication.shipstostate("AR")?"checked=\"checked\"":"") %>/>Arkansas </td>
							<td><input type="checkbox" name="shiptostates" value="CA" <%=(shopapplication.shipstostate("CA")?"checked=\"checked\"":"") %>/>California </td>	
					</tr>
					<tr>		
							<td><input type="checkbox" name="shiptostates" value="CO" <%=(shopapplication.shipstostate("CO")?"checked=\"checked\"":"") %>/>Colorado </td>
							<td><input type="checkbox" name="shiptostates" value="CT" <%=(shopapplication.shipstostate("CT")?"checked=\"checked\"":"") %>/>Connecticut </td>
							<td><input type="checkbox" name="shiptostates" value="DE" <%=(shopapplication.shipstostate("DE")?"checked=\"checked\"":"") %>/>Delaware </td>
							<td><input type="checkbox" name="shiptostates" value="FL" <%=(shopapplication.shipstostate("FL")?"checked=\"checked\"":"") %>/>Florida </td>
							<td><input type="checkbox" name="shiptostates" value="GA" <%=(shopapplication.shipstostate("GA")?"checked=\"checked\"":"") %>/>Georgia </td>
					</tr>
					<tr>
							<td><input type="checkbox" name="shiptostates" value="HI" <%=(shopapplication.shipstostate("HI")?"checked=\"checked\"":"") %>/>Hawaii </td>
							<td><input type="checkbox" name="shiptostates" value="ID" <%=(shopapplication.shipstostate("ID")?"checked=\"checked\"":"") %>/>Idaho </td>
							<td><input type="checkbox" name="shiptostates" value="IL" <%=(shopapplication.shipstostate("IL")?"checked=\"checked\"":"") %>/>Illinois </td>
							<td><input type="checkbox" name="shiptostates" value="IN" <%=(shopapplication.shipstostate("IN")?"checked=\"checked\"":"") %>/>Indiana </td>
							<td><input type="checkbox" name="shiptostates" value="IA" <%=(shopapplication.shipstostate("IA")?"checked=\"checked\"":"") %>/>Iowa </td>
					</tr>
					<tr>		
							<td><input type="checkbox" name="shiptostates" value="KS" <%=(shopapplication.shipstostate("KS")?"checked=\"checked\"":"") %>/>Kansas </td>
							<td><input type="checkbox" name="shiptostates" value="KY" <%=(shopapplication.shipstostate("KY")?"checked=\"checked\"":"") %>/>Kentucky </td>
							<td><input type="checkbox" name="shiptostates" value="LA" <%=(shopapplication.shipstostate("LA")?"checked=\"checked\"":"") %>/>Louisiana </td>
							<td><input type="checkbox" name="shiptostates" value="ME" <%=(shopapplication.shipstostate("ME")?"checked=\"checked\"":"") %>/>Maine </td>
							<td><input type="checkbox" name="shiptostates" value="MD" <%=(shopapplication.shipstostate("MD")?"checked=\"checked\"":"") %>/>Maryland </td>
					</tr>
					<tr>		
							<td><input type="checkbox" name="shiptostates" value="MA" <%=(shopapplication.shipstostate("MA")?"checked=\"checked\"":"") %>/>Massachusetts </td>
							<td><input type="checkbox" name="shiptostates" value="MI" <%=(shopapplication.shipstostate("MI")?"checked=\"checked\"":"") %>/>Michigan </td>
							<td><input type="checkbox" name="shiptostates" value="MN" <%=(shopapplication.shipstostate("MN")?"checked=\"checked\"":"") %>/>Minnesota </td>
							<td><input type="checkbox" name="shiptostates" value="MS" <%=(shopapplication.shipstostate("MS")?"checked=\"checked\"":"") %>/>Mississippi </td>
							<td><input type="checkbox" name="shiptostates" value="MO" <%=(shopapplication.shipstostate("MO")?"checked=\"checked\"":"") %>/>Missouri </td>
					</tr>
					<tr>
							<td><input type="checkbox" name="shiptostates" value="MT" <%=(shopapplication.shipstostate("MT")?"checked=\"checked\"":"") %>/>Montana </td>
							<td><input type="checkbox" name="shiptostates" value="NE" <%=(shopapplication.shipstostate("NE")?"checked=\"checked\"":"") %>/>Nebraska </td>
							<td><input type="checkbox" name="shiptostates" value="NV" <%=(shopapplication.shipstostate("NV")?"checked=\"checked\"":"") %>/>Nevada </td>	
							<td><input type="checkbox" name="shiptostates" value="NH" <%=(shopapplication.shipstostate("NH")?"checked=\"checked\"":"") %>/>New Hampshire </td>
							<td><input type="checkbox" name="shiptostates" value="NJ" <%=(shopapplication.shipstostate("NJ")?"checked=\"checked\"":"") %>/>New Jersey </td>
					</tr>	
					<tr>
							<td><input type="checkbox" name="shiptostates" value="NM" <%=(shopapplication.shipstostate("NM")?"checked=\"checked\"":"") %>/>New Mexico </td>
							<td><input type="checkbox" name="shiptostates" value="NY" <%=(shopapplication.shipstostate("NY")?"checked=\"checked\"":"") %>/>New York </td>
							<td><input type="checkbox" name="shiptostates" value="NC" <%=(shopapplication.shipstostate("NC")?"checked=\"checked\"":"") %>/>North Carolina </td>
							<td><input type="checkbox" name="shiptostates" value="ND" <%=(shopapplication.shipstostate("ND")?"checked=\"checked\"":"") %>/>North Dakota </td>
							<td><input type="checkbox" name="shiptostates" value="OH" <%=(shopapplication.shipstostate("OH")?"checked=\"checked\"":"") %>/>Ohio </td>
					</tr>	
					<tr>							
							<td><input type="checkbox" name="shiptostates" value="OK" <%=(shopapplication.shipstostate("OK")?"checked=\"checked\"":"") %>/>Oklahoma</td>
							<td><input type="checkbox" name="shiptostates" value="OR" <%=(shopapplication.shipstostate("OR")?"checked=\"checked\"":"") %>/>Oregon</td>
							<td><input type="checkbox" name="shiptostates" value="PA" <%=(shopapplication.shipstostate("PA")?"checked=\"checked\"":"") %>/>Pennsylvania </td>
							<td><input type="checkbox" name="shiptostates" value="RI" <%=(shopapplication.shipstostate("RI")?"checked=\"checked\"":"") %>/>Rhode Island </td>
							<td><input type="checkbox" name="shiptostates" value="SC" <%=(shopapplication.shipstostate("SC")?"checked=\"checked\"":"") %>/>South Carolina </td>
					</tr>
					<tr>								
							<td><input type="checkbox" name="shiptostates" value="SD" <%=(shopapplication.shipstostate("SD")?"checked=\"checked\"":"") %>/>South Dakota </td>
							<td><input type="checkbox" name="shiptostates" value="TN" <%=(shopapplication.shipstostate("TN")?"checked=\"checked\"":"") %>/>Tennessee </td>
							<td><input type="checkbox" name="shiptostates" value="TX" <%=(shopapplication.shipstostate("TX")?"checked=\"checked\"":"") %>/>Texas </td>
							<td><input type="checkbox" name="shiptostates" value="UT" <%=(shopapplication.shipstostate("UT")?"checked=\"checked\"":"") %>/>Utah </td>
							<td><input type="checkbox" name="shiptostates" value="VT" <%=(shopapplication.shipstostate("VT")?"checked=\"checked\"":"") %>/>Vermont </td>
					</tr>
					<tr>						
							<td><input type="checkbox" name="shiptostates" value="VA" <%=(shopapplication.shipstostate("VA")?"checked=\"checked\"":"") %>/>Virginia </td>
							<td><input type="checkbox" name="shiptostates" value="WA" <%=(shopapplication.shipstostate("WA")?"checked=\"checked\"":"") %>/>Washington </td>
							<td><input type="checkbox" name="shiptostates" value="WV" <%=(shopapplication.shipstostate("WV")?"checked=\"checked\"":"") %>/>West Virginia </td>
							<td><input type="checkbox" name="shiptostates" value="WI" <%=(shopapplication.shipstostate("WI")?"checked=\"checked\"":"") %>/>Wisconsin </td>
							<td><input type="checkbox" name="shiptostates" value="WY" <%=(shopapplication.shipstostate("WY")?"checked=\"checked\"":"") %>/>Wyoming </td>
					</tr>
					</table>
</li><li>To what other countries does your store ship? <br/>Global delivery <input type="checkbox" name="global" <%=(shopapplication.isGlobal()?"checked='checked'":"") %>/> <B><br/>Or : </B>
<br> <textarea rows="5" cols="80" wrap="physical" name="countriesstatesshippingto"><%=shopapplication.getCountriesstatesshippingto() %></textarea><br> 
</li><li>Do you also have a physical store customers can visit? Yes <input type="checkbox" name="physical" <%=(shopapplication.isPhysical()?"checked='checked'":"") %>/> <br>
</li></ul>
<hr/><br/><b>Commercial questions:</b><br>
<ul><li>Would you be interested in advertising on vinopedia.com? 
					<input name="interestinadvertisingY"  type="checkbox" <%=(shopapplication.isInterestinadvertisingY()?"checked='checked'":"") %>>Yes<br>
</li><li>Would you be interested in methods to further increase your revenue via an affiliate program?
					<input name="affiliateprogramY" type="checkbox" <%=(shopapplication.isAffiliateprogramY()?"checked='checked'":"") %>>Yes <br>
</li><li>Does your store have an affiliate program? If so please describe the details of this program. 
<br> 				<textarea rows="5" cols="80" wrap="physical" name="affiliateprogram"><%=shopapplication.getAffiliateprogram() %></textarea>
<br>
</li><li>Do you have any questions, remarks or requests ? 			<br> <textarea rows="5" cols="80" wrap="physical" name="questions"><%=shopapplication.getQuestions() %></textarea>
</li></ul>
<input type="hidden" name="id" value="<%=shopapplication.getId() %>">
<input type="hidden" name="status" value="replied">
<input type="hidden" name="action" value="save">
<input type="hidden" name="password" value="<%=shopapplication.getPassword() %>">
<br/>
					<input value="Send" type="submit"> 
<hr>Thank you for all that information. I hope we made is this process as efficient as possible for you. 
Your store will be on Vinopedia soon. We are looking forward to provide you lot of additional new customers
in the near future.Please feel free to mail/call or Skype me at any time.
<br>
<br>
Regards, Jeroen Starrenburg
<br>
<br>
Email: Jeroen@vinopedia.com<br>
Phone: +31653725961<br>
Skype: VinopediaCom@skype.com
<br>
<br>
<b>PS, Questions you might have:</b><br>
----------------------------------------------------------------------------------------------------------------------<br>
<b>Why should I get my store listed on www.vinopedia.com?</b><br>
<i>Because it is free way to attract new customers.</i><br>
<b>Why do wine lovers visit www.vinopedia.com?</b><br>
<i>Because Vinopedia offers them a transparent view of the wine market. We do not filter results by only showing sponsoring shops. Therefore customers see Vinopedia as a trusted and objective advisor.
</i><br>
<b>What is in it for Vinopedia?</b><br>
<i>Vinopedia is ran by just two wine lovers. Our goal is to become the best search engine for wines in the world. Listing your store makes our search engine more complete!</i><br>
<b>Why are data feeds preferred by search engines?</b><br>
<i>Data feeds are the easiest way for a search engine to retrieve the required data from your website. Advantages are: <br />
* Even if you change the look and feel of your websites, search engines will still be able to retrieve the required information.<br />
* It takes us less time to get your store listed.
</i><br>
<b>Is it a problem if I don't have a data feed?</b><br>
<i>No, if it is not possible to deliver a data feed, we are willing to spend additional effort to get your store listed anyway.</i><br>
<b>How often will you update my store information?</b><br>
<i>Our software will automatically visit your website once a day to retrieve the latest information.</i><br>
<b>Does getting listed impact the performance of my website in any way?</b><br>
<i>No not at all, it can be seen as one normal visitor each day.</i><br>
----------------------------------------------------------------------------------------------------------------------<br>
</font>
</form><%} else if (version==1) {%>
Dear Sir/Madam, <br/>
<br/>
We would like to invite you to get listed on the wine search engine <a href='https://www.vinopedia.com'>Vinopedia.com</a> (no costs involved). Via this search engine customers all over the world can find your store and products. <br/><br/>
On the European market we have had over a million unique visitors. On request of many US wine lovers we are live in the USA now. <br/><br/>
Please take 5 minutes of your time to provide us with your contact details. (note: If your email client does not support HTML or none of the information is pre-filled, you can <a href="https://www.vinopedia.com/getstoreinvitation.jsp?id=<%=shopapplication.getId() %>&version=1&password=<%=shopapplication.getPassword() %>"  target="_blank">click here</a> to show this form in a web browser.)<br/>
<hr>
<b>Contact details:</b><br>
<ul><li>Store name: 									<br> <input size="70" maxlength="60" name="shopname" type="text" value="<%=shopapplication.getShopname() %>"> <br>
</li><li>Attention:						<br> <input size="70" maxlength="60" name="contactname" type="text" value=""> <br>
</li><li>Store address:	<br> <input size="70" maxlength="60" name="address" type="text" value="<%=shopapplication.getAddress() %>"> <br>
</li><li>E-mail address: 							<br> <input size="70" maxlength="60" name="storeemailaddressforcustomers" type="text" value="<%=shopapplication.getStoreemailaddressforcustomers() %>"> <br>
</li><li>Phone number : 									<br> <input size="70" maxlength="60" name="storephonenumber" type="text" value="<%=shopapplication.getStorephonenumber() %>"> <br>
</li></ul>
<hr/><br/>
<b>Data required for the search engine:</b><br>
<ul><li>URL of homepage:								<br> <input size="70" maxlength="60" name="urlhomepage" type="text" value="<%=shopapplication.getUrlhomepage() %>"> <br>
</li><li>Does your store have a <a href="https://www.vinopedia.com/datafeed.jsp" target="_blank">data feed </a>, if so, please provide us the URL? (<I>a data feed can be an Excel file, csv file, Google Base, etc</I>)   	
												<br> <input size="70" maxlength="60" name="urldatafeed" type="text" value="<%=shopapplication.getUrldatafeed() %>"><br>
</li><li>Or, is there a single (or browse through) page where we can find the complete price list? Please provide us the URL:
												<br><input size="70" maxlength="60" name="urlbrowsethrough" type="text" value="<%=shopapplication.getUrlbrowsethrough() %>"><br>

</li><li>Please acknowledge you will accept our <a href="https://www.vinopedia.com/siteindexationagreement.jsp" target="_blank">Terms and conditions</a>: 

					<input name="acknowledgeTandCY" type="checkbox" <%=(shopapplication.isAcknowledgeTandCY()?"checked='checked'":"") %>>Accept
					
<br>
</li></ul>
<input type="hidden" name="id" value="<%=shopapplication.getId() %>">
<input type="hidden" name="status" value="replied">
<input type="hidden" name="action" value="save">
<input type="hidden" name="password" value="<%=shopapplication.getPassword() %>">
<input type="hidden" name="version" value="1">

<br/>
					<input value="Send" type="submit"> 
<hr>After filling in this form, your store will be on Vinopedia.com within a week. If you have any questions, please feel free to mail or call me.
<br>
<br>
Regards, Jeroen Starrenburg<br/>
<br>
Commercial and marketing director Vinopedia.com<br/>
Phone: +31 653725961<br/>
Email: <a href='mailto:Jeroen@vinopedia.com'>Jeroen@vinopedia.com</a><br/>
Skype: vinopediacom<br/>
<br>
PS: Vinopedia.com does not charge anything for getting your store listed!<br/>
</font>
</form><%} %>

	<%} else {
	%><h1>Incorrect password</h1><%	}%></body></html>