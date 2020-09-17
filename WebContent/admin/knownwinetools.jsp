<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.common.Dbutil"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<title>
Knownwines editors <%=Wijnzoeker.version%>
</title>
<%@ include file="/header.jsp" %><br/>
Filter wrongly recognized wines<br/>
<a href="<%= response.encodeURL("edittoocheapwines.jsp")%>">If it sounds too good to be true, it probably is...</a><br/>
<a href="<%= response.encodeURL("editBestPQRatedWines.jsp")%>">Edit best PQ rated wines</a><br/>
<br/>Correct wines that have been identified as two or more different wines<br/>
<a href="<%= response.encodeURL("editdoubleratedwines.jsp")%>">Edit double knownwines in rated wines</a><br/>
<a href="<%= response.encodeURL("editknownwineslist.jsp")%>">Edit Double Entries Known Wines List</a><br/>
<br/>Analysis of terms that are unnecessary<br/>
<a href="<%= response.encodeURL("editimprovedknownwines.jsp")%>">Commit redundant terms improvedknownwines</a><br/>
<a href="<%= response.encodeURL("editdoubleimprovedknownwines.jsp")%>">Edit double terms improvedknownwines</a><br/>
<br/>Analysis of wines that were not recognized at all<br/>
<a href="<%= response.encodeURL("editunrecognizedratedwines.jsp")%>">Edit unrecognized rated wines</a><br/>
<a href="<%= response.encodeURL("editalmostmatchedwines.jsp")%>">Edit Almost Matched Known Wines List</a><br/>
<br/>Obsolete:<br/>
<a href="<%= response.encodeURL("editknownwinesinternaldoubles.jsp")%>">Edit internal double terms in Knownwines</a><br/>

</div>
</body> 
</html>

