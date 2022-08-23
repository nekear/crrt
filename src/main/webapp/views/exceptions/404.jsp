<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>

<%@ include file="../components/generals.jspf"%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>404 | СRRT.</title>
    <link rel="stylesheet" href="${assets}css/themes/${requestScope.get("endTheme")}.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/page_forbidden.css">
</head>
<body>
<div class="exception-wrapper">
    <div class="forbidden-container">
        <div class="forbidden-content">
            <h1>404</h1>
            <h2><fmt:message key="forbidden.404.title" /> </h2>
        </div>

        <c:choose>
            <c:when test="${not empty user}">
                <a href='profile' class="mdx-hover-underline-animation forbidden-return"><fmt:message key="forbidden.return"/></a>
            </c:when>
            <c:when test="${empty user}">
                <a href='${pageContext.request.contextPath}/' class="mdx-hover-underline-animation forbidden-return"><fmt:message key="forbidden.return"/> </a>
            </c:when>
        </c:choose>
    </div>
</div>

<div class="logo">
    <a href='${pageContext.request.contextPath}/'><img src="${assets}imgs/CRRT.svg" alt="crrt car rent logo"></a>
</div>

</body>
</html>

