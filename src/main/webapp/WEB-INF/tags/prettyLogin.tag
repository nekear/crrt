<%@ tag import="com.github.DiachenkoMD.entities.dto.users.AuthUser" %>
<%
    AuthUser currentUser = ((AuthUser) session.getAttribute("auth"));

    out.print(currentUser.getLogin());
%>