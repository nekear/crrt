<%@ page import="com.github.DiachenkoMD.entities.dto.Car" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

<%@ include file="components/generals.jspf"%>

<%
   Map.Entry<Car, List<LocalDate>> rentData = (Map.Entry<Car, List<LocalDate>>) request.getAttribute("carData");

   Gson gson = (Gson) application.getAttribute("gson");

    String rentDataJson = null;
    if(rentData != null){
        rentDataJson = gson.toJson(
                Map.of(
                    "data", rentData.getKey(),
                    "disabledDates", rentData.getValue()
                )
        );
   }
%>

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
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">
    <link rel="stylesheet" href="${assets}modules/datepicker/hotel-datepicker.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/${requestScope.get("endTheme")}.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/inside.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/rent.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title><fmt:message key="title.rent"/> | CRRT.</title>

    <!--  JS libs  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/customParseFormat.min.js"></script>
    <script src="${assets}modules/datepicker/fecha.min.js"></script>
    <script src="${assets}modules/datepicker/hotel-datepicker.min.js"></script>
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <!--  Custom  -->
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/rent.js" defer></script>
    <script>
        const userBalance = ${user.balance};
        const carData = <%=rentDataJson%>;

        const js_localization = <%=JSJS.transForPassport((String) pageContext.getAttribute("lang"))%>;

        const datepickerI18n = <%=JSJS.transForDatepicker((String) pageContext.getAttribute("lang"))%>;

        const rentDatepickerI18n = {
            night: '<fmt:message key="days.1"/>',
            nights: '<fmt:message key="days.3"/>'
        }
    </script>
