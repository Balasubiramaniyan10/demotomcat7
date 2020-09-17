<html>
<head>
<title>
vinopedia
</title>

<jsp:include page="/header.jsp" />


<script type="text/javascript">


function moreFields()
{
	counter++;
	var newFields = document.getElementById('readroot').cloneNode(true);
	newFields.id = '';
	newFields.style.display = 'block';
	var newField = newFields.childNodes;
	for (var i=0;i<newField.length;i++)
	{
		var theid = newField[i].id
		if (theid='msg')
			newField[i].id = theid + counter;
	}
	
	var insertHere = document.getElementById('writeroot');
	insertHere.parentNode.insertBefore(newFields,insertHere);
}

// --------------------------------------------
//                  setfocus
// Delayed focus setting to get around IE bug
// --------------------------------------------

function setFocusDelayed()
{
  global_valfield.focus();
}

function trim(str)
{
  return str.replace(/^\s+|\s+$/g, '');
}

function setfocus(valfield)
{
  // save valfield in global variable so value retained when routine exits
  global_valfield = valfield;
  setTimeout( 'setFocusDelayed()', 100 );
}


// --------------------------------------------
//                  msg
// Display warn/error message in HTML element.
// commonCheck routine must have previously been called
// --------------------------------------------

function msg(fld,     // id of element to display message in
             msgtype, // class to give element ("warn" or "error")
             message) // string to display
{
  // setting an empty string can give problems if later set to a 
  // non-empty string, so ensure a space present. (For Mozilla and Opera one could 
  // simply use a space, but IE demands something more, like a non-breaking space.)
  var dispmessage;
  if (emptyString.test(message)) 
    dispmessage = String.fromCharCode(nbsp);    
  else  
    dispmessage = message;

  var elem = document.getElementById(fld);
  elem.firstChild.nodeValue = dispmessage;  
  
  elem.className = msgtype;   // set the CSS class to adjust appearance of message
}

// --------------------------------------------
//            commonCheck
// Common code for all validation routines to:
// (a) check for older / less-equipped browsers
// (b) check if empty fields are required
// Returns true (validation passed), 
//         false (validation failed) or 
//         proceed (don't know yet)
// --------------------------------------------

var proceed = 2;  

function commonCheck    (valfield,   // element to be validated
                         infofield,  // id of element to receive info/error msg
                         required)   // true if required
{
  if (!document.getElementById) 
    return true;  // not available on this browser - leave validation to the server
  var elem = document.getElementById(infofield);
  if (!elem.firstChild) return true;  // not available on this browser 
  if (elem.firstChild.nodeType != node_text) return true;  // infofield is wrong type of node  

  if (emptyString.test(valfield.value)) {
    if (required) {
      msg (infofield, "error", "ERROR: field is required");  
      setfocus(valfield);
      return false;
    }
    else {
      msg (infofield, "warn", "");   // OK
      return true;  
    }
  }
  return proceed;
}

// --------------------------------------------
//            validatePresent
// Validate if something has been entered
// Returns true if so 
// --------------------------------------------

function validatePresent(valfield,   // element to be validated
                         infofield ) // id of element to receive info/error msg
{
  var stat = commonCheck (valfield, infofield, true);
  if (stat != proceed) return stat;

  msg (infofield, "warn", "");  
  return true;
}

// --------------------------------------------
//               validateEmail
// Validate if e-mail address
// Returns true if so (and also if could not be executed because of old browser)
// --------------------------------------------

function validateEmail  (valfield,   // element to be validated
                         infofield,  // id of element to receive info/error msg
                         required)   // true if required
{
  var stat = commonCheck (valfield, infofield, required);
  if (stat != proceed) return stat;

  var tfld = trim(valfield.value);  // value of field with whitespace trimmed off
  var email = /^[^@]+@[^@.]+\.[^@]*\w\w$/  ;
  if (!email.test(tfld)) {
    msg (infofield, "error", "ERROR: not a valid e-mail address");
    setfocus(valfield);
    return false;
  }
}
// --------------------------------------------
//               validateVintage
// --------------------------------------------

function validateVintage (valfield)   
{
  //var stat = commonCheck (valfield, infofield, false);
  //if (stat != proceed) return stat;
	var vintage = /^((19|20)\d\d)?$/  ;
  valfield.parentNode.parentNode.getElementsByTagName('TD')[4].innerHTML='';
  if (!vintage.test(valfield.value)) {
    valfield.parentNode.parentNode.getElementsByTagName('TD')[4].innerHTML='Error: vintage is incorrect';
    setfocus(valfield);
    return false;
  }
}

function validateVintages(){
var vintage=document.getElementsByName("vintage");
for (var i=0;i<vintage.length;i++)
validateVintage(vintage[i]);

}



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
	DecimalFormatSymbols DFS = new DecimalFormatSymbols();
	DFS.setDecimalSeparator('.');
	
	NumberFormat format  = new DecimalFormat("#0.00",DFS); 

	
	%>
	<jsp:useBean id="mywines" class="com.freewinesearcher.obsolete.Mywines" scope="session"/>
	<jsp:setProperty name="mywines" property="vintage" param="vintage"/> 
	<jsp:setProperty name="mywines" property="*"/> 
		

<div id="readroot" style="display: none">
<Table class="mywines"><TR><TD class="wine"><INPUT TYPE="TEXT" NAME="name" value="" size=70></TD>
<TD class="vintage"><INPUT TYPE="TEXT" NAME="vintage" value=""  ONCHANGE="validateVintage(this, 'msg')" size=10></TD>
<TD class="price"><INPUT TYPE="TEXT" NAME="price" value="0.0" size=10></TD>
<TD><input type="button" value="Remove wine" style="font-size: 10px"
		onClick="
			this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode);
		"></TD>
<TD id="msg">message goes here</TD></TR>
		</TR>
</table>
</div>

	
<form id='form' action = "<%=response.encodeURL("editmywines.jsp")%>" method="POST">

<TABLE class="mywines" name="mywines"><TR><TD class="wine">Name</TD><TD class="vintage" align='left'>Vintage</TD><TD width=10%>Price</TD><TD></TD></TR></table>
<% 	if (mywines.getName()!=null){
	for (int i=0;i<mywines.getName().length;i++){ %>
<div id="pre" style="display: block">
<TABLE id="table<%=i%>" class="mywines"><TR><TD class="wine"><INPUT TYPE="TEXT" NAME="name" value="<%=mywines.getName()[i]%>" size=70></TD>
<TD class="vintage"><INPUT TYPE="TEXT" NAME="vintage" value="<%=mywines.getVintage()[i]%>" ONCHANGE="validateVintage(this)" size=10></TD>
<TD class="price"><INPUT TYPE="TEXT" NAME="price" value="<%=format.format(mywines.getPrice()[i])%>" align='right' size=10></TD>
<TD><input type="button" value="Remove wine" style="font-size: 10px"
		onClick="
			this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode);
		"></TD>
<TD>Validating...</TD>
</TR>
</table></div>

			<%}
			}%>
			

<span id="writeroot"></span>

<input type="button" value="Add wine" onClick="moreFields()">
<input name="Submit" value="Save" type="submit">

</form>
<script type="text/javascript">
validateVintages();
<% if (mywines.getName()!=null) {%>
var counter = <%=mywines.getName().length%>;
<%} else {%>
var counter=0;
<%}%>

</script>
<!--main-->			


</div>

</body> 
</html>