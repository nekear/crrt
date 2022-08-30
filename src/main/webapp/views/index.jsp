<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

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
    <link rel="stylesheet" href="${assets}modules/loaders/loaders.css">
    <link rel="stylesheet" href="https://unpkg.com/@vuepic/vue-datepicker@latest/dist/main.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/white_theme.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/main.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>CRRT.</title>

    <!--  JS libs  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>

    <!--  Custom  -->
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js"></script>
    <script src="${assets}js/main.js" defer></script>

    <script>
        const contextPath = '${imagesDir}';
        const loaded = {
            "cities": <%=JSJS.CitiesList((String) pageContext.getAttribute("lang"))%>,
            "segments": <%=JSJS.SegmentsList((String) pageContext.getAttribute("lang"))%>,
        };
        const isLoggedIn = ${not empty user};
    </script>
</head>
<body>
<div class="intro-wrapper">
    <header>
        <div class="logo">
            <img src="${assets}imgs/CRRT.svg" alt="carrent crrt logo">
        </div>
        <nav>
            <div class="lang-switcher">
                <div id="lang-switcher-dropdown" data-bs-toggle="dropdown" data-bs-auto-close="true" aria-expanded="false" style="border: none">
                    <img src="${assets}imgs/flags/${lang.equalsIgnoreCase(pageContext.servletContext.getInitParameter("enLocale")) ? 'england' : 'ukraine'}.svg" alt="flag">
                </div>
                <ul class="dropdown-menu" aria-labelledby="lang-switcher-dropdown">
                    <li><a class="dropdown-item" href="?lang=<crrt:lang prefix="uk" clean="true"/>">
                        <img src="${assets}imgs/flags/ukraine.svg" alt="flag">
                        <span>Українська</span>
                    </a></li>
                    <li><a class="dropdown-item" href="?lang=<crrt:lang prefix="en" clean="true"/>">
                        <img src="${assets}imgs/flags/england.svg" alt="flag">
                        <span>English</span>
                    </a></li>
                </ul>
            </div>

            <c:choose>
                <c:when test="${empty user}">
                    <a href="login" class="sign-button">
                        <div class="sign-icon">
                            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M16.3333 17V15.3333C16.3333 14.4493 15.9821 13.6014 15.357 12.9763C14.7319 12.3512 13.8841 12 13 12H6.33333C5.44928 12 4.60143 12.3512 3.97631 12.9763C3.35119 13.6014 3 14.4493 3 15.3333V17" stroke="black" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M9.66668 8.66667C11.5076 8.66667 13 7.17428 13 5.33333C13 3.49238 11.5076 2 9.66668 2C7.82573 2 6.33334 3.49238 6.33334 5.33333C6.33334 7.17428 7.82573 8.66667 9.66668 8.66667Z" stroke="black" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="sign-text">
                            <fmt:message key="page.main.header.button.login"/>
                        </div>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="profile" class="sign-button">
                        <div class="sign-icon">
                            <div class="avatar cover-bg-type" style="background-image: url('${userAvatarPath}')"></div>
                        </div>
                        <div class="sign-text">
                            <fmt:message key="page.main.header.button.profile"/>
                        </div>
                    </a>
                </c:otherwise>
            </c:choose>
        </nav>
    </header>

    <div class="car_preview">
        <img src="${assets}imgs/animated_preview.gif" alt="crrt car preview">
    </div>

    <div class="crosses-pattern left"></div>
    <div class="crosses-pattern right"></div>

    <div class="intro-content">
        <div class="centered-data">
            <h1 class="main-title"><fmt:message key="page.main.intro.title.p1"/> <div class="gradient-rotator-wrapper"><fmt:message key="page.main.intro.title.p2"/><div class="rotatable"></div></div>.</h1>
            <h3 class="additional-title"><strong>CARRENT</strong> - <fmt:message key="page.main.intro.subtitle"/> </h3>
            <div style="text-align: center; margin-top: 50px; margin-bottom: 100px">
                <a href="#" class="find_car-button">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 12H18" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M6 12H2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 6V2" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 22V18" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>

                    <span><fmt:message key="page.main.intro.button"/></span>
                </a>
            </div>
        </div>
    </div>
