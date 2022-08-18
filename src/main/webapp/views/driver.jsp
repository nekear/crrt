<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

<%@ include file="components/generals.jspf"%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <!-- Design libs -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">
    <link rel="stylesheet" href="${assets}modules/loaders/loaders.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/inside.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/driver.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>Drivers | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/isBetween.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/customParseFormat.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/isSameOrAfter.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/isSameOrBefore.min.js"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/driver.js" defer></script>

    <script>
        const loaded = {
            "cities": <%=JSJS.CitiesList((String) pageContext.getAttribute("lang"))%>,
            "invoiceStatuses": <%=JSJS.InvoiceStatusesList((String) pageContext.getAttribute("lang"), true)%>,
        };

        const currentCityId = ${cityId};
    </script>
</head>
<body>
<div id="app">
    <%@include file="components/header.jspf"%>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container">
                    <div class="row">
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M13.0079 2.6L15.7079 7.2L21.0079 8.4C21.9079 8.6 22.3079 9.7 21.7079 10.4L18.1079 14.4L18.6079 19.8C18.7079 20.7 17.7079 21.4 16.9079 21L12.0079 18.8L7.10785 21C6.20785 21.4 5.30786 20.7 5.40786 19.8L5.90786 14.4L2.30785 10.4C1.70785 9.7 2.00786 8.6 3.00786 8.4L8.30785 7.2L11.0079 2.6C11.3079 1.8 12.5079 1.8 13.0079 2.6Z" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title">
                                {{driver.upcomingRents}}
                            </div>
                            <div class="stat-car-description">
                                <div>Upcoming rents</div>
                                <div class="micro-caution">Calculated from {{getCurrentDate}}</div>
                            </div>
                        </div>
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M11 2.375L2 9.575V20.575C2 21.175 2.4 21.575 3 21.575H9C9.6 21.575 10 21.175 10 20.575V14.575C10 13.975 10.4 13.575 11 13.575H13C13.6 13.575 14 13.975 14 14.575V20.575C14 21.175 14.4 21.575 15 21.575H21C21.6 21.575 22 21.175 22 20.575V9.575L13 2.375C12.4 1.875 11.6 1.875 11 2.375Z" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title" v-if="currentCity >= 0">
                                {{cities[currentCity].name}}
                            </div>
                            <div class="stat-card-title" v-else>
                                <fmt:message key="driver.couldnt_load_city" />
                            </div>
                            <div class="stat-car-description">
                                <div>Current dislocation</div>
                                <div class="micro-caution">All rents targeted to cars in that city, <br> might be coupled with you</div>
                            </div>
                        </div>
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path opacity="0.3" d="M12.5 22C11.9 22 11.5 21.6 11.5 21V3C11.5 2.4 11.9 2 12.5 2C13.1 2 13.5 2.4 13.5 3V21C13.5 21.6 13.1 22 12.5 22Z" fill="currentColor"/>
                                    <path d="M17.8 14.7C17.8 15.5 17.6 16.3 17.2 16.9C16.8 17.6 16.2 18.1 15.3 18.4C14.5 18.8 13.5 19 12.4 19C11.1 19 10 18.7 9.10001 18.2C8.50001 17.8 8.00001 17.4 7.60001 16.7C7.20001 16.1 7 15.5 7 14.9C7 14.6 7.09999 14.3 7.29999 14C7.49999 13.8 7.80001 13.6 8.20001 13.6C8.50001 13.6 8.69999 13.7 8.89999 13.9C9.09999 14.1 9.29999 14.4 9.39999 14.7C9.59999 15.1 9.8 15.5 10 15.8C10.2 16.1 10.5 16.3 10.8 16.5C11.2 16.7 11.6 16.8 12.2 16.8C13 16.8 13.7 16.6 14.2 16.2C14.7 15.8 15 15.3 15 14.8C15 14.4 14.9 14 14.6 13.7C14.3 13.4 14 13.2 13.5 13.1C13.1 13 12.5 12.8 11.8 12.6C10.8 12.4 9.99999 12.1 9.39999 11.8C8.69999 11.5 8.19999 11.1 7.79999 10.6C7.39999 10.1 7.20001 9.39998 7.20001 8.59998C7.20001 7.89998 7.39999 7.19998 7.79999 6.59998C8.19999 5.99998 8.80001 5.60005 9.60001 5.30005C10.4 5.00005 11.3 4.80005 12.3 4.80005C13.1 4.80005 13.8 4.89998 14.5 5.09998C15.1 5.29998 15.6 5.60002 16 5.90002C16.4 6.20002 16.7 6.6 16.9 7C17.1 7.4 17.2 7.69998 17.2 8.09998C17.2 8.39998 17.1 8.7 16.9 9C16.7 9.3 16.4 9.40002 16 9.40002C15.7 9.40002 15.4 9.29995 15.3 9.19995C15.2 9.09995 15 8.80002 14.8 8.40002C14.6 7.90002 14.3 7.49995 13.9 7.19995C13.5 6.89995 13 6.80005 12.2 6.80005C11.5 6.80005 10.9 7.00005 10.5 7.30005C10.1 7.60005 9.79999 8.00002 9.79999 8.40002C9.79999 8.70002 9.9 8.89998 10 9.09998C10.1 9.29998 10.4 9.49998 10.6 9.59998C10.8 9.69998 11.1 9.90002 11.4 9.90002C11.7 10 12.1 10.1 12.7 10.3C13.5 10.5 14.2 10.7 14.8 10.9C15.4 11.1 15.9 11.4 16.4 11.7C16.8 12 17.2 12.4 17.4 12.9C17.6 13.4 17.8 14 17.8 14.7Z" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title">
                                {{driver.salaryThisMonth}}$
                            </div>
                            <div class="stat-car-description">
                                <div>Salary for that month</div>
                                <div class="micro-caution">From {{getMonthStart}} to {{getMonthEnd}}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="canvas-container">
                    <div class="mb-3">
                        <h5>Connected rents list</h5>
                        <h6>You are able to cancel a rent if system find another suitable driver for work.</h6>
                    </div>
                    <div class="mdx-divider solid">
                    </div>
                    <div class="mb-3 mt-3">
                        <h5>Search:</h5>
                        <input type="text" class="form-control" id="client-invoices-search" placeholder="Enter here vehicle name or date..." v-model="invoices.search.filters.value">
                    </div>
                    <table class="client-invoices-table table table-bordered" style="margin-bottom: 0 !important;">
                        <thead class="cit-header">
                        <tr>
                            <th>
                                Vehicle name
                            </th>
                            <th>
                                Renting dates
                            </th>
                            <th>
                                Salary
                            </th>
                            <th>
                                City
                            </th>
                            <th>
                                Statuses
                            </th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="(invoice, index) in invoices_list_paginated" :key="invoice.id">
                            <td>{{invoice.brand}} {{invoice.model}}</td>
                            <td>
                                <span class="flat-chip status-chip" data-status-code="1">{{invoice.datesRange.start}}</span>
                                <span style="margin-right: .5rem">[{{invoice.additions.daysBetween}}]</span>
                                <span class="flat-chip status-chip" data-status-code="2">{{invoice.datesRange.end}}</span>
                                <span v-if="invoice.additions.daysDiff">(in {{invoice.additions.daysDiff.days}} {{invoice.additions.daysDiff.suffix}})</span>
                                <span v-if="invoice.additions.isActive" class="active-invoice">(active)</span>
                            </td>
                            <td>
                                {{invoice.salary}} $
                            </td>
                            <td>
                                <span class='status-chip flat-chip' :data-status-code="invoice.city % 4 + 2">{{cities[invoice.city].name}}</span>
                            </td>
                            <td>
                                <template v-if="invoice.statusList.length">
                                    <span v-for="status in invoice.statusList" class='invoice-status-chip flat-chip' :data-status-code="status">{{invoiceStatuses[status].name}}</span>
                                </template>
                                <template v-else>
                                    <span class="micro-caution" style="opacity: .3">Empty...</span>
                                </template>
                            </td>
                            <td>
                                <button class="mdx-md-button button-red button-bordered button-w-icon" @click="skipInvoice(invoice.id)" v-if="invoice.additions.isAbleToSkip">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M13 17L18 12L13 7" stroke="black" stroke-opacity="0.6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        <path d="M6 17L11 12L6 7" stroke="black" stroke-opacity="0.6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    <span>Skip</span>
                                </button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class='banner-alert danger' v-if="!invoices_list.length">
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
                            <div>Nothing was found...</div>
                        </div>
                    </div>
                    <div>
                        <ul class="pagination mt-3">
                            <li class="page-item" v-for="n in invoices_pages"><a class="page-link" href="#" :class="{active: n == invoices.search.pagination.currentPage}" @click="goToInvoicesPage(n)">{{n}}</a></li>
                        </ul>
                    </div>
                </div>
                <div class="canvas-container">
                    <h5>Changing of current dislocation:</h5>
                    <div class="row">
                        <div class="col-4">
                            <select class="form-control form-select" v-model="selectedCity">
                                <option v-for="(city, index) in cities" :value="index" v-show="parseInt(index) !== 0">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                    <button class="mdx-md-button button-blue button-bordered mt-3" :disabled="parseInt(currentCity) === parseInt(selectedCity)" @click="changeDriverCity()">
                        Change
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="components/loader.jspf"%>
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
</body>
</html>