<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>

<%@ include file="components/generals.jspf"%>

<!doctype html>
<html lang="<crrt:lang/>">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <%@include file="components/favicon.jspf" %>

  <!-- Design libs -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

  <!--  Custom  -->
  <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
  <link rel="stylesheet" href="${assets}css/globals.css">
  <link rel="stylesheet" href="${assets}css/inside.css">
  <link rel="stylesheet" href="${assets}css/mdx.css">
  <link rel="stylesheet" href="${assets}css/sign_rel.css">

  <link rel="stylesheet" href="${assets}css/media.css">
  <title><fmt:message key="title.register"/> | CRRT.</title>

  <!--  JS libs  -->
  <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
  <script src="https://www.google.com/recaptcha/api.js?hl=<crrt:lang/>&onload=recaptchaLoadCallback" async defer></script>

  <!--  Custom  -->
  <script src="${assets}js/mdx.js"></script>
  <script src="${assets}js/global.js" defer></script>
  <script src="${assets}js/register.js" defer></script>

  <script>
    const js_localization = <%=JSJS.transForRegisterPage((String) pageContext.getAttribute("lang"))%>;
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
        <a href="${pageContext.request.contextPath}/"><img src="${assets}imgs/CRRT.svg" alt="carrent crrt logo"></a>
      </div>
      <div class="phrase">
        <h1>98%</h1>
        <h5><fmt:message key="pages.sign.phrase"/></h5>
        <h6>- crrt.com</h6>
      </div>
    </div>
    <div class="sign-process-container col-lg-9" id="app">
      <h1><fmt:message key="page.register.title" /></h1>
      <h6><fmt:message key="page.register.desc" /></h6>
      <form method="POST" class="sign-inputs-container" ref="registrationForm" action="register">
        <div class="input-item" v-for="(input_i, key) in input_list" :key="key" :class="{necessary_input: input_i.isNecessary, highlight_necessity: input_i.isNecessary && input_i.isFocused, 'mdx-password-wrap': key === 'password'}">
          <input :type="input_i.type" class="form-control" :placeholder="input_i.placeholder"
                 v-model.trim="input_i.inputData"
                 :data-validation-highlight="input_i.shouldHighlight"
                 @focus="input_i.isFocused = true"
                 @blur="input_i.isFocused = false"
                 :name="key"
          >
          <div class="material-icons mdx-password-show" data-password-status="hidden" v-if="key === 'password' ">visibility_off</div>
          <div class="input-tips" v-if="input_i.checks.length > 0 && input_i.isFocused">
            <div v-for="(tip, index) in input_i.checks" :key="index"
                 :data-level="tip.configs.level"
                 :data-valid="tip.isValid">
              {{tip.message}}
            </div>
          </div>
        </div>
        <div class="input-item">
          <div id="recaptchaEl" :class="{'disabled-captcha': !showRecaptcha}"></div>
        </div>
        <div class="input-item agree_with_terms_container">
          <input class="input-mdx-square-checkbox" id="agree_with_terms" type="checkbox" style="display: none" v-model="doesAgreeWithTerms"/>
          <label class="mdx-square-checkbox" for="agree_with_terms">
              <span>
                  <svg width="12px" height="10px" viewbox="0 0 12 10">
                    <polyline points="1.5 6 4.5 9 10.5 1"></polyline>
                </svg>
              </span>
            <span><fmt:message key="page.register.agree_with_terms.0" /> <a href="#" class="mdx-hover-underline-animation"><fmt:message key="page.register.agree_with_terms.1" /></a>.</span>
          </label>
        </div>
        <div class="input-item">
          <button class="mdx-md-button button-blue button-bordered" data-ripple="#1890FF" :disabled="!doesAgreeWithTerms || !isRecaptchaSubmitted" @click.prevent="registerSubmit()">
            <fmt:message key="page.register.button" />
          </button>
        </div>
        <div class="mdx-divider solid mt-4 mb-4"></div>
        <div class="sub-sign-info">
          <span><fmt:message key="page.register.already_have_account" /></span> <a href="login" class="mdx-hover-underline-animation"><fmt:message key="page.register.login_link" /></a>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
</body>
</html>