</head>
<body>
<div id="app">
    <%@ include file="components/header.jspf" %>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="rent-container">
                    <c:if test="${not empty requestScope.get('carData')}">
                        <div id="car-images-carousel" class="carousel slide" data-bs-ride="carousel" v-if="rent.images && rent.images.length">
                            <div class="carousel-inner">
                                <div class="carousel-item" v-for="(image, index) in rent.images" :class="{active: index === 0}">
                                    <div class="car-carousel-image cover-bg-type" :style="{backgroundImage: 'url(&quot;${imagesDir}/'+image.fileName+'&quot;)'}">
                                    </div>
                                </div>
                            </div>
                            <template v-if="rent.images.length > 1">
                                <button class="carousel-control-prev" type="button" data-bs-target="#car-images-carousel" data-bs-slide="prev">
                                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                                    <span class="visually-hidden">Previous</span>
                                </button>
                                <button class="carousel-control-next" type="button" data-bs-target="#car-images-carousel" data-bs-slide="next">
                                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                                    <span class="visually-hidden">Next</span>
                                </button>
                            </template>
                        </div>
                        <div class="rent-info">
                            <div class="car-title">{{rent.brand}} {{rent.model}}</div>
                            <div class="mdx-divider mb-4 solid arrow-down"></div>
                            <div class="mc-item"><fmt:message key="page.rent.city"/>: <span class="flat-chip status-chip" :data-status-code="rent.city % 4 + 2">{{cities[rent.city].name}}</span></div>
                            <div class="mc-item"><fmt:message key="page.rent.segment"/>: <span class="flat-chip status-chip" :data-status-code="rent.segment % 5">{{segments[rent.segment].name}}</span></div>
                            <div class="mc-item"><fmt:message key="page.rent.price"/>: <span class="flat-chip price-chip" :data-price-code="getPriceLevel(rent.price)">{{rent.price}} $</span></div>
                            <div class="mdx-divider mb-4"></div>
                            <div class="mc-part-title"><fmt:message key="page.rent.rent_config"/>:</div>
                            <div class="mc-item row">
                                <div class="col-4">
                                    <input class="form-control" placeholder="<fmt:message key='page.rent.date_picker.placeholder'/>" id="rent-range-datepicker" type="text" @change="setRantingDates">
                                </div>
                            </div>
                            <div class="mc-item">
                                <div class="with-driver-checkbox" :class="{disabled: !clientData.datesRange}">
                                    <input type="checkbox" id="wdc" class="mdx-checkbox-toggle" style="display:none" @change="setWithDriverStatus">
                                    <label for="wdc" class="mdx-toggle">
                                        <span>
                                          <svg width="10px" height="10px" viewBox="0 0 10 10">
                                            <path d="M5,1 L5,1 C2.790861,1 1,2.790861 1,5 L1,5 C1,7.209139 2.790861,9 5,9 L5,9 C7.209139,9 9,7.209139 9,5 L9,5 C9,2.790861 7.209139,1 5,1 L5,9 L5,1 Z"></path>
                                          </svg>
                                        </span>
                                    </label>
                                    <span class="wdc-text"><fmt:message key="page.rent.with_driver"/></span>
                                </div>
                                <div class="micro-caution-alert mt-2" v-if="!clientData.datesRange"><fmt:message key="page.rent.with_driver.no_dates_selected"/></div>
                            </div>
                            <div class="mdx-divider mb-4"></div>
                            <div class="mc-part-title"><fmt:message key="pages.passport.title"/>:</div>
                            <div class="mc-item">
                                <div class="row">
                                    <div class="col-4 mb-3" v-for="(input_i, key) in clientData.input_list">
                                        <div class="input-item form-floating" :key="key" :class="{highlight_necessity: input_i.isNecessary && input_i.isFocused}">
                                            <input :type="input_i.type" class="form-control"
                                                   v-model.trim="input_i.inputData"
                                                   :data-validation-highlight="input_i.shouldHighlight"
                                                   @focus="input_i.isFocused = true"
                                                   @blur="input_i.isFocused = false"
                                                   :name="key"
                                                   :id="'input_'+key"
                                            >
                                            <label :for="'input_'+key">{{input_i.placeholder}}</label>
                                            <div class="input-tips" v-if="input_i.checks.length > 0 && input_i.isFocused">
                                                <div v-for="(tip, index) in input_i.checks" :key="index"
                                                     :data-level="tip.configs.level"
                                                     :data-valid="tip.isValid">
                                                    {{tip.message}}
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div class="mdx-divider mb-4"></div>
                            <div class="mc-item mr-5" v-if="finalPrice">
                                <div class="final-price" style="text-align: right">{{finalPrice}}$</div>
                                <div class="mt-3" style="text-align: right">
                                    <button class="mdx-flat-button button-green" :class="{disabled: user.balance < finalPrice}" style="display: inline-flex" @click="payInvoice()"><fmt:message key="page.rent.button.pay"/></button>
                                </div>
                                <div class="micro-caution-alert mt-2" style="text-align: right" v-if="user.balance < finalPrice"><fmt:message key="page.rent.with_driver.you_are_missing"/> {{finalPrice - user.balance}}$</div>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${not empty requestScope.carError}">
                        <div class='banner-alert danger'>
                            <div class='banner-icon'>
                                <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="24px" height="24px" viewBox="0 0 24 24" version="1.1">
                                    <g stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                                        <rect x="0" y="0" width="24" height="24" />
                                        <path d="M14.2928932,16.7071068 C13.9023689,16.3165825 13.9023689,15.6834175 14.2928932,15.2928932 C14.6834175,14.9023689 15.3165825,14.9023689 15.7071068,15.2928932 L19.7071068,19.2928932 C20.0976311,19.6834175 20.0976311,20.3165825 19.7071068,20.7071068 C19.3165825,21.0976311 18.6834175,21.0976311 18.2928932,20.7071068 L14.2928932,16.7071068 Z" fill="#000000" fill-rule="nonzero" opacity="0.3"/>
                                        <path d="M11,16 C13.7614237,16 16,13.7614237 16,11 C16,8.23857625 13.7614237,6 11,6 C8.23857625,6 6,8.23857625 6,11 C6,13.7614237 8.23857625,16 11,16 Z M11,18 C7.13400675,18 4,14.8659932 4,11 C4,7.13400675 7.13400675,4 11,4 C14.8659932,4 18,7.13400675 18,11 C18,14.8659932 14.8659932,18 11,18 Z" fill="#000000" fill-rule="nonzero"/>
                                    </g>
                                </svg>
                            </div>
                            <div class='banner-text'>
                                <div>${requestScope.carError}</div>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="components/footerLinks.jspf"%>
</body>
</html>