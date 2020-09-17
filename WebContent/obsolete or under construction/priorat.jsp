<html>
<head>
<title>
vinopedia
</title>
<jsp:include page="/header.jsp" />
<br/><br/>
<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	actionurl=actionurl+"&pricemin="+form.priceminstring.value;
	actionurl=actionurl+"&pricemax="+form.pricemaxstring.value;
	actionurl=actionurl+"&rareoldstring="+form.rareoldstring.value;
  	document.Searchform.action=actionurl;
	form.submit();
	
  	return 0;
}
-->
</script>
<!-- google_ad_section_end -->

<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"

	
	
%>
<%
	session = request.getSession(true);
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
		
<%
			}else {
		%>
		<jsp:setProperty name="searchdata" property="*"/> 
		
		<%
 					}

 					if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
 				%>
		<jsp:setProperty name="searchdata" property="vintage" value="<%=(searchdata.getVintage()+" "+Webroutines.getVintageFromName(searchdata.getName())%>"/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}
 		String ipaddress="";
 	    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
 	    	ipaddress = request.getRemoteAddr();
 	    } else {
 	        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
 	    }

 	    if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
 	    	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
 	    	Webroutines.logWebAction("NZ: Access denied Tips",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,new Float(0), new Float(0), "", false, "", "", "", "",0.0);
 			
 	    } else {
 	    
 	    	String[] winenames={"Doix Costers de Vinyes Velles ","Vega Sicilia Unico ","Pesus ","Nebro ","Muga Aro ","Liberalia Enológica Cinco Reserva ","El Nido ","Alvear Pedro Ximénez Solera ","Amancio ","Valdegatiles ","Contador ","Aquilon ","Pingus -flor","Avan Cepas Centenarias ","Mauro Terreus ","Mauro Vendimia Seleccionada ","Palomero ","Paisajes I ","Vinos Pinol Mistela Blanca ","Santa Cruz de Artazu ","Dos Victorias Gran Elías Mora ","Alion ","Allende Aurus ","Salanques ","Alvear Solera Cream","Vega Sicilia Único ","Matallana ","Torre Muga ","Paciena Cuvee d'Exception ","Alvear Pedro Ximénez de Añada ","El Bosque ","Cervoles ","Masis Carreras ","Luberri Finca Los Merinos Cepas Viejas ","Bodegas Valsardo Reserva Superior ","Jarrarte ","Alonso del Yerro María ","La Cueva del Contador ","Emilio Moro Malleolus ","Termanthia ","Mauro Vendimia Seleccionada ","Pintia ","Remirez de Ganuza ","L'Ermita ","Remelluri Collección Jaime Rodríguez ","Alto Moncayo ","Bodegas Vizcarra Ramos Celia ","Flor de Pingus ","Cyan Vendimia Seleccionada ","Clos Erasmus ","Marqués de Murrieta Dalmau Reserva ","Corullón ","Hermanos Sastre Regina Vides ","Viña Pedrosa Reserva ","Celler de Capcanes Val de Calas ","Celler del Roure Maduressa ","Fra Fulco Vilella Alta ","Artadi Viñas de Gain ","Clos d'Englora","Vall Llach ","Sierra Cantabria Cuvee Especial ","Bodegas Olivares Dulce Monastrell","Tilenus Pagos de Posada ","Finca Allende Calvario ","Allende ","Finca Sandoval ","Tomas Cusiné Geol ","Neo Daniel ","Bellum el Remate ","Bellum Providencia","El Nido Clio ","San Vicente Reserva ","Altos de Lanzaga ","Pago la Jara ","Pazo de Señorans ","Hacienda Monasterio Crianza ","Muga Prado Enea Gran Reserva ","Muga Reserva ","Masia Sera Ino","Vega Sicilia Valbuena ","Mas de Camperol ","Pujaza Norte ","Quinta de la Quietud ","Luna Berberide Daniel ","Dos Victorias Gran Elías Mora Daniel ","Mas D'en Compte Blanco ","Les Alcusses ","Mas Donis Barrica ","El Bugader ","L'Avi Arrufi ","Vinos sin ley G5 ","Coma d'en Pou ","Pérez Pascuas Gran Selección Gran Reserva ","Ribas de Cabrera ","Pétalos de Bierzo ","Lan Edición Limitada ","Numanthia ","Clos Figueras ","Dominio de Atauta ","Les Brugueres ","Montebaco Crianza ","Avan Concentracion ","Bodegas Peique Seleccion Familiar ","Finca Dofi ","6 Vinyes de Laurona ","Castaño Solanera ","Cyan Vendimia Seleccionada ","Cyan Crianza ","Clos Erasmus Laurel ","Rottlán Torra Tirant ","Paisajes VIII ","Malleolus de Valderamiro ","Amadis ","Francesca Vicent Robert Abat Domenech ","Finca Terrerazo ","Pago de Santa Cruz+","Cirsion ","Pares Balta Gratavinum ","Ferreiro Albariño ","Juan Gil ","Juan Gil ","Vinos Pinol Mater Teresina","Vinos Pinol Portal Crianza","El Bugader ","Pujanza Daniel ","Alonso del Yerro ","Marta Fabra Seleccio Vinyes Velles ","Genium ","Aalto ","Garnacha de l'Emporda","Sierra Cantabria Crianza ","Masia Sera Gneis ","Finca Villacreces Reserva ","Ercavio Roble ","Mas Garrian Clos Severi ","Mas Garrian Mas del Camperol ","Cabeza de Cuba Crianza Seleccion ","Pazo de Señorans Albariño Seleccion de Añada ","Bodegas Matarredonda Libranza ","Telmo Rodríguez M2 ","Ramirez de la Piscina Gran Reserva ","Senorio de Barrahonda Monastrell Tinto ","Liberalia Enológica Tres ","Bodegas Bernabe Navarro Leva Daniel ","Neo ","Pazo de Barrantes ","Monte Negro Crianza ","Muga Reserva Seleccion Especial ","Tomas Cusiné Vilosell ","La Rioja Alta Gran Reserva 904 ","Muga Blanco"};
 	    	//String[] winenames={"Doix Costers de Vinyes Velles ","Vega Sicilia Unico ","Pesus ","Nebro "};
 	    	ArrayList<String> countries = Webroutines.getCountries();
 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 	%>

<TABLE  class="main" >
	<TR><TD class="left">
	
	<!--search-->
	</TD><TD class="centre"><%=Webroutines.getConfigKey("systemmessage")%>
	
	

	
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start -->
			<H4>Priorat searcher</H4><BR/>
			Every day we check for new wines that come to the market. If we find a wine interesting because is was offered cheaply, we put it on our tip list. This list changes every day, and if you are hunting bargains it should not be missed!<BR>
			<table><tr><td>Name</td>
			
			<% 
			for (int vint=1994;vint<2007;vint++){
				out.print("<td>"+vint+"</td>");
			}
			out.print("</tr>");
			NumberFormat format  = new DecimalFormat("#,##0.00");	
			
			Wineset wineset =null;
			for (String winename : winenames){
			out.print("<tr><td><a href='/wine/"+winename+"'>"+winename+"</a></td>");
				for (int vint=1994;vint<2007;vint++){
					wineset = new Wineset(winename,vint+"", 0,searchdata.getPricemin(), searchdata.getPricemax(), new Float(0.75),searchdata.getCountry(), searchdata.getRareold(), "TRUE",searchdata.getOrder(),searchdata.getOffset(),1);
					if (wineset!=null&&wineset.Wine.length>0){
					out.print("<td>&euro;&nbsp;"+format.format(wineset.Wine[0].PriceEuroIn)+"</td>");
					} else {
						out.print("<td></td>");
							
					}
				}
				%></tr><%
			}
			
			%>
</table>
	<!--hints-->
	<!-- google_ad_section_end -->

</TD><TD class="right">
		
	</TD></TR>
</TABLE>	
<!--main-->		
<jsp:include page="/footer.jsp" />	
<%} //NZ filter %>

</div>

</body> 
</html>