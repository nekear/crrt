<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>

<%@ include file="../components/generals.jspf"%>

<!doctype html>
<html lang="<crrt:lang/>">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <%@include file="../components/favicon.jspf" %>

  <!-- Design libs -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

  <!--  Custom  -->
  <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
  <link rel="stylesheet" href="${assets}css/globals.css">
  <link rel="stylesheet" href="${assets}css/inside.css">
  <link rel="stylesheet" href="${assets}css/mdx.css">
  <link rel="stylesheet" href="${assets}css/restore.css">

  <link rel="stylesheet" href="${assets}css/media.css">
  <title><fmt:message key="title.restore"/> | CRRT.</title>

  <!--  JS libs  -->
  <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

  <!--  Custom  -->
  <script src="${assets}js/global.js" defer></script>
  <script src="${assets}js/mdx.js"></script>
  <script src="${assets}js/restore/new_password_step.js" defer></script>
  <script>
    const jwtToken = '${param.token}';
    const js_localization = {
      inputs:{
        password: {
          placeholder: '<fmt:message key="pages.sign.input.password.placeholder"/>',
          checks: [
            "<fmt:message key="pages.sign.input.password.checks.0"/>",
            "<fmt:message key="pages.sign.input.password.checks.1"/>"
          ]
        }
      }
    }
  </script>
</head>
<body>

<div class="content" id="app">
  <div class="restore-form">
    <div class="logo">
      <img src="${assets}imgs/CRRT.svg" alt="crrt logo">
    </div>
    <div class="title">
      <fmt:message key="page.restore.title"/>
    </div>
    <div class="subtitle">
      <fmt:message key="page.restore.new_password_step.subtitle"/> <span style="color: var(--systemIndigo_accessible)">${requestScope.targetEmail}</span>.
    </div>
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
      <template v-if="restorePassword != null">
        <div class="mt-1" :class="{'micro-caution-success': restorePassword.status === true, 'micro-caution-alert': restorePassword.status === false}">
          {{restorePassword.message}}
        </div>
      </template>
    </div>
    <button class="mdx-md-button button-blue button-bordered" :disabled="!allowRestoration" @click="performRestore"><fmt:message key="page.restore.new_password_step.button"/></button>
    <div class="micro-caution mt-2"><fmt:message key="page.restore.caution.0"/> <a href="login" class="mdx-hover-underline-animation"><fmt:message key="page.restore.caution.1"/></a> <fmt:message key="page.restore.caution.2"/>.</div>
  </div>
</div>
<%@include file="../components/loader.jspf"%>
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
</body>
</html>