<html>
<head>
<title>
vinopedia
</title>
<jsp:include page="/header.jsp" />

<script type="text/javascript">
<!--
function addFieldold () {
  if (document.getElementById) {
    var input = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        input.type = "hidden";
        input.name = "name";
        input.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        input.setAttribute('type', 'hidden');
        input.setAttribute('name', 'name');
        input.setAttribute('value', '');
      }
    document.getElementById('form').appendChild(input);
  }
   if (document.getElementById) {
    var input = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        input.type = "hidden";
        input.name = "vintage";
        input.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        input.setAttribute('type', 'hidden');
        input.setAttribute('name', 'vintage');
        input.setAttribute('value', '');
      }
    document.getElementById('form').appendChild(input);
  }
document.getElementById('form').submit() 
}
function addField () {
  if (document.getElementById) {
    var inputname = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        inputname.type = "TEXT";
        inputname.name = "name";
        inputname.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        inputname.setAttribute('type', 'TEXT');
        inputname.setAttribute('name', 'name');
        inputname.setAttribute('value', '');
      }
    document.getElementById('form').appendChild(inputname);
  }
   if (document.getElementById) {
    var inputvintage = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        inputvintage.type = "TEXT";
        inputvintage.name = "vintage";
        inputvintage.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        inputvintage.setAttribute('type', 'TEXT');
        inputvintage.setAttribute('name', 'vintage');
        inputvintage.setAttribute('value', '');
      }
    document.getElementById('form').appendChild(inputvintage);
  }
document.getElementById('form').submit() 
}

function setRowCount(num){
rowCount=num;
}

function newrow(){


  if (document.getElementById) {
    var inputname = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        inputname.type = "TEXT";
        inputname.name = "name";
        inputname.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        inputname.setAttribute('type', 'TEXT');
        inputname.setAttribute('name', 'name');
        inputname.setAttribute('value', '');
      }
    var addednamefield=document.getElementById('form').appendChild(inputname);
  }
   if (document.getElementById) {
    var inputvintage = document.createElement('INPUT');
      if (document.all) { // what follows should work 
                          // with NN6 but doesn't in M14
        inputvintage.type = "TEXT";
        inputvintage.name = "vintage";
        inputvintage.value = "";
      }
      else if (document.getElementById) { // so here is the
                                          // NN6 workaround
        inputvintage.setAttribute('type', 'TEXT');
        inputvintage.setAttribute('name', 'vintage');
        inputvintage.setAttribute('value', '');
      }
    document.getElementById('form').appendChild(inputvintage);
  }



var tabel=document.getElementById('wines');
var tbody = document.getElementById('wines').getElementsByTagName("TBODY")[0];

var row = document.createElement("TR");

row.setAttribute('id', 'row' + rowCount);

var t1 = document.createElement("TD");
t1.innerHTML=rowCount;
t1.appendChild(addednamefield);
document.getElementById('form').submit();

var t2 = document.createElement("TD");
t2.appendChild(inputvintage);

var t3 = document.createElement("TD");
//alert("t1:"+t1.innerHTML+", t2:"+t2.innerHTML);

row.appendChild(t1);
row.appendChild(t2);
row.appendChild(t3);

tbody.appendChild(row)

t3.innerHTML='<input type="button" onclick="deleterow(this)" value="Delete row">';

rowCount++;
document.getElementById('form').submit();
	}	

function deleterow(link){
var tbl = document.getElementById("wines");
 var tableRow = link.parentNode.parentNode; //gets TR object
  tbl.deleteRow(tableRow.rowIndex);
	}	

-->
</script>
<br/><br/>

<%@ page   
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.obsolete.Mywines"

	
	
%>
<%	session = request.getSession(true); 
	%>
	<jsp:useBean id="mywines" class="com.freewinesearcher.obsolete.Mywines" scope="session"/>
	<jsp:setProperty name="mywines" property="vintage" param="vintage"/> 
	<jsp:setProperty name="mywines" property="*"/> 
		
	

<Table id="wines"><TBODY>
<form id='form' action = "<%=response.encodeURL("editmywinesold.jsp")%>" method="POST">
<% 	if (mywines.getName()!=null){
	for (int i=0;i<mywines.getName().length;i++){ %>
			<TR id='<%="row"+i%>'><TD><%=i%><INPUT TYPE="TEXT" NAME="name" value="<%=mywines.getName()[i]%>" size=25></TD><TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=mywines.getVintage()[i]%>" size=25></TD><TD><input type="button" onclick="deleterow(this)" value="Delete row"></TD></TR>
			<%}
			}%>
			
			
<input name="Submit" type="submit">
<input type="button" onclick="newrow()" value="Insert row">
<input type="button" onclick="addField()" value="addField">
</TBODY>
</TABLE>	
<script type="text/javascript">
setRowCount(<%=mywines.getName().length%>);
</script>
<!--main-->			


</div>

</body> 
</html>