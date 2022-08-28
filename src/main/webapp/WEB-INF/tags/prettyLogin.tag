<%@ tag import="com.github.DiachenkoMD.entities.dto.users.AuthUser" %>
<%@ tag import="static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH" %>
<%
    AuthUser currentUser = ((AuthUser) session.getAttribute(SESSION_AUTH));

    out.print(currentUser.getLogin());
%>