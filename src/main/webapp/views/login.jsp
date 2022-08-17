<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
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
  <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">

  <!--  Custom  -->
  <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
  <link rel="stylesheet" href="${assets}css/globals.css">
  <link rel="stylesheet" href="${assets}css/inside.css">
  <link rel="stylesheet" href="${assets}css/mdx.css">
  <link rel="stylesheet" href="${assets}css/sign_rel.css">

  <link rel="stylesheet" href="${assets}css/media.css">
  <title>Login | CRRT.</title>

  <!--  Jquery  -->
  <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

  <!--  Custom  -->
  <script src="${assets}js/mdx.js"></script>
  <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
  <script src="${assets}js/global.js"></script>
  <script src="${assets}js/login.js" defer></script>

  <script>
    const js_localization = <%=JSJS.transForLoginPage((String) pageContext.getAttribute("lang"))%>;
  </script>
</head>
<body>

<div class="content">
  <div class="spinning-pattern">
    <img src="${assets}imgs/patterns/Spinning%20Road%20Pattern.svg" alt="carrent spinning road pattern">
  </div>
  <div class="spinning-pattern">
    <img src="${assets}imgs/patterns/Spinning%20Car%20Pattern.svg" alt="carrent spinning car pattern">
  </div>

  <div class="pos-wrapper row">
    <div class="sign-phrase-container col-lg-3">
      <div class="logo">
        <a href="index.html"><img src="${assets}imgs/CRRT.svg" alt="carrent crrt logo"></a>
      </div>
      <div class="phrase">
        <h1>98%</h1>
        <h5><fmt:message key="pages.sign.phrase" /></h5>
        <h6>- crrt.com</h6>
      </div>
    </div>
    <div class="sign-process-container col-lg-9" id="app">
      <h1><fmt:message key="page.login.title" /></h1>
      <h6><fmt:message key="page.login.desc" /></h6>
      <div class="sign-inputs-container">
        <div class="input-item" v-for="(input_i, key) in input_list" :key="key">
          <input :type="input_i.type" class="form-control" :placeholder="input_i.placeholder"
                 v-model.trim="input_i.inputData"
                 :data-validation-highlight="input_i.shouldHighlight"
                 @focus="input_i.isFocused = true"
                 @blur="input_i.isFocused = false"
                 :name="key"
          >
          <div class="input-tips" v-if="input_i.checks && input_i.checks.length > 0 && input_i.isFocused">
            <div v-for="(tip, index) in input_i.checks" :key="index"
                 :data-level="tip.configs.level"
                 :data-valid="tip.isValid">
              {{tip.message}}
            </div>
          </div>
        </div>
        <div class="input-item">
          <button class="mdx-md-button button-blue button-bordered"  data-ripple="#1890FF" @click="login()">
            <fmt:message key="page.login.button" />
          </button>
        </div>
        <div class="mdx-divider solid mt-4 mb-4"></div>
        <div class="sub-sign-info">
          <span><fmt:message key="page.login.dont_have_account_yet" /></span> <a href="register" class="mdx-hover-underline-animation"><fmt:message key="page.login.create_account_link" /></a>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
</body>
</html>