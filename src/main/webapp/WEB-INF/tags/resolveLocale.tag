<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="java.util.Optional" %>

<%
request.setAttribute("endLocale", Optional.of(request.getSession().getAttribute("lang")).orElse("en"));
%>
