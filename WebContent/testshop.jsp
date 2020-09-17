<%


//javax.servlet.http.Cookie mycookie = new javax.servlet.http.Cookie("ASP.NET_SessionId","m5jwrg45or3vrv2deldm3555");
//response.addCookie(mycookie);
//mycookie = new javax.servlet.http.Cookie("fwstest","RP2");
//response.addCookie(mycookie);
 %>
<html><head>
<title>
Demonstration shop
</title>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
</head><body>
<a href='https://www.vinopedia.com' target='_blank'><img style='border-style:none;' src='https://www.vinopedia.com/images/ListedOnVinopediaSmall.jpg' /></a><br/>These wines are listed here for demonstration purposes only, they are not really for sale (not that you would want to buy them for these prices, but anyway!)<br/>
<div> el.src ="testje"  el2.src="/testje" </div>
<table>
<%	HttpServletResponse res;
	Cookie[] theCookies = request.getCookies();
if (theCookies != null) {
    for (int i =0; i< theCookies.length; i++) {
       Cookie aCookie = theCookies[i];
       if (aCookie.getName().equals("fwstest")&&aCookie.getValue().equals("RP")){ %> <tr><td>Léoville Las Cases St. Julien 2eme Cru Classe </font>  </td><td>0,75 l</td><td>Vintage:1994</td><td>RP: 94</td></tr>
       <%} 
       if (aCookie.getName().equals("fwstest2")&&aCookie.getValue().equals("RP")){ %> <tr><td>Léoville Las Cases St. Julien 2eme Cru Classe </font>  </td><td>0,75 l</td><td>Vintage:1993</td><td>RP: 93</td></tr>
       <%} 
       
    }
    }%>

<tr><td><a href="https://www.vinopedia.com/wine/leoville las cases">Leoville las Cases</a></td><td>1990</td><td>St. Julien</td><td>120,00</td></tr>
<tr><td><a href="/wine/leoville las cases">Leoville las Cases</a></td><td>1991</td><td>St. Julien</td><td>40,00</td></tr>
<tr><td>Rieussec</td><td>2001</td><td>Sauternes</td><td>&euro; 150,00</td></tr>
<tr><td><img src='/images/myimage.jpg' alt=''/>Leoville las Cases</td><td>1993</td><td>St. Julien</td><td>300,00</td></tr>
<tr><td><a href='/images/label.jpg'><img src='/images/thumb.jpg' alt=''/></a>Leoville las Cases</td><td>1994</td><td>St. Julien</td><td>500,00</td></tr>
</table>
2008 Lemelson Thea’s Pinot Noir
<script type='text/javascript'>document.addEventListener("contextmenu", function(e) { 
	var re = /%u\d+/g;
	console.log(escape(window.getSelection()).replace(re,  "%20"));
	},false);</script>
</body></html>