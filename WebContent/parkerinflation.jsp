<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<html>
<head>
<title>
Vinopedia: Parker rating inflation?
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Parkerinflation");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h1>Is Parker te scheutig geworden met punten?</h1>
De laatste jaren zijn er geluiden te horen dat de "Robert Parker punten" de Robert Parker punten niet meer zijn. Vroeger werd een wijn lovend besproken en kreeg hij 86 punten. 90 punten of meer was een uitzondering. Tegenwoordig krijgt bijna elke redelijke wijn 90 punten ook al valt hij een beetje tegen. En 94 of meer punten is ook doodnormaal. Zo gaat althans de theorie. <br/>
<br/>
Met name de reviews van Jay Miller (namens Robert Parker jr.) van Spanje 2004 leidden tot veel commotie gegeven het hoge puntengemiddelde en maar liefst 5 100-punten wijnen. De inflatie zou overigens niet voor alle gebieden en reviewers gelden: Antonio Galloni, die voor Parker Italië reviewt, zou veel gematigder zijn in het toekennen van hoge punten. Daarmee zou de puntenwaardering van Spaanse wijnen t.o.v. Italiaanse wijnen scheef komen te liggen terwijl de kwaliteit vergelijkbaar zou zijn.<br/><br/>
Vinopedia onderzocht of dit ook daadwerkelijk terug te vinden is in de gegeven punten, en of er een verschil qua waardering is met vroegere goede jaren. Bijgaand een analyse van ratings in 2004, 2001 en 1990. De aantallen zijn gebaseerd op de ratings zoals deze op 1 februari 2009 beschikbaar waren.<br/>
<br/><h2>Spanje vs. Italië in 2004</h2>
In de tabel staan per gebied het aantal wijnen dat een review kreeg (niet alle geproefde wijnen kregen dit, sommigen waren onder de maat). Verder het aantal wijnen dat een bepaald puntenaantal (of meer) scoorde.<br/><br/> 
<table rules="none" border="0" cellspacing="0" cols="9" frame="void">
	<colgroup><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"></colgroup>
	<tbody>
		<tr>
			<td width="120" align="left" height="17"><br></td>
			<td width="86" align="right">Totaal</td>
			<td width="86" align="right">90+</td>
			<td width="86" align="right">90+(%)</td>

			<td width="86" align="right">92+</td>
			<td width="86" align="right">92+(%)</td>
			<td width="86" align="right">94+</td>
			<td width="86" align="right">94+(%)</td>
			<td sdval="100" sdnum="1043;" width="86" align="right">100</td>
		</tr>

		<tr>
			<td align="left" height="17">Spanje 2004</td>
			<td sdval="693" sdnum="1043;" align="right">693</td>
			<td sdval="412" sdnum="1043;" align="right">412</td>
			<td sdval="0,594516594516595" sdnum="1043;0;0,00%" align="right">59,45%</td>
			<td sdval="205" sdnum="1043;" align="right">205</td>

			<td sdval="0,295815295815296" sdnum="1043;0;0,00%" align="right">29,58%</td>
			<td sdval="98" sdnum="1043;" align="right">98</td>
			<td sdval="0,141414141414141" sdnum="1043;0;0,00%" align="right">14,14%</td>
			<td sdval="5" sdnum="1043;" align="right">5</td>
		</tr>
		<tr>
			<td align="left" height="17">Italië 2004</td>

			<td sdval="949" sdnum="1043;" align="right">949</td>
			<td sdval="672" sdnum="1043;" align="right">672</td>
			<td sdval="0,708113804004215" sdnum="1043;0;0,00%" align="right">70,81%</td>
			<td sdval="358" sdnum="1043;" align="right">358</td>
			<td sdval="0,377239199157007" sdnum="1043;0;0,00%" align="right">37,72%</td>
			<td sdval="124" sdnum="1043;" align="right">124</td>

			<td sdval="0,130663856691254" sdnum="1043;0;0,00%" align="right">13,07%</td>
			<td sdval="0" sdnum="1043;" align="right">0</td>
		</tr>
	</tbody>
