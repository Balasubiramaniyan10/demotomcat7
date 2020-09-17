<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>Site indexation agreement</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Publishers");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpagenosearch.jsp" %>
<h1>SITE INDEXATION AGREEMENT</h1>
<br/>
This Agreement permits you or the company or other legal entity you represent (hereinafter: Merchant), to have at least part of Merchant's offerings included in the database of the search engine Vinopedia.com, operated by Search as a Service (hereinafter: Vinopedia). Registration at the Vinopedia Web site indicates acceptance of the terms and conditions of this Agreement.<br/>
<br/>

<h2>Article 1. Access to Merchant's wine information</h2>
1.1 Merchant authorizes Vinopedia to access, retrieve, copy, transmit and store information regarding wines, origins, prices, availability and other data as supplied by Merchant (hereafter: the Wine Information).<br/>
1.2 In the Wine Information Merchant will at least include an identification of name, vintage, availability and price of the wines.<br/>
1.3 Merchant will make the Wine Information available to Vinopedia in the manner and format as prescribed and documented by Vinopedia. Vinopedia will make documentation available to Merchant regarding such manner and format.<br/>
<h2>Article 2. Availability of the wine information</h2>
2.1 Vinopedia will strive to include the Wine Information in the database for the Vinopedia Internet search engine. Vinopedia has the sole right to decide which part(s) of the Wine Information are included in the database for the Vinopedia Internet search engine or shown on any search engine result page.<br/>
2.2 Vinopedia can make no guarantee that all Wine Information supplied by Merchant is included in this database or on any search engine result page, even when Vinopedia has indicated to Merchant that such information will be so included and even when Vinopedia uses a custom spider (see article 3) to obtain the Wine Information.<br/>
2.3 Merchant will ensure that the Wine Information it supplies is accurate and up-to-date, in particular with regards to availability and price of the wines included in the Wine Information. Vinopedia will retrieve the Wine Information at reasonable intervals.<br/>
2.4 Vinopedia has the right, at its sole discretion, to remove any or all of the Wine Information supplied by Merchant if such Wine Information is or may reasonably be considered to be false, inaccurate or misleading or contains any other data which may be harmful to the business, website or other activities of Vinopedia.<br/>

<h2>Article 3. Custom spider</h2>
3.1 If Merchant is unable to make the Wine Information available as stated in article 1, Merchant shall indicate so to Vinopedia. Vinopedia will provide a cost estimate to Merchant for the development of a custom spider to obtain the Wine Information from Merchant and to transform the thus-obtained Wine Information into the required format at a price to be determined in the estimate.<br/>
3.2 Upon written acceptance by Vinopedia, Vinopedia will develop and test such custom spider. Merchant will provide all necessary support and assistance to enable Vinopedia to do so.<br/>
3.3 Vinopedia shall not be liable for any damages, direct or indirect, that result from development and testing such custom spider, even when Vinopedia knew or should have known that such development and testing could have led to such damages.<br/>
3.4 If during development and testing Vinopedia determines that the amount of time and/or effort will substantially exceed the original estimate, Vinopedia has the right to increase the price to match the extra time and/or effort. If the increased price is unacceptable to Merchant, Vinopedia may terminate the development.<br/>
3.5 Merchant will inform Vinopedia about any changes to the structure of its website or the Wine Information that may affect the performance of the custom spider. Vinopedia will strive to update the custom spider for changes that in the opinion of Vinopedia constitute minor changes. Vinopedia cannot guarantee that the custom spider will continue to function if Merchant makes any changes to its website or the Wine Information to be collected by the custom spider.<br/>
3.6 If Merchant desires that the custom spider will continue to function after non-minor changes, Vinopedia will provide a cost estimate for the necessary adaptations. Articles 3.2-3.4 shall apply "mutatis mutandis" to such cost estimate.<br/>
<h2>Article 4. Maintenance</h2>
4.1 Vinopedia may temporarily suspend its retrieval and/or inclusion of Wine Information for the purposes of maintenance or upgrades to the Website and related systems. Vinopedia cannot be held liable for any damages that may result therefrom.<br/>

4.2 In case of force majeure, which includes but is not limited to communications, power failure, riot, insurrection, labor disputes, accident, action of government, restrictions on import and/or export or any inability to  perform which is caused by Vinopedia's suppliers, Vinopedia is entitled to suspend its obligations under these terms of use or to terminate this agreement in its entirety, without any obligation to compensate Merchant for any damages Merchant may suffer as a result.<br/>
<h2>Article 5. Liability</h2>
5.1 Vinopedia is not liable for any indirect damages, including consequential damages, loss of income or  profits, loss of data or special damages suffered by Merchant or others.<br/>
5.2 Any liability for Vinopedia concerning direct damages suffered by Merchant, regardless of cause, shall be limited to 10 euro (excluding VAT) per event or series of related events.<br/>
5.3 Merchant indemnifies Vinopedia for any claims by any third party regarding damages, costs or interest in connection with this agreement.<br/>
5.4 The previous clauses do not apply in case of intent or recklessness on the part of Vinopedia.<br/>
<h2>Article 6. Duration and termination</h2>
6.1 These terms of use are entered for an indefinite period of time.<br/>
6.2 Both Merchant and Vinopedia may terminate this agreement at any time by providing written notice to that effect.<br/>

6.3 Within 3 business days from the date of termination, Vinopedia will cease its retrieval of the Wine Information and remove the Wine Information from its search engine database, unless agreed otherwise between Vinopedia and Merchant.<br/>
<h2>Article 7. Adaptation of these terms</h2>
7.1 Vinopedia has the right to adapt these terms or to provide additional terms at any time. Such adapted or additional terms shall enter into force 30 days after communication thereof to Merchant. Minor adaptations shall enter into force immediately after communication.<br/>
7.2 If Merchant does not want to accept an adapted or additional term, Merchant must terminate this agreement within these 30 days. Failure to communicate its refusal to accept such terms within this period constitutes acceptance of such terms.<br/>
7.3 Any terms, conditions or exceptions provided by Merchant are binding upon Vinopedia only if explicitly agreed upon in writing.<br/>
7.4 Both parties are entitled to transfer this agreement and any of their respective rights and obligations to any third party in case of a sale or other transfer of the respective business to said third party. However, Merchant is only entitled to do so after obtaining written approval from Vinopedia, which approval shall not be unreasonably withheld.<br/>
<h2>Article 8. Miscellaneous</h2>
8.1 Dutch law applies to this agreement.<br/>
8.2 Unless prescribed otherwise by mandatory provisions of law, all conflicts arising out of this agreement shall be brought before the competent Dutch court.<br/>

8.3 In case any part of this agreement is held to be invalid, void or unenforceable for any reason, such invalidity shall not affect the rest of this agreement. The parties shall in such a case determine one or more replacement provisions that most closely approximates the clause concerned and which is legal under applicable law.<br/>
8.4 Any requirement for a 'written' statement can be fulfilled by using e-mail, provided the identity and integrity of such e-mail can be determined with sufficient certainty.<br/>
8.5 If Merchant sends any message to Vinopedia, the version of such message stored by Vinopedia shall be regarded as authentic unless Merchant proves otherwise.<br/>
8.6 Merchant provides a link back to Vinopedia.com on the Merchant's public web site.<br/>
<br/>
Last updated on November 24th 2008.
</div>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
