<!DOCYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
</head><body>
<%@ page import = "com.freewinesearcher.online.*"	
 import = "com.freewinesearcher.common.*"	
 import = "com.freewinesearcher.batch.Coordinates"	%>
 
<%
StoreLocator sl=new StoreLocator();
try{sl.setProducer(Integer.parseInt(request.getParameter("id")));}catch(Exception e){}
if (sl.getProducer()>0) Dbutil.executeQuery("update kbproducers set demopage=now() where id="+sl.getProducer());
PageHandler p=PageHandler.getInstance(request,response,"Storelocatordemo "+sl.getProducer()); 

%>
<h1>Localisateur de boutiques pour les vins de <%=sl.producername %></h1>
Les visiteurs de votre site web peuvent vouloir rechercher des d�taillants � qui ils peuvent acheter votre vin. Vinopedia est un moteur de recherche de vins sachant exactement qui vend vos produits. Nous avons cr�� un widget de "localisateur de boutiques" sur lequel vous pouvez placer votre propre site web en affichant les visiteurs vendant vos vins.<br/>
<h2>Fonctionnement</h2>
Le widget est extr�mement simple d'utilisation. Nous d�tectons l'emplacement du visiteur et lui montrons tous les d�taillants revendant vos vins � proximit�. Lorsqu'un visiteur clique sur une boutique, nous lui pr�sentons lesquels de vos vins sont en stock.<br/> 
En zoomant ou en faisant glisser la carte, ils peuvent rechercher la disponibilit� des vins dans d'autres emplacements.
<h2>Essayez-le !</h2>
La fen�tre ci-dessous montre comment le localisateur apparaitra sur votre site web. Utilisez la fonction de zoom pour visualiser la disponibilit� de vos vins en Europe, aux �tats-Unis, en Australie, etc.<br/><br/>
<div id='storelocator' style='border:1px solid black;width:900px;height:500px;'>Loading store locator from <a href='https://www.vinopedia.com/winery/<%=Webroutines.URLEncodeUTF8Normalized(sl.producername).replaceAll("%2F", "/").replace("&", "&amp;") %>' target='_blank' id='vplink'>Vinopedia.com</a>
<noscript><h4><font color='red'>Javascript in currently disabled in your browser. In order to use the store locator you need to enable Javascript. </font></h4></noscript>
<script src='https://<%=(Configuration.serverrole.equals("DEV")?"test":"www") %>.vinopedia.com/js/injectstorelocator.jsp?id=<%=sl.getProducer() %>' ></script></div>
<h2>Comment placer ce widget sur mon site web</h2>
Rien de plus simple : Tout ce dont vous avez besoin est de copier/coller la partie du code html suivante dans votre site web.<br/><br/>
<code>
&lt;div id='storelocator' style='border:1px solid black;width:900px;height:500px;'&gt;<br/>Loading store locator from &lt;a href='https://www.vinopedia.com/winery/<%=Webroutines.URLEncodeUTF8Normalized(sl.producername).replaceAll("%2F", "/").replace("&", "&amp;") %>' target='_blank' id='vplink'&gt;Vinopedia.com&lt;/a&gt;
&lt;noscript&gt;&lt;h4&gt;&lt;font color='red'&gt;Javascript in currently disabled in your browser. In order to use the store locator you need to enable Javascript. &lt;/font&gt;&lt;/h4&gt;&lt;/noscript&gt;
&lt;script src='https://www.vinopedia.com/js/injectstorelocator.jsp?id=<%=sl.getProducer() %>' &gt;&lt;/script&gt;&lt;/div&gt;

</code><br/><br/>
En copiant le code ci-dessus, le localisateur de boutiques fera partie int�grante de votre site en l'espace d'une minute. Ce dernier est personnalis� pour les vins de Miguel Torres. La premi�re ligne permet la personnalisation de la fen�tre du localisateur de boutiques. Vous pouvez par exemple en changez la taille. Le reste du code doit rester intact. 
<br/><br/>
<h2>Principe de fonctionnement</h2>
<a href='/'>Vinopedia.com</a> est un moteur de recherche destin� aux vins. Chaque jour, nous suivons le stock de milliers de boutiques vinicoles aux quatre coins du monde. Nous mettons ces informations gratuitement � disposition des acheteurs afin qu'ils puissent savoir o� acqu�rir le vin qu'ils recherchent.<br/>
Notre moteur de recherche reconna�t les vins individuellement � partir de diff�rents producteurs. En cons�quence, nous savons qui vend le vin que vous produisez. D�s qu'un magasin d�cide de commercialiser l'un de vos vins, il appara�t sur la carte, habituellement dans les 24 heures. <br/>
Si vous trouvez des boutiques manquantes dans notre liste, n'h�sitez-pas � les contacter afin de les informer qu'elles peuvent �galement �tre r�pertori�es. Leurs propri�taires peuvent <a href='https://www.vinopedia.com/retailers.jsp'>nous contacter</a> pour plus d'informations.<br/>
</body></html><% p.logger.logaction();
%>