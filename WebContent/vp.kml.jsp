<%@ page contentType="text/xml" %><?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>	
    <NetworkLink>
        <description></description>
        <Link>
            <href>https://test.vinopedia.com/KML/<%=request.getParameter("session")%></href>
            <viewRefreshMode>onStop</viewRefreshMode>
            <viewRefreshTime>1</viewRefreshTime>
        </Link>
    </NetworkLink>
</Document>
</kml>