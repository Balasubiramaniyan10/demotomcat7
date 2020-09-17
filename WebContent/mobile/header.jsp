<%@ page import="com.freewinesearcher.common.Configuration"%><link rel="icon" type="image/png" href="<%=Configuration.staticprefix %>/favicon.png" /> 
<div data-role="header" data-position="fixed"><h1><a href="/m" title="home"><span class='vino'>vino</span><span class='pedia'>pedia</span></a></h1>
<span  onclick='javascript:$(".searchform").slideDown();scroll(0,0);$(".srchname").focus();return false;' class="ui-btn-left ui-btn ui-btn-icon-left ui-btn-corner-all ui-shadow ui-btn-up-a" ><span class="ui-btn-inner ui-btn-corner-all"><span class="ui-btn-text">Search</span><span class="ui-icon ui-icon-search ui-icon-shadow"></span></span></span>
<a href="/mobile/settings.jsp?keepdata=true" data-icon="gear" class="ui-btn-right" title="Settings">Settings</a></div><!-- /header -->
<form onsubmit="$('.searchform').hide();" action="/m?sneakpreview=true" data-ajax="false" method="post" class="searchform"><div data-role="fieldcontain">
    <input type="search" name="name" class='srchname' value="" data-theme="c" />
</div></form>

