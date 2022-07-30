<%@ page import="com.github.DiachenkoMD.dto.User" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    List<User> users = (List<User>) request.getAttribute("usersList");
%>
<html>
<head>
    <title>Users List</title>
</head>
<body>
<ul>
    <%
        for(User user : users){
    %>
        <li><%=user.getUsername()%></li>
    <%
        }
    %>
</ul>
</body>
</html>
