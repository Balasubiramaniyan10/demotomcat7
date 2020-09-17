<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title>Terms of use</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Terms of use");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpagenosearch.jsp" %>
<h1>Terms of Use for vinopedia.com</h1><br/>
vinopedia.com ("the Website") is offered to you free of charge. The Provider of the Website is Search as a Service (address see below). Any usage of the Web site vinopedia.com is subject to the terms below. Your use of the Website constitutes acceptance of these terms. If you have any questions, please contact Provider <a href='/contact.jsp' >here</a>.<br/>
 <br/>
These Terms of Use were last updated on October 9th, 2008.
<br/><br/><h2>Article 1. Website content</h2><br/>
1.1 On the Website, Provider publishes texts, images and other materials that originate with Provider and third parties. You may read, download and print these materials for your own personal or business use.<br/>
1.2 Copying, distribution and any other use of these materials is prohibited without prior written permission of Provider, except and to the extent permitted by mandatory provisions of law.<br/>
1.3 The materials are for informational purposes only and are offered AS-IS without warranty of any kind, including but not limited to warranties of accuracy, merchantability or fitness for any particular purpose.<br/>
1.4 Some of the materials relate or refer to offers for commercial transactions. Provider is not a party in any such transactions. You enter into any such transactions at your own risk and responsibility. <br/>
1.5 Provider may change the offered materials at any time without prior notice.<br/>
1.6 Using automated processes, spiders or other software tools to request all or a substantial part of the Website is prohibited.
<br/><br/><h2>Article 2. Registration on the Website</h2><br/>
2.1 To publish a text, image or other material on the Website you must first register yourself. Provider is entitled to refuse your registration without motivation.<br/>
2.2 You must provide a working e-mail address and your real name during registration.<br/>
2.3 If your registration is accepted, Provider will enable you to log in using a username and password. It is your responsibility to keep the password private from others. Provider is not responsible for any misuse of the password and may assume that anything that occurs using your username is under your supervision and risk.<br/>
2.4 Provider will strive to provide you with reasonable support in your usage of the website but cannot make any guarantees about accuracy or completeness of any information provided.
<br/><br/><h2>Article 3. Publications on the Website</h2><br/>
3.1 On the Website you can publish materials such as reviews, ratings, addresses and feedback regarding wines or wine sellers. You are free to decide which materials you want to publish and when (subject to the rules below). However you are not entitled to any consideration for any publication on the Website.<br/>
3.2 You represent that your publications are accurate to the best of your knowledge. You will not intentionally add false information, or change or remove existing information without good reason.<br/>
3.3 Publications may not be defamatory, obscene, offensive, racist or otherwise unlawful.<br/>
3.4 Publications may not violate the rights of third parties and in particular may not link to images on third-party servers without the permission of the copyright holders.<br/>
3.5 Provider may block, adapt or remove any publication by you if Provider, at its sole discretion, determines that such publication is likely to be unlawful or in violation of these terms of use. Provider does not need to consult or inform you in such a case. Provider is not liable for any damages you may suffer as a result of its actions under this Article.
3.6 You agree to fully indemnify Provider for any third-party claims alleging that your publications on the Website violate their rights or otherwise are unlawful.
<br/><br/><h2>Article 4. License to Provider</h2><br/>
4.1 By publishing materials on the Website, you declare that you fully own all rights to these materials and that you are permitted to grant the license requested below without any obligations or restrictions on the part of Provider.<br/>
4.2 You retain the full copyright on materials you publish on the Website. You hereby grant Publisher a non-exclusive license to publish and reuse these materials on the Website in any way Provider sees fit, including inclusion in the search engine algorythms. This license includes the right to adapt these materials. While Provider will strive to publish your name in connection with these materials if reasonably practical, you waive any claim to demand acknowledgment on the Website as author.<br/>
4.3 You authorize Publisher to take legal action against third parties in case of publication of your materials in the form in which they have been published on the Website.
<br/><br/><h2>Article 5. Processing of personal data</h2><br/>
5.1 Provider respects the privacy of all users of the Website and has taken steps to ensure that personal data is processed in compliance with the Dutch Data Protection Act ("Wet Bescherming Persoonsgegevens"). Provider will not provide personal data to third parties unless indicated otherwise in these terms of use. You hereby grant Provider permission to use your personal data for any purpose within the framework of these terms of use.<br/>
5.2 You can withdraw your permission at any time. Your username and account will be canceled and your personal data will be removed from Provider's files, unless continued use is necessary for Provider's business purposes or is required by law. Provider does not have to remove any publications on the Website, unless you can demonstrate a serious reason to the contrary.<br/>
5.3 You have a right to inspect and correct your personal data. Please contact Provider using the address below.<br/>
5.4 Provider records general data about visitors, among other reasons to prevent abuse of the Website. This data includes the IP-address of the computers that access the Website, the time of such access and any data provided by a web browser.<br/>
5.5 Provider uses cookies on the Website. Cookies are small text files that your Web browser leaves on your hard drive to recognize you as a repeat user of our Web site, to track your use of our Web site and to target advertising. You can refuse to accept cookies but this may negatively affect the performance of the Website for you.<br/>
5.6 During registration Provider asks you to provide certain personal data, such as your name and a working e-mail address. This data will not be provided to third parties unless you have explicitly granted permission to do so, or such provision is necessary to provide services to you or is required by law.<br/>
5.7 The Website contains advertisements provided by the US company Google. Google uses technology such as cookies and web beacons to keep track of how visitors use the Website. Any personal data obtained in this way is transported to servers in the United States for further processing by Google. Google uses this information to determine how you use the Website, which in turn is used to provide reports to Provider about Website usage and to adapt the effectiveness of its advertisements. Provider has no control over Google's use of your personal data.<br/>
5.8 To prevent and detect click fraud and other forms of abuse, Provider may supply information such as IP-addresses and search queries to third parties. Provider will take reasonable steps to ensure individual users cannot be identified from this information, but cannot guarantee that this will be impossible under all circumstances.
<br/><br/><h2>Article 6. Availability and changes</h2><br/>
6.1 Provider may temporarily disable access to the Website or parts thereof for the purposes of maintenance or upgrades to the Website and related systems, or due to technical problems. Provider cannot be held liable for any damages that may result from unavailability of the service.<br/>
6.2 Provider may adapt the Website and associated hard- and software to correct errors and to improve the functionality. Provider cannot be held liable for any damages that may result from any adaptation.<br/>
6.3 In case of force majeure, which includes but is not limited to communications, power failure, riot, insurrection, labor disputes, accident, action of government, restrictions on import and/or export or any inability to  perform which is caused by Provider's suppliers, Provider is entitled to suspend its obligations under these terms of use or to terminate this agreement in its entirety, without any obligation to compensate you for any damages you may suffer as a result.
<br/><br/><h2>Article 7. Liability</h2><br/>
7.1 Provider is not liable for any indirect damages, including consequential damages, loss of income or  profits, loss of data or special damages suffered by you or others.<br/>
7.2 Any liability for Provider concerning direct damages suffered by you, regardless of cause, shall be limited to 10 euro (excluding VAT) per event or series of related events.<br/>
7.3 You indemnify Provider for any claims by any third party regarding damages, costs or interest in connection with this agreement.<br/>
7.4 The previous clauses do not apply in case of intent or recklessness on the part of Provider.
<br/><br/><h2>Article 8. Duration and termination</h2><br/>
8.1 These terms of use are entered for an indefinite period of time. Both you and Provider may terminate this agreement at any time, without having to provide prior notice. After termination by either party, you may no longer use the Website.<br/>
8.2 Articles 4 and 7 shall survive termination of this agreement for as long as Provider reasonably can claim rights under these Articles.<br/>
8.3 Provider is entitled to suspend its obligations towards you in case of a reasonable suspicion that you act in any way that is in violation of these terms, without any right of compensation for you.
<br/><br/><h2>Article 9. Adaptation of these terms</h2><br/>
9.1 Provider has the right to adapt these terms or to provide additional terms at any time. Such adapted or additional terms shall enter into force 30 days after publication thereof on the Website. Minor adaptations shall enter into force upon publication.<br/>
9.2 If you do not want to accept an adapted or additional term, you must terminate this agreement within these 30 days. By using the Website after these 30 days, you indicate your acceptance of these new terms.<br/>
9.3 Any terms, conditions or exceptions provided by you are binding upon Provider only if explicitly agreed upon in writing.<br/>
9.4 Provider is entitled to transfer this agreement and any of its rights and obligations to any third party in case of a sale or other transfer of the Website.
<br/><br/><h2>Article 10. Miscellaneous</h2><br/>
10.1 Dutch law applies to this agreement.<br/>
10.2 Unless prescribed otherwise by mandatory provisions of law, all conflicts arising out of this agreement shall be brought before the competent Dutch court.<br/>
10.3 In case any part of this agreement is held to be invalid, void or unenforceable for any reason, such invalidity shall not affect the rest of this agreement. The parties shall in such a case determine (a) replacement provision(s) that most closely approximates the clause concerned and which is legal under applicable law.<br/>
10.4 Any requirement for a "written" statement can be fulfilled by using e-mail, provided the identity and integrity of such e-mail can be determined with sufficient certainty.<br/>
10.5 If you send any message to Provider, the version of such message stored by Provider shall be regarded as authentic unless you can prove otherwise.<br/>
<br/>
Search as a Service<br/>
Pieter Nieuwlandstraat 57<br/>
3514 HD Utrecht<br/>
The Netherlands <br/>
Registered at the Chamber of Commerce in Utrecht, registration number 30226759<br/>
<a href='/contact.jsp' >Send a message</a><br/>
 
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
</body>
</html>