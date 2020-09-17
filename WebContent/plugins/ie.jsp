<%@page import="com.freewinesearcher.online.PageHandler"%><%PageHandler p=PageHandler.getInstance(request,response,"IE Extension install");
p.getLogger().logaction();
%><?xml version="1.0" encoding="UTF-8"?>
<os:openServiceDescription
    xmlns:os="http://www.microsoft.com/schemas/openservicedescription/1.0">
    <os:homepageUrl>https://www.vinopedia.com</os:homepageUrl>
    <os:display>
        <os:name>Find on Vinopedia</os:name>
        <os:icon>https://www.vinopedia.com/favicon4.ico</os:icon>
        <os:description>Find the best online wine prices on Vinopedia.com</os:description>
    </os:display>
    <os:activity category="Wine Search">
        <os:activityAction context="selection">
            <os:execute action="https://www.vinopedia.com/add-on/ie/{selection}" method="get">
 
            </os:execute>
        </os:activityAction>
    </os:activity>
</os:openServiceDescription> 