</div>
<div class="search-wrapper" id="app">
    <div class="search-container">
        <div class="search-blazing-panel">
            <div class="search-field">
                <input type="text" placeholder="<fmt:message key='page.main.search.input.placeholder'/>" v-model="offers.search.collector.inputValue" @keyup.enter="addSearchableTag()">
            </div>
            <div class="search-badges">
                <div class="search-badge" v-for="(tag, index) in offers.search.filters.tags">
                    <div class="title">
                        {{tag}}
                    </div>
                    <div class="close" @click="removeSearchTag(index)">
                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M13.5 4.5L4.5 13.5" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M4.5 4.5L13.5 13.5" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                </div>
            </div>
        </div>
        <h5 class="found-propositions-banner"><fmt:message key="page.main.search.found_res.p1"/> <strong>{{offers_list.length}}</strong> <fmt:message key="page.main.search.found_res.p2"/></h5>
        <div class="search-content row">
            <div class="search-results col-lg-9">
                <table class="table table-bordered" :class="{'table-hover': isLoggedIn}" style="margin-bottom: 0 !important;">
                    <thead>
                        <tr>
                            <th style="position: relative">
                                <fmt:message key="page.main.search.vehicle_name"/>
                                <Sorter name="carName" :order-by="offers.search.filters.orderBy"></Sorter>
                            </th>
                            <th style="position: relative">
                                <fmt:message key="page.main.search.segment"/>
                                <Sorter name="segment" :order-by="offers.search.filters.orderBy"></Sorter>
                            </th>
                            <th style="position: relative">
                                <fmt:message key="page.main.search.price"/>
                                <Sorter name="price" :order-by="offers.search.filters.orderBy"></Sorter>
                            </th>
                            <th><fmt:message key="page.main.search.city"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="offer in offers_list_paginated" @click="redirectToRent(offer.id)" :style="{cursor: isLoggedIn ? 'pointer' : 'auto'}">
                            <td>{{offer.brand}} {{offer.model}}</td>
                            <td><span class="flat-chip status-chip" :data-status-code="offer.segment % 5">{{segments[offer.segment].name}}</span></td>
                            <td><span class="flat-chip price-chip" :data-price-code="getPriceLevel(offer.price)">{{offer.price}} $</span></td>
                            <td>{{cities[offer.city].name}}</td>
                        </tr>
                    </tbody>
                </table>
                <div class='banner-alert danger' v-if="!offers_list_paginated.length">
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
                        <div><fmt:message key="page.main.search.banner.nothing_found"/></div>
                    </div>
                </div>
                <div style="margin-top: 1rem">
                    <ul class="pagination">
                        <li class="page-item" v-for="n in offers_pages"><a class="page-link" href="#" :class="{active: n === offers.search.pagination.currentPage}" @click.prevent="goToOffersPage(n)">{{n}}</a></li>
                    </ul>
                </div>
                <div class="alert alert-primary" role="alert" v-if="!isLoggedIn">
                    <svg width="24" height="21" viewBox="0 0 24 21" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 1rem; margin-top: -4px">
                        <path d="M9.90306 2.02134L1.43306 16.1613C1.25843 16.4638 1.16602 16.8066 1.16505 17.1559C1.16407 17.5051 1.25455 17.8485 1.42748 18.1519C1.60042 18.4553 1.84978 18.7081 2.15077 18.8852C2.45175 19.0623 2.79386 19.1575 3.14306 19.1613H20.0831C20.4323 19.1575 20.7744 19.0623 21.0753 18.8852C21.3763 18.7081 21.6257 18.4553 21.7986 18.1519C21.9716 17.8485 22.062 17.5051 22.0611 17.1559C22.0601 16.8066 21.9677 16.4638 21.7931 16.1613L13.3231 2.02134C13.1448 1.72744 12.8938 1.48446 12.5943 1.31582C12.2947 1.14719 11.9568 1.05859 11.6131 1.05859C11.2693 1.05859 10.9314 1.14719 10.6319 1.31582C10.3323 1.48446 10.0813 1.72744 9.90306 2.02134V2.02134Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <fmt:message key="page.main.search.should_be_logged_in.p1"/> <a href="login" style="text-decoration: none" class="mdx-hover-underline-animation"><fmt:message key="page.main.search.should_be_logged_in.p2"/></a> <fmt:message key="page.main.search.should_be_logged_in.p3"/>.
                </div>
            </div>
            <div class="search-filters col-lg-3">
                <div class="filters-sidebar">
                    <div class="filter-item">
                        <div class="filter-title">
                            <fmt:message key="page.main.filters.segment"/>
                        </div>
                        <div class="filter-content">
                            <select class="form-control form-select" v-model="offers.search.collector.segment">
                                <option v-for="(segment, index) in segments" :value="index">{{segment.name}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="filter-item">
                        <div class="filter-title">
                            <fmt:message key="page.main.filters.place_of_delivery"/>
                        </div>
                        <div class="filter-content">
                            <select class="form-control form-select" v-model="offers.search.collector.city">
                                <option v-for="(city, index) in cities" :value="index">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                    <div class="filter-item rent-dates-filter">
                        <div class="filter-title">
                            <fmt:message key="page.main.filters.renting_dates"/>
                        </div>
                        <div class="filter-content">
                            <Datepicker v-model="offers.search.collector.datesRange"
                                        range
                                        :format="format"
                                        :preview-format="format"
                                        :enable-time-picker="false"
                                        hide-input-icon
                                        input-class-name="form-control"
                                        locale="<crrt:lang/>"
                                        auto-apply
                                        :min-date="new Date()"
                                        placeholder="<fmt:message key='page.main.filters.dates_range'/>"/>
                        </div>
                    </div>
                    <div class="filter-item">
                        <div class="filter-title">
                            <fmt:message key="page.main.filters.price_filter"/>
                        </div>
                        <div class="filter-content row">
                            <div class="col-lg-6">
                                <input type="number" class="form-control" placeholder="<fmt:message key='page.main.filters.price_filter.min'/>" v-model="offers.search.collector.pricesRange.min">
                            </div>
                            <div class="col-lg-6">
                                <input type="number" class="form-control" placeholder="<fmt:message key='page.main.filters.price_filter.max'/>" v-model="offers.search.collector.pricesRange.max">
                            </div>
                        </div>
                    </div>
                    <div class="filter-item">
                        <div class="filter-title">
                            <fmt:message key='page.main.filters.elements_per_page'/>
                        </div>
                        <div class="filter-content">
                            <select class="form-control form-select" v-model="offers.search.collector.itemsPerPage">
                                <option value="1">1</option>
                                <option value="15">15</option>
                                <option value="50">50</option>
                                <option value="100">100</option>
                            </select>
                        </div>
                    </div>
                    <div class="filter-item" style="text-align: center">
                        <button class="mdx-md-button button-bordered button-blue" @click="applyFilters()">
                            <fmt:message key="page.main.filters.button.apply"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<footer>
    <div class="left-content">
        <div class="logo">
            <img src="${assets}imgs/CRRT.svg" alt="CRRT logo carrent logo">
        </div>
        <div class="description">
            <fmt:message key="page.main.footer.credits"/>
        </div>
        <div class="date">
            2022
        </div>
    </div>
    <div class="right-content">
        <div class="copy-item">
            <div class="title">
                <fmt:message key="page.main.footer.hotline"/>
            </div>
            <div class="content">
                <div class="text">
                    +380123456789
                </div>
                <div class="copy-icon">
                    <img src="${assets}imgs/icons/bi_clipboard.svg" alt="clipboard crrt icon carrent">
                </div>
            </div>
            <div class="micro-caution" style="opacity: 0; transition: .25s"><fmt:message key="page.main.footer.caution.ctc"/></div>
        </div>
        <div class="copy-item">
            <div class="title">
                <fmt:message key="page.main.footer.support"/>
            </div>
            <div class="content">
                <div class="text">
                    support@crrt.ua
                </div>
                <div class="copy-icon">
                    <img src="${assets}imgs/icons/bi_clipboard.svg" alt="clipboard crrt icon carrent">
                </div>
            </div>
            <div class="micro-caution" style="opacity: 0; transition: .25s"><fmt:message key="page.main.footer.caution.ctc"/></div>
        </div>
    </div>
</footer>
<%@include file="components/loader.jspf"%>
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
<script src="https://unpkg.com/@vuepic/vue-datepicker@latest"></script>
</body>
</html>