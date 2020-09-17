<%@page import="com.freewinesearcher.online.Auditlogger"%><jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/><%
cu.setAl(new Auditlogger(request));
out.write(cu.update(request));%>