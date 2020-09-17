<%@page import="com.searchasaservice.parser.xpathparser.XpathParser"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.common.datafeeds.DataFeed"%>
<% int shopid=Integer.parseInt(request.getParameter("shopid"));
	DataFeed datafeed=DataFeed.getDataFeed(new Context(1), shopid,0);
	if (datafeed!=null){%>
		<script type="text/javascript">location.href="/settings/editdatafeed.jsp?shopid=<%=shopid%>&action=Retrieve";</script><% 
	} else if (XpathParser.getXpathParsers(new Context(1), shopid).size()>0){%>
	<script type="text/javascript">location.href="/moderator/analyzer.jsp?shopid=<%=shopid%>&actie=retrieve";</script>
	<% 
}else {%>
	<script type="text/javascript">location.href="/moderator/edittablescraper.jsp?shopid=<%=shopid%>&actie=retrieve";</script>
		<% 
	}%>