</table>
<br/>
Spanje wordt in 2004 dus niet voorgetrokken op Italië. Italië scoort over vrijwel de gehele linie relatief meer hoge punten. Daarbij moet worden aangetekend dat veel van de Italiaanse topwijnen uit 2004 nog niet gereviewed zijn, te verwachten is dat de scores voor 2004 hierdoor nog hoger zullen uitpakken. Dit lijkt de mythe te ontkrachten dat Galloni altijd laag scoort en Jay Miller elk sapje ultiem vindt. Alleen in de hoogste segmenten gaat Spanje het winnen. Die 5-0 zege Spanje-Italië bij de 100-punters... Zit Jay Miller hier fout? Of is het zo dat, <a href='http://www.drvino.com/2007/03/05/lake-wobegon-wines/#comment-11112' target='_blank'>zoals Jay Miller zelf zegt</a>, "If you can't give a 100 point score in the 2004 vintage, you'll never give one, it was that great a year."? Daar zit misschien wel wat in.<br/><br/>
<h2>Spanje vs. Italië in 2001</h2>
Zijn dit soort percentages nu te danken aan punteninflatie, aan een andere reviewer of aan een uitzonderlijk goed jaar dat gewoon de punten krijgt die het verdient? Laten we er een ander goed jaar bijpakken: 2001. Italië is gereviewed door Daniel Thomasses, Spanje door Robert Parker zelf.
<table rules="none" border="0" cellspacing="0" cols="9" frame="void">
	<colgroup><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"></colgroup>
	<tbody>
		<tr>

			<td width="120" align="left" height="17"><br></td>
			<td width="86" align="right">Totaal</td>
			<td width="86" align="right">90+</td>
			<td width="86" align="right">90+(%)</td>
			<td width="86" align="right">92+</td>
			<td width="86" align="right">92+(%)</td>

			<td width="86" align="right">94+</td>
			<td width="86" align="right">94+(%)</td>
			<td sdval="100" sdnum="1043;" width="86" align="right">100</td>
		</tr>
		<tr>
			<td align="left" height="17">Spanje 2001</td>
			<td sdval="421" sdnum="1043;" align="right">421</td>

			<td sdval="251" sdnum="1043;" align="right">251</td>
			<td sdval="0,596199524940618" sdnum="1043;0;0,00%" align="right">59,62%</td>
			<td sdval="137" sdnum="1043;" align="right">137</td>
			<td sdval="0,32541567695962" sdnum="1043;0;0,00%" align="right">32,54%</td>
			<td sdval="66" sdnum="1043;" align="right">66</td>
			<td sdval="0,156769596199525" sdnum="1043;0;0,00%" align="right">15,68%</td>

			<td sdval="0" sdnum="1043;" align="right">0</td>
		</tr>
		<tr>
			<td align="left" height="17">Italië 2001</td>
			<td sdval="1613" sdnum="1043;" align="right">1613</td>
			<td sdval="843" sdnum="1043;" align="right">843</td>
			<td sdval="0,522628642281463" sdnum="1043;0;0,00%" align="right">52,26%</td>

			<td sdval="372" sdnum="1043;" align="right">372</td>
			<td sdval="0,230626162430254" sdnum="1043;0;0,00%" align="right">23,06%</td>
			<td sdval="102" sdnum="1043;" align="right">102</td>
			<td sdval="0,0632362058276503" sdnum="1043;0;0,00%" align="right">6,32%</td>
			<td sdval="0" sdnum="1043;" align="right">0</td>
		</tr>
	</tbody>

