<%@ page 
import = "com.freewinesearcher.common.Configuration"
%><script type='text/javascript'>function showresult(){document.getElementById('mobileresult').style.display='inline';document.getElementById('mobileapp').style.display='none';}</script>
<div id='mobileapp'><h2>vinopedia now has a mobile app for your iPhone. Would you like to install it?</h2>
<a href='https://itunes.apple.com/us/app/vinopedia.com/id668028831?mt=8' title='Vinopedia iPhone app'><img style='margin:10px;width:135px;height:40px;' src='<%=Configuration.staticprefix %>/images/Download_on_the_App_Store_Badge_US-UK_135x40.png' alt='Vinopedia iPhone app' /></a>
<br/><br/><a href='?' onclick='javascript:showresult();return false;' alt='Show web version'>Nope, just let me use the web interface</a></div>