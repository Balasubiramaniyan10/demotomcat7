<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<head> 
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type='text/javascript'>function loadframe(){if ($('#storeiframe').attr('src')=='') window.setTimeout("$('#storeiframe').attr('src','https://www.vinopedia.com/wine/Chateau+Latour?utm_source=vinopedia1&utm_medium=vinopedia2&utm_campaign=freshonlyjsload');",200);}</script>
</head>
<body >

<%@ include file="/snippets/topbar.jsp" %>

Test pagina frames<br/>
<a href='https://www.vinopedia.com/wine/Chateau+Latour?utm_source=www.obama.com&utm_medium=referral'>Click</a><br/>

	<iframe id="storeiframe" src=""  ></iframe>

</body>
</html>