</table>
<br/>
Aan de kant van Spanje is nog geen inflatie van punten te bespeuren. Robert Parker zelf gaf Spanje in 2001 relatief meer 90+ punten. Dit pleit Jay Miller op dit gebied min of meer vrij (alhoewel het de vraag is of 2004 het echt gaat halen bij 2001). Opvallend is wel dat in het sublieme Spaanse jaar 2001 Robert Parker geen enkele wijn 99 of 100 punten gaf.
<br/><br/><h2>Frankrijk 1990</h2>
Wil je echt naar punteninflatie kijken, dan moet je verder teruggaan. We gaan naar Frankrijk in het legendarische jaar 1990, dat o.a. in de Bordeaux en de Rhône vele toppers kende.<br/>
<table rules="none" border="0" cellspacing="0" cols="9" frame="void">

	<colgroup><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"><col width="86"></colgroup>
	<tbody>
		<tr>
			<td width="120" align="left" height="17"><br></td>
			<td width="86" align="right">Totaal</td>
			<td width="86" align="right">90+</td>
			<td width="86" align="right">90+(%)</td>

			<td width="86" align="right">92+</td>
			<td width="86" align="right">92+(%)</td>
			<td width="86" align="right">94+</td>
			<td width="86" align="right">94+(%)</td>
			<td sdval="100" sdnum="1043;" width="86" align="right">100</td>
		</tr>

		<tr>
			<td align="left" height="17">Frankrijk 1990</td>
			<td sdval="1858" sdnum="1043;" align="right">1858</td>
			<td sdval="531" sdnum="1043;" align="right">531</td>
			<td sdval="0,285791173304629" sdnum="1043;0;0,00%" align="right">28,58%</td>
			<td sdval="305" sdnum="1043;" align="right">305</td>

			<td sdval="0,164155005382131" sdnum="1043;0;0,00%" align="right">16,42%</td>
			<td sdval="155" sdnum="1043;" align="right">155</td>
			<td sdval="0,0834230355220667" sdnum="1043;0;0,00%" align="right">8,34%</td>
			<td sdval="16" sdnum="1043;" align="right">16</td>
		</tr>
	</tbody>
</table>
<br/>
En inderdaad zien we hier heel andere getallen. Maar 29% van de wijnen haalt de 90-puntengrens. Parker omschrijft in zijn <a href='http://www.erobertparker.com/Members/info/legend.asp' target='_blank'>puntenschaal</a> wijnen met 90-95 punten als "An outstanding wine of exceptional complexity and character. In short, these are terrific wines". 29% exceptionele wijnen lijkt redelijk voor een uitstekend jaar. Als 60-70% van de gereviewde wijnen exceptioneel zijn, dan klopt er wellicht iets niet met de maatstaf van de gemiddelde wijnen.<br/>
<br/>Overigens kregen 16 op de 1858 wijnen 100 punten. Percentueel (0,86%) dus hoger dan Spanje 2004 met 5 op de 693 wijnen (0,72%). Nu was 1990 in de Bordeaux en Rhône ook echt geweldig, maar Jay Millers conclusie dat in een topjaar in Spanje 5 100-punten wijnen een redelijk aantal is lijkt hiermee gestaafd.
<br/><h2>Conclusie</h2>
Harde conclusies zijn moeilijk te trekken, aangezien reviews van verschillende gebieden van verschillende jaren met elkaar vergeleken worden. Veel punten voor een gebied in een specifiek jaar kan gewoon betekenen dat het een uitzonderlijk jaar was. Toch zijn wel wat trends te ontdekken.<br/><br/>
Het percentage wijnen dat minder dan 90 punten scoort in een topjaar is de afgelopen 20 jaar sterk afgenomen. Dat kan deels liggen aan verbeterde wijntechnieken, maar een stijging van het percentage exceptionele wijnen van 30% naar 60%-70% lijkt niet alleen hierdoor verklaard te kunnen worden. Inderdaad lijkt er sprake te zijn van punteninflatie in de afgelopen 20 jaar, alhoewel niet gekeken is naar mindere jaren. <br/>
<br/>Voor het voortrekken van Spanje op Italië is geen bewijs gevonden: Beide hebben geprofiteerd van de "punteninflatie".
<br/><br/>
Jasper Hammink, 24 februari 2009

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	

</body> 
</html>