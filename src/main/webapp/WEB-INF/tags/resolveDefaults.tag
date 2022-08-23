<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="java.util.Optional" %>
<%@ tag import="com.github.DiachenkoMD.web.utils.Utils" %>
<%@ tag import="com.github.DiachenkoMD.entities.enums.VisualThemes" %>
<%@ tag import="java.util.Arrays" %>
<%@ tag import="java.util.stream.Collectors" %>

<%
    // Resolving lang
    request.setAttribute("endLocale", Optional.of(request.getSession().getAttribute("lang")).orElse("en"));

    // Resolving theme
    Cookie themeCookie = Utils.getCookieFromArray("theme", request.getCookies()).orElse(null);

    if(themeCookie != null) {
        request.setAttribute("endTheme", VisualThemes.valueOf(themeCookie.getValue()).getFileName());
    }else{
        request.setAttribute("endTheme", VisualThemes.DARK.getFileName());
    }
%>
