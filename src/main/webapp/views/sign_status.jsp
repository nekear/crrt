<%@ page import="com.github.DiachenkoMD.entities.enums.StatusStates" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>

<%@ include file="components/generals.jspf"%>

<!doctype html>
<html lang="${lang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <!-- Design libs -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${assets}modules/argon/argon.min.css">

    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/colorize.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/status_rel.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>Sign Status | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

    <!--  Custom  -->
    <script src="${assets}js/mdx.js"></script>
</head>
<body>

<div class="content">
    <div class="pos-wrapper">
        <div class="logo">
            <a href="index.html"><img src="${assets}imgs/CRRT.svg" alt="carrent crrt logo"></a>
        </div>
        <c:set var="status_type" value="${sessionScope.get('login_prg_message').getState(0)}" scope="page" />
        <div class="status-data" data-status="${status_type.toString().toLowerCase()}">
            <div class="status-icon">
                <crrt:Icon type="STATUS_${status_type}"/>
            </div>
            <div class="status-title">
                <fmt:message key="sign_up.status_${status_type.toString().toLowerCase()}"/>
            </div>
            <div class="status-description">
                <c:out value="${sessionScope.get('login_prg_message').get(0, lang)}" escapeXml="false"/>
            </div>
            <div><a href="login" class="mdx-hover-underline-animation"><fmt:message key="sign.return_to_login"/> </a></div>
        </div>
    </div>
</div>

<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="${assets}modules/argon/argon.min.js"></script>
</body>
</html>