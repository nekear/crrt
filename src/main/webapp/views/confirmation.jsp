<%@ page import="com.github.DiachenkoMD.dto.StatusStates" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>

<c:set var="lang" value="en"/>

<fmt:setBundle basename="langs.i18n"/>
<fmt:setLocale value="${lang}" />

<!doctype html>
<html lang="${lang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <!-- Design libs -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/modules/argon/argon.min.css">

    <!--  Custom  -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/globals.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/colorize.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/mdx.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/status_rel.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/media.css">
    <title>Sign Status | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

    <!--  Custom  -->
    <script src="${pageContext.request.contextPath}/assets/js/mdx.js"></script>
</head>
<body>

<div class="content">
    <div class="pos-wrapper">
        <div class="logo">
            <a href="index.html"><img src="${pageContext.request.contextPath}/assets/imgs/CRRT.svg" alt="carrent crrt logo"></a>
        </div>
        <c:set var="status_type" value="${requestScope.get('conf_res').getState(0)}" scope="page" />
        <div class="status-data" data-status="${status_type.toString().toLowerCase()}">
            <div class="status-icon">
                <crrt:Icon type="STATUS_${status_type}"/>
            </div>
            <div class="status-title">
                <fmt:message key="email_conf.status_${status_type.toString().toLowerCase()}"/>
            </div>
            <div class="status-description">
                <c:out value="${requestScope.get('conf_res').get(0, lang)}" escapeXml="false"/>
            </div>
            <div><a href="login" class="mdx-hover-underline-animation"><fmt:message key="sign.return_to_login"/> </a></div>
        </div>
    </div>
</div>

<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/modules/argon/argon.min.js"></script>
</body>
</html>