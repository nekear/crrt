<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

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
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">

    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/inside.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/profile.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>Profile | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/global.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/profile.js" defer></script>

    <script>
        const incomingUserData = {
            firstname: "${user.firstname}",
            surname: "${user.surname}",
            patronymic: "${user.patronymic}"
        };
        const userBalance = ${user.balance};
        const avatar = "${userAvatarPath}";
    </script>
</head>
<body>

<div id="app">
    <%@include file="components/header.jspf" %>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container profile-canvas mb-5 row">
                    <div class="col-lg-8 col-md-12 col-xs-12">
                        <div class="profile-canvas-info mb-4">
                            <h5><fmt:message key="page.profile.personal_data.title" /></h5>
                            <h6><fmt:message key="page.profile.personal_data.desc" /></h6>
                        </div>
                        <div class="profile-canvas-item">
                            <h5><fmt:message key="page.profile.avatar.title" /></h5>
                            <form action="#" class="avatar-add-photo-form">
                                <div class="profile-avatar cover-bg-type" :style="avatar">
                                </div>
                                <div class="profile-avatar-settings">
                                    <div class="pas-buttons">
                                        <input type="file" style="display: none" class="avatar-add-photo-input" name="avatar" id="avatar-file-input">
                                        <button class="mdx-md-button button-bordered button-blue avatar-add-photo-button">
                                            <fmt:message key="page.profile.avatar.change_button" />
                                        </button>
                                        <div class="mdx-md-button button-bordered button-reversed delete-avatar-button ml-2" @click="deleteAvatar()"><fmt:message key="page.profile.avatar.delete_button" /></div>
                                    </div>
                                    <div class="pas-description">
                                        <fmt:message key="page.profile.avatar.upload_info" />
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="profile-canvas-item row">
                            <div class="col-lg-4 col-md-4 col-xs-12">
                                <label for="i-firstname" class="mb-2"><fmt:message key="pages.sign.input.firstname.placeholder" /></label>
                                <input type="text" id="i-firstname" class="form-control" placeholder="${i18n.getString("pages.sign.input.firstname.placeholder")}" v-model="userDataCurrent.firstname">
                            </div>
                            <div class="col-lg-4 col-md-4 col-xs-12">
                                <label for="i-surname" class="mb-2"><fmt:message key="pages.sign.input.surname.placeholder" /></label>
                                <input type="text" id="i-surname" class="form-control" placeholder="${i18n.getString("pages.sign.input.surname.placeholder")}" v-model="userDataCurrent.surname">
                            </div>
                            <div class="col-lg-4 col-md-4 col-xs-12">
                                <label for="i-patronymic" class="mb-2"><fmt:message key="pages.sign.input.patronymic.placeholder" /></label>
                                <input type="text" id="i-patronymic" class="form-control" placeholder="${i18n.getString("pages.sign.input.patronymic.placeholder")}" v-model="userDataCurrent.patronymic">
                            </div>
                        </div>
                        <div class="profile-canvas-item">
                            <button class="mdx-flat-button button-blue" :class="{disabled: wasUserDataChanged}" @click="saveUserData()"><fmt:message key="pages.buttons.save" /></button>
                        </div>
                    </div>
                </div>
                <div class="canvas-container profile-canvas mb-5 row">
                    <div class="col-lg-8 col-md-12 col-xs-12">
                        <div class="profile-canvas-info mb-4">
                            <h5><fmt:message key="page.profile.password.title" /></h5>
                        </div>
                        <div class="row">
                            <div class="col-lg-8 col-md-10 col-xs-12 mb-3">
                                <input type="password" id="i-old_password" class="form-control" placeholder="${i18n.getString("page.profile.password.old_pass")}" v-model="passwords.old">
                            </div>
                            <div class="col-lg-8 col-md-10 col-xs-12 mb-3 mdx-password-wrap">
                                <input type="password" id="i-new_password" class="form-control" placeholder="${i18n.getString("page.profile.password.new_pass")}" v-model="passwords.new">
                                <div class="material-icons mdx-password-show" data-password-status="hidden">visibility_off</div>
                            </div>
                        </div>
                        <div>
                            <button class="mdx-flat-button button-blue" :class="{disabled: isUpdatePasswordButtonBlocked}" @click="changePasswordAction()"><fmt:message key="pages.buttons.change_password" /></button>
                        </div>
                    </div>
                </div>
                <div class="canvas-container profile-canvas mb-5 row">
                    <div class="col-lg-8 col-md-12 col-xs-12">
                        <div class="profile-canvas-info mb-4">
                            <h5><fmt:message key="page.profile.balance.title" /></h5>
                        </div>
                        <div class="money-replenishment mb-3">
                            <input type="number" class="form-control" v-model="balanceReplenishmentNumber">
                        </div>
                        <div>
                            <button class="mdx-flat-button button-blue" :class="{disabled: balanceReplenishmentNumber <= 0}" @click="replenishBalance()"><fmt:message key="pages.buttons.replenish" /></button>
                        </div>
                    </div>
                </div>
                <button class="mdx-flat-button button-pink mb-5" @click="exitAccount"><fmt:message key="pages.buttons.exit.long" /></button>
            </div>
        </div>
    </div> <!-- #app -->
</div>
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
</body>
</html>