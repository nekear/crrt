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
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">
    <link rel="stylesheet" href="${assets}modules/loaders/loaders.css">
    <link rel="stylesheet" href="https://unpkg.com/@vuepic/vue-datepicker@latest/dist/main.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/${requestScope.get("endTheme")}.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/inside.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/admin.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title><fmt:message key="title.admin"/> | CRRT.</title>

    <!--  JS libs  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/admin.js" defer></script>

    <script>
        const contextPath = '${imagesDir}';
        const loaded = {
            "segments": <%=JSJS.SegmentsList((String) pageContext.getAttribute("lang"))%>,
            "cities": <%=JSJS.CitiesList((String) pageContext.getAttribute("lang"))%>,
            "roles": <%=JSJS.RolesList((String) pageContext.getAttribute("lang"))%>,
            "accountStates": <%=JSJS.AccountStatesList((String) pageContext.getAttribute("lang"))%>,
            "invoiceStatuses": <%=JSJS.InvoiceStatusesList((String) pageContext.getAttribute("lang"), false)%>,
        };

        const js_localization = <%=JSJS.transForRegisterPage((String) pageContext.getAttribute("lang"))%>;
        const users_delete_conf_localization = <%=JSJS.transForUsersDeleteConfirmation((String) pageContext.getAttribute("lang"))%>;
    </script>
</head>
<body>
<div id="app">
    <%@include file="components/header.jspf"%>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container unstyled greetings-banner">
                    <h3><fmt:message key="page.admin.welcome" />, <strong><l:prettyLogin /></strong></h3>
                </div>
                <div class="canvas-container">
                    <div class="row">
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path opacity="0.3" d="M21 11H18.9C18.5 7.9 16 5.49998 13 5.09998V3C13 2.4 12.6 2 12 2C11.4 2 11 2.4 11 3V5.09998C7.9 5.49998 5.50001 8 5.10001 11H3C2.4 11 2 11.4 2 12C2 12.6 2.4 13 3 13H5.10001C5.50001 16.1 8 18.4999 11 18.8999V21C11 21.6 11.4 22 12 22C12.6 22 13 21.6 13 21V18.8999C16.1 18.4999 18.5 16 18.9 13H21C21.6 13 22 12.6 22 12C22 11.4 21.6 11 21 11ZM12 17C9.2 17 7 14.8 7 12C7 9.2 9.2 7 12 7C14.8 7 17 9.2 17 12C17 14.8 14.8 17 12 17Z" fill="currentColor"/>
                                    <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title">
                                {{stats[0]}}
                            </div>
                            <div class="stat-car-description">
                                <div><fmt:message key="page.admin.stats.stat1.title" /> </div>
                                <div class="micro-caution"><fmt:message key="page.admin.stats.stat1.subtitle"/> {{getMonthStart}}</div>
                            </div>
                        </div>
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M13 5.91517C15.8 6.41517 18 8.81519 18 11.8152C18 12.5152 17.9 13.2152 17.6 13.9152L20.1 15.3152C20.6 15.6152 21.4 15.4152 21.6 14.8152C21.9 13.9152 22.1 12.9152 22.1 11.8152C22.1 7.01519 18.8 3.11521 14.3 2.01521C13.7 1.91521 13.1 2.31521 13.1 3.01521V5.91517H13Z" fill="currentColor"/>
                                    <path opacity="0.3" d="M19.1 17.0152C19.7 17.3152 19.8 18.1152 19.3 18.5152C17.5 20.5152 14.9 21.7152 12 21.7152C9.1 21.7152 6.50001 20.5152 4.70001 18.5152C4.30001 18.0152 4.39999 17.3152 4.89999 17.0152L7.39999 15.6152C8.49999 16.9152 10.2 17.8152 12 17.8152C13.8 17.8152 15.5 17.0152 16.6 15.6152L19.1 17.0152ZM6.39999 13.9151C6.19999 13.2151 6 12.5152 6 11.8152C6 8.81517 8.2 6.41515 11 5.91515V3.01519C11 2.41519 10.4 1.91519 9.79999 2.01519C5.29999 3.01519 2 7.01517 2 11.8152C2 12.8152 2.2 13.8152 2.5 14.8152C2.7 15.4152 3.4 15.7152 4 15.3152L6.39999 13.9151Z" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title">
                                {{stats[1]}}
                            </div>
                            <div class="stat-car-description">
                                <div><fmt:message key="page.admin.stats.stat2.title" /> </div>
                                <div class="micro-caution"><fmt:message key="page.admin.stats.stat2.subtitle" /> {{getMonthStart}}</div>
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
                                {{stats[2]}} $
                            </div>
                            <div class="stat-car-description">
                                <div><fmt:message key="page.admin.stats.stat3.title" /></div>
                                <div class="micro-caution"><fmt:message key="page.admin.stats.stat3.subtitle" /></div>
                            </div>
                        </div>
                    </div>
                    <div style="text-align: center">
                        <button class="mdx-md-button button-bordered button-indigo button-w-icon rotating-icon mt-4" data-ripple="#7D7AFFFF">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M14.5 20.7259C14.6 21.2259 14.2 21.826 13.7 21.926C13.2 22.026 12.6 22.0259 12.1 22.0259C9.5 22.0259 6.9 21.0259 5 19.1259C1.4 15.5259 1.09998 9.72592 4.29998 5.82592L5.70001 7.22595C3.30001 10.3259 3.59999 14.8259 6.39999 17.7259C8.19999 19.5259 10.8 20.426 13.4 19.926C13.9 19.826 14.4 20.2259 14.5 20.7259ZM18.4 16.8259L19.8 18.2259C22.9 14.3259 22.7 8.52593 19 4.92593C16.7 2.62593 13.5 1.62594 10.3 2.12594C9.79998 2.22594 9.4 2.72595 9.5 3.22595C9.6 3.72595 10.1 4.12594 10.6 4.02594C13.1 3.62594 15.7 4.42595 17.6 6.22595C20.5 9.22595 20.7 13.7259 18.4 16.8259Z" fill="currentColor"/>
                                <path opacity="0.3" d="M2 3.62592H7C7.6 3.62592 8 4.02592 8 4.62592V9.62589L2 3.62592ZM16 14.4259V19.4259C16 20.0259 16.4 20.4259 17 20.4259H22L16 14.4259Z" fill="currentColor"/>
                            </svg>
                            <span @click="reloadStats()"><fmt:message key="page.admin.stats.button" /></span>
                        </button>
                    </div>
                </div>
                <div class="canvas-tabs">
                    <ul class="nav nav-pills">
                        <li class="nav-item">
                            <a class="nav-link" aria-current="page" href="#" :class="{active: tabs.panel.active === 'cars'}" @click.prevent="activateTab('panel','cars')"><fmt:message key="page.admin.tabs.cars" /></a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="#" :class="{active: tabs.panel.active === 'users'}" @click.prevent="activateTab('panel','users')"><fmt:message key="page.admin.tabs.users" /></a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="#" :class="{active: tabs.panel.active === 'invoices'}"@click.prevent="activateTab('panel','invoices')"><fmt:message key="page.admin.tabs.invoices" /></a>
                        </li>
                    </ul>
                </div>
                <div class="canvas-container" v-if="tabs.panel.active === 'cars'">
                    <div class="mb-3 canvas-tools">
                        <div>
                            <h5><fmt:message key="page.admin.categories.cars.title"/> </h5>
                            <h6><fmt:message key="page.admin.categories.cars.subtitle"/> <span>{{cars.list.length}}</span></h6>
                            <div>
                                <button class="mdx-md-button button-green button-bordered" @click="openCarCreateModal()"><fmt:message key="page.admin.categories.cars.button.create"/></button>
                            </div>
                        </div>
                        <div>
                            <label for="cars-pag-sel"><fmt:message key="page.admin.categories.cars.pagination.title" /></label>
                            <select class="form-control form-select" v-model="cars.pagination.itemsPerPage" id="cars-pag-sel">
                                <option value="1" selected>1</option>
                                <option value="15">15</option>
                                <option value="50">50</option>
                                <option value="100">100</option>
                            </select>
                        </div>
                    </div>
                    <table class="car-park-table table table-bordered">
                        <thead class="cpt-header">
                        <tr>
                            <th>
                                <input type="text" class="form-control" placeholder="<fmt:message key='pages.cars.brand'/>" v-model="cars.search.brand">
                            </th>
                            <th>
                                <input type="text" class="form-control" placeholder="<fmt:message key='pages.cars.model'/>" v-model="cars.search.model">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="cars.search.segment">
                                    <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                                </select>
                            </th>
                            <th>
                                <input type="number" class="form-control" placeholder="<fmt:message key='page.admin.categories.cars.min_price'/>" v-model="cars.search.price">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="cars.search.city">
                                    <option v-for="(city, index) in cities" :key="index" :value="index">{{city.name}}</option>
                                </select>
                            </th>
                            <th>

                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="car in cars_list_paginated" :key="car.id">
                            <td>{{car.brand}}</td>
                            <td>{{car.model}}</td>
                            <td>
                                <span class='status-chip flat-chip' data-ripple='#D70015FF' :data-status-code="car.segment % 5" @click="cars_sc_segment(car.segment)">{{segments[car.segment].name}}</span>
                            </td>
                            <td class="price-td">
                                {{car.price}}$
                            </td>
                            <td>
                                <span class='status-chip flat-chip' data-ripple='#76ccc2' :data-status-code="car.city % 4 + 2" @click="cars_sc_city(car.city)">{{cities[car.city].name}}</span>
                            </td>
                            <td>
                                <button class="mdx-md-button button-green button-bordered" @click="openCarEditModal(car.id)"><fmt:message key="page.admin.categories.cars.button.edit" /></button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div>
                        <ul class="pagination">
                            <li class="page-item" v-for="n in cars_pages"><a class="page-link" href="#" :class="{active: n == cars.pagination.currentPage}" @click="goToCarsPage(n)">{{n}}</a></li>
                        </ul>
                    </div>

                </div>
                <div class="canvas-container" v-if="tabs.panel.active === 'users'">
                    <div class="mb-3 canvas-tools">
                        <div>
                            <h5><fmt:message key="page.admin.categories.users.title" /></h5>
                            <h6><fmt:message key="page.admin.categories.users.subtitle" /> <span>{{users.search.pagination.totalFoundEntities}}</span></h6>
                            <div>
                                <button class="mdx-md-button button-green button-bordered" @click="openUserCreateModal()"><fmt:message key="page.admin.categories.users.button.create" /></button>
                                <button class="mdx-md-button button-red button-bordered ml-2" @click="deleteSelectedUsers()"><fmt:message key="page.admin.categories.users.button.delete_selected" /></button>
                            </div>
                        </div>
                        <div>
                            <div>
                                <label for="users-pag-sel"><fmt:message key="page.admin.categories.users.pagination.title"/></label>
                                <select class="form-control form-select" id="users-pag-sel" v-model="users.search.pagination.itemsPerPage">
                                    <option value="1">1</option>
                                    <option value="15">15</option>
                                    <option value="50">50</option>
                                    <option value="100">100</option>
                                </select>
                            </div>
                            <div class="mt-2">
                                <button class="mdx-md-button button-blue button-bordered" @click="performUsersSearch(1)"><fmt:message key="page.admin.categories.users.button.search"/></button>
                            </div>
                        </div>
                    </div>
                    <table class="users-panel-table table table-bordered">
                        <thead class="users-panel-header">
                        <tr>
                            <th class="admin-checkbox-cell">
                                <div class="table-cell-flex">
                                    <input class="input-mdx-square-checkbox" id="check-all-checkbox" type="checkbox" style="display: none" @change="usersCheckboxAll($event)"/>
                                    <label class="mdx-square-checkbox" for="check-all-checkbox">
                                        <span>
                                            <svg width="12px" height="10px" viewbox="0 0 12 10">
                                              <polyline points="1.5 6 4.5 9 10.5 1"></polyline>
                                            </svg>
                                        </span>
                                    </label>
                                </div>
                            </th>
                            <th>
                                <input type="email" id="search-email" placeholder="<fmt:message key='pages.input.email'/>" class="form-control" v-model="users.search.filters.email">
                            </th>
                            <th>
                                <input type="text" id="search-firstname" placeholder="<fmt:message key='pages.input.firstname'/>" class="form-control" v-model="users.search.filters.firstname">
                            </th>
                            <th>
                                <input type="text" id="search-surname" placeholder="<fmt:message key='pages.input.surname'/>" class="form-control" v-model="users.search.filters.surname">
                            </th>
                            <th>
                                <input type="text" id="search-patronymic" placeholder="<fmt:message key='pages.input.patronymic'/>" class="form-control" v-model="users.search.filters.patronymic">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="users.search.filters.role">
                                    <option v-for="(role, index) in roles" :key="index" :value="index">{{role.name}}</option>
                                </select>
                            </th>
                            <th>
                                <select id="select-access-status" class="form-control form-select" v-model="users.search.filters.state">
                                    <option v-for="(state, index) in accountStates" :key="index" :value="index">{{state.name}}</option>
                                </select>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr v-for="user in users.list" :class="{blocked: user.state === 1}">
                                <td class="admin-checkbox-cell">
                                    <div class="table-cell-flex">
                                        <input class="input-mdx-square-checkbox" :id="'user-checkbox-'+user.id" type="checkbox" style="display: none" v-model="user.isChecked">
                                        <label class="mdx-square-checkbox" :for="'user-checkbox-'+user.id">
                                                <span>
                                                    <svg width="12px" height="10px" viewBox="0 0 12 10">
                                                      <polyline points="1.5 6 4.5 9 10.5 1"></polyline>
                                                    </svg>
                                                </span>
                                        </label>
                                    </div>
                                </td>
                                <td class="column-email">{{user.email}}</td>
                                <td>{{user.firstname}}</td>
                                <td>{{user.surname}}</td>
                                <td>{{user.patronymic}}</td>
                                <td><span class="flat-chip status-chip" data-ripple="#76ccc2" :data-status-code="user.role % 4 + 1" @click="users_sc_role(user.role)">{{roles[user.role].name}}</span></td>
                                <td>
                                    <button class="mdx-md-button button-bordered button-green" @click="openUserEditModal(user.id)"><fmt:message key='page.admin.categories.users.button.details'/></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div>
                        <ul class="pagination">
                            <li class="page-item" v-for="n in users.search.pagination.availablePages"><a class="page-link" href="#" :class="{active: n == users.search.pagination.currentPage}" @click="goToUsersPage(n)">{{n}}</a></li>
                        </ul>
                    </div>
                </div>
                <div class="canvas-container" v-if="tabs.panel.active === 'invoices'">
                    <div class="mb-3 canvas-tools">
                        <div>
                            <h5><fmt:message key="page.manager.categories.invoices.title"/></h5>
                            <h6><fmt:message key="page.manager.categories.invoices.subtitle"/>: <span>{{invoices.search.pagination.totalFoundEntities}}</span></h6>
                            <div>
                                <button class="mdx-md-button button-blue button-bordered" @click="performInvoicesSearch(1)">
                                    <fmt:message key="page.manager.categories.invoices.button.search"/>
                                </button>
                                <button class="mdx-md-button button-green button-bordered button-w-icon ml-3" @click="generateInvoicesReport()">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M2.85902 2.87697L15.429 1.08197C15.5 1.07179 15.5723 1.07699 15.641 1.0972C15.7098 1.11741 15.7734 1.15216 15.8275 1.1991C15.8817 1.24605 15.9251 1.30408 15.9549 1.36928C15.9846 1.43447 16 1.5053 16 1.57697V22.423C16 22.4945 15.9846 22.5653 15.9549 22.6304C15.9252 22.6955 15.8819 22.7535 15.8279 22.8004C15.7738 22.8473 15.7103 22.8821 15.6417 22.9024C15.5731 22.9227 15.5009 22.928 15.43 22.918L2.85802 21.123C2.61964 21.089 2.40152 20.9702 2.24371 20.7883C2.08591 20.6065 1.99903 20.3738 1.99902 20.133V3.86697C1.99903 3.62618 2.08591 3.39348 2.24371 3.21161C2.40152 3.02975 2.61964 2.91092 2.85802 2.87697H2.85902ZM4.00002 4.73497V19.265L14 20.694V3.30597L4.00002 4.73497ZM17 19H20V4.99997H17V2.99997H21C21.2652 2.99997 21.5196 3.10533 21.7071 3.29286C21.8947 3.4804 22 3.73475 22 3.99997V20C22 20.2652 21.8947 20.5195 21.7071 20.7071C21.5196 20.8946 21.2652 21 21 21H17V19ZM10.2 12L13 16H10.6L9.00002 13.714L7.40002 16H5.00002L7.80002 12L5.00002 7.99997H7.40002L9.00002 10.286L10.6 7.99997H13L10.2 12Z" fill="var(--systemGreen_default)"/>
                                    </svg>
                                    <span><fmt:message key="page.manager.categories.invoices.button.convert_to_excel"/></span>
                                </button>
                            </div>
                        </div>
                        <div>
                            <div>
                                <label for="invoices-pag-sel"><fmt:message key="page.manager.categories.invoices.pagination.title"/></label>
                                <select class="form-control form-select" id="invoices-pag-sel" v-model="invoices.search.pagination.itemsPerPage">
                                    <option value="1">1</option>
                                    <option value="15">15</option>
                                    <option value="50">50</option>
                                    <option value="100">100</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <table class="invoices-panel-table table table-bordered">
                        <thead class="invoices-panel-header">
                        <tr>
                            <th>
                                <input type="text" placeholder="<fmt:message key='pages.invoices.code'/>" class="form-control" v-model="invoices.search.filters.code">
                            </th>
                            <th>
                                <input type="text" placeholder="<fmt:message key='pages.invoices.vehicle_name'/>" class="form-control" v-model="invoices.search.filters.carName">
                                <Sorter name="carName" :order-by="invoices.search.orderBy"></Sorter>
                            </th>
                            <th>
                                <div>
                                    <Datepicker v-model="invoices.search.filters.datesRange"
                                                range
                                                :format="format"
                                                :preview-format="format"
                                                :enable-time-picker="false"
                                                dark hide-input-icon
                                                input-class-name="form-control invoices-dates-range-input"
                                                auto-apply
                                                locale="<crrt:lang/>"
                                                placeholder="<fmt:message key='pages.invoices.dates_range'/>"/>
                                </div>
                                <Sorter name="datesRange" :order-by="invoices.search.orderBy"></Sorter>
                            </th>
                            <th class="sortableTh">
                                <fmt:message key='pages.invoices.price'/>
                                <Sorter name="price" :order-by="invoices.search.orderBy"></Sorter>
                            </th>
                            <th>
                                <input type="text" placeholder="<fmt:message key='pages.invoices.driver_email'/>" class="form-control" v-model="invoices.search.filters.driverEmail">
                            </th>
                            <th>
                                <input type="email" placeholder="<fmt:message key='pages.invoices.client_email'/>" class="form-control" v-model="invoices.search.filters.clientEmail">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="invoices.search.filters.status">
                                    <option v-for="(status, index) in invoiceStatuses" :key="index" :value="index">{{status.name}}</option>
                                </select>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="invoice in invoices.list" :key="invoice.id" :class="{lostAttention: invoice.status === 2 || invoice.status === 3}" @click="openInvoiceDetailsModal(invoice.id)">
                            <td>{{invoice.code}}</td>
                            <td>{{invoice.brand}} {{invoice.model}}</td>
                            <td><span class="flat-chip status-chip" data-status-code="1">{{invoice.datesRange.start}}</span><span class="flat-chip status-chip" data-status-code="2">{{invoice.datesRange.end}}</span></td>
                            <td>{{invoice.price}}$</td>
                            <td><div class="driver-chip" v-if="invoice.driver"><span class="driver-avatar cover-bg-type" v-if="invoice.driver.avatar" :style="{backgroundImage: 'url(${avatarsDir}/'+invoice.driver.avatar+')'}"></span><span class="driver-code">{{invoice.driver.email}}</span></div></td>
                            <td>{{invoice.clientEmail}}</td>
                            <td class="status-column">
                                <span class="flat-chip invoice-status-chip" v-for="iStatus in invoice.statusList" :data-status-code="iStatus">{{invoiceStatuses[iStatus].name}}</span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div>
                        <ul class="pagination">
                            <li class="page-item" v-for="n in invoices.search.pagination.availablePages"><a class="page-link" href="#" :class="{active: n == invoices.search.pagination.currentPage}" @click="goToInvoicesPage(n)">{{n}}</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="carEdit_modal" tabindex="1" role="dialog" aria-labelledby="carEdit_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.admin.cars.editing.title"/></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.cars.editing.car_photos"/>
                        </div>
                        <form action="#" class="car-add-photo-form">
                            <input type="file" style="display: none" class="car-add-photo-input" name="car-image" accept="image/png, image/gif, image/jpeg">
                        </form>
                        <div class="car-photos-container mb-2">
                            <div class="mc-photo cover-bg-type"
                                 v-for="imageItem in cars.workingOn.images"
                                 :key="imageItem.id"
                                 :style="{'background-image': 'url(&quot;${imagesDir}/'+imageItem.fileName+'&quot;)'}"
                                 :class="{'mc-photo-focused': focusedImage && focusedImage.id === imageItem.id}"
                                 @click="focusOnImage(imageItem.id)"
                            ></div>
                            <div class="mc-add-photo">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect opacity="0.5" x="11.364" y="20.364" width="16" height="2" rx="1" transform="rotate(-90 11.364 20.364)" fill="currentColor"/>
                                    <rect x="4.36396" y="11.364" width="16" height="2" rx="1" fill="currentColor"/>
                                </svg>
                            </div>
                        </div>
                        <div class="car-photos-container-actions">
                            <template v-if="focusedImage">
                                <a :href="'${imagesDir}/'+focusedImage.fileName" target="_blank">
                                    <button class="mdx-md-button button-blue button-bordered button-w-icon">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path opacity="0.3" d="M4.7 17.3V7.7C4.7 6.59543 5.59543 5.7 6.7 5.7H9.8C10.2694 5.7 10.65 5.31944 10.65 4.85C10.65 4.38056 10.2694 4 9.8 4H5C3.89543 4 3 4.89543 3 6V19C3 20.1046 3.89543 21 5 21H18C19.1046 21 20 20.1046 20 19V14.2C20 13.7306 19.6194 13.35 19.15 13.35C18.6806 13.35 18.3 13.7306 18.3 14.2V17.3C18.3 18.4046 17.4046 19.3 16.3 19.3H6.7C5.59543 19.3 4.7 18.4046 4.7 17.3Z" fill="currentColor"/>
                                            <rect x="21.9497" y="3.46448" width="13" height="2" rx="1" transform="rotate(135 21.9497 3.46448)" fill="currentColor"/>
                                            <path d="M19.8284 4.97161L19.8284 9.93937C19.8284 10.5252 20.3033 11 20.8891 11C21.4749 11 21.9497 10.5252 21.9497 9.93937L21.9497 3.05029C21.9497 2.498 21.502 2.05028 20.9497 2.05028L14.0607 2.05027C13.4749 2.05027 13 2.52514 13 3.11094C13 3.69673 13.4749 4.17161 14.0607 4.17161L19.0284 4.17161C19.4702 4.17161 19.8284 4.52978 19.8284 4.97161Z" fill="currentColor"/>
                                        </svg>
                                        <span><fmt:message key="modal.admin.cars.editing.active_photo.button.open"/></span>
                                    </button>
                                </a>
                                <button class="mdx-md-button button-red button-bordered ml-2 button-w-icon" @click="deleteImage(focusedImage.id)">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 9C5 8.44772 5.44772 8 6 8H18C18.5523 8 19 8.44772 19 9V18C19 19.6569 17.6569 21 16 21H8C6.34315 21 5 19.6569 5 18V9Z" fill="currentColor"/>
                                        <path opacity="0.5" d="M5 5C5 4.44772 5.44772 4 6 4H18C18.5523 4 19 4.44772 19 5V5C19 5.55228 18.5523 6 18 6H6C5.44772 6 5 5.55228 5 5V5Z" fill="currentColor"/>
                                        <path opacity="0.5" d="M9 4C9 3.44772 9.44772 3 10 3H14C14.5523 3 15 3.44772 15 4V4H9V4Z" fill="currentColor"/>
                                    </svg>

                                    <span><fmt:message key="modal.admin.cars.editing.active_photo.button.delete"/></span>
                                </button>
                            </template>
                        </div>
                        <div class="mdx-divider mb-4"></div>
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.cars.editing.general_info.title"/>
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control required" placeholder="<fmt:message key='pages.cars.brand'/>" v-model="cars.workingOn.brand" ref="car_edit-brand">
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control" placeholder="<fmt:message key='pages.cars.model'/>" v-model="cars.workingOn.model" ref="car_edit-model">
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" placeholder="<fmt:message key='modal.admin.cars.editing.price'/>" v-model="cars.workingOn.price" ref="car_edit-price">
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.segment" ref="car_edit-segment">
                                <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                            </select>
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.city" ref="car_edit-city">
                                <option v-for="(city, index) in cities" :key="index" :value="index">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal"><fmt:message key='modal.admin.cars.editing.footer.button.close'/></button>
                    <button type="button" class="mdx-flat-button button-pink" @click="deleteCar()"><fmt:message key='modal.admin.cars.editing.footer.button.delete'/></button>
                    <button type="button" class="mdx-flat-button button-blue" @click="updateCar()"><fmt:message key='modal.admin.cars.editing.footer.button.update'/></button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="carCreate_modal" tabindex="1" role="dialog" aria-labelledby="carCreate_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key='modal.admin.cars.creation.title'/></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.cars.creation.car_photos.title"/>
                            <div class="mc-caution mt-2">
                                <fmt:message key="modal.admin.cars.creation.car_photos.currently_added"/> {{cars.selectedPhotosNumber}}
                            </div>
                        </div>
                        <form action="#" class="car-add-photo-form">
                            <input type="file" style="display: none" class="car-add-photo-input" name="car-image" multiple ref="create_car-images-input" @change="updateSelectedPhotosCounter" accept="image/png, image/gif, image/jpeg">
                        </form>
                        <button class="mc-add-photo mdx-md-button button-green button-w-icon button-bordered mb-3">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect opacity="0.5" x="11.364" y="20.364" width="16" height="2" rx="1" transform="rotate(-90 11.364 20.364)" fill="currentColor"/>
                                <rect x="4.36396" y="11.364" width="16" height="2" rx="1" fill="currentColor"/>
                            </svg>
                            <span><fmt:message key="modal.admin.cars.creation.car_photos.button.add"/></span>
                        </button>
                        <div class="mc-part-title">
                           <fmt:message key="modal.admin.cars.creation.general_info.title"/>
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control required" placeholder="<fmt:message key='pages.cars.brand'/>" v-model="cars.workingOn.brand" ref="car_create-brand">
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control" placeholder="<fmt:message key='pages.cars.model'/>" v-model="cars.workingOn.model"  ref="car_create-model">
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" placeholder="<fmt:message key='modal.admin.cars.creation.price'/>" v-model="cars.workingOn.price"  ref="car_create-price">
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.segment" ref="car_create-segment">
                                <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                            </select>
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.city"  ref="car_create-city">
                                <option v-for="(city, index) in cities" :key="index" :value="index">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal"><fmt:message key="modal.admin.cars.creation.footer.button.close"/></button>
                    <button type="button" class="mdx-flat-button button-blue" @click="createCar()"><fmt:message key="modal.admin.cars.creation.footer.button.create"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="userCreate_modal" tabindex="1" role="dialog" aria-labelledby="userCreate_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.admin.users.creation.title" /></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.users.creation.profile_info.title" />
                        </div>
                        <div class="mc-item" v-for="(input_i, key) in users.creation.input_list" :key="key" :class="{necessary_input: input_i.isNecessary, highlight_necessity: input_i.isNecessary && input_i.isFocused, 'mdx-password-wrap': key === 'password'}">
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
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="users.creation.chosenRole" ref="user_create-role-select">
                                <option v-for="(role, index) in roles" :key="index" :value="index">{{role.name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal"><fmt:message key="modal.admin.users.creation.footer.button.close"/></button>
                    <button type="button" class="mdx-flat-button button-blue" @click="createUser()"><fmt:message key="modal.admin.users.creation.footer.button.create"/></button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="userEdit_modal" tabindex="1" role="dialog" aria-labelledby="userEdit_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.admin.users.editing.title"/> {{users.editing.originalData.email}}</span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-item">
                            <span class="flat-chip status-chip" data-status-code="2" v-if="users.editing.originalData.state === 1">{{accountStates[1].name}}</span>
                        </div>
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.users.editing.profile_info.title"/>
                        </div>
                        <div class="mc-item" v-for="(input_i, key) in users.editing.input_list" :key="key" :class="{necessary_input: input_i.isNecessary, highlight_necessity: input_i.isNecessary && input_i.isFocused, 'mdx-password-wrap': key === 'password'}">
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
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="users.editing.chosenRole" ref="user_edit-role-select">
                                <option v-for="(role, index) in roles" :key="index" :value="index" v-show="parseInt(index) !== 0">{{role.name}}</option>
                            </select>
                        </div>
                        <div class="mc-part-title">
                            <fmt:message key="modal.admin.users.editing.extra_info.title"/>
                        </div>
                        <div class="mdx-divider arrow-down solid mt-3 mb-3"></div>
                        <div class="mc-item">
                            <fmt:message key="modal.admin.users.editing.balance"/>: <span class="flat-chip status-chip" data-status-code="5">{{users.editing.originalData.balance}}$</span>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.admin.users.editing.invoices_amount"/>: <span class="flat-chip status-chip" data-status-code="6">{{users.editing.originalData.invoicesAmount}}</span>
                        </div>
                        <div class="mc-item" v-if="users.editing.originalData.confirmationCode">
                            <fmt:message key="modal.admin.users.editing.conf_code"/>: <span class="flat-chip status-chip" data-status-code="7">{{users.editing.originalData.confirmationCode}}</span>
                        </div>
                        <div class="mc-item" v-if="users.editing.originalData.city">
                            <div><fmt:message key="modal.admin.users.editing.current_city.title"/>: <span class="flat-chip status-chip" :data-status-code="users.editing.originalData.city % 4 + 2">{{cities[users.editing.originalData.city].name}}</span></div>
                            <div class="micro-caution"><fmt:message key="modal.admin.users.editing.current_city.caution"/></div>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.admin.users.editing.reg_date"/>: <span class="flat-chip status-chip" data-status-code="8">{{users.editing.originalData.regDate}}</span>
                        </div>
                        <div class="mc-item">
                            <button class="mdx-md-button button-green button-bordered" data-ripple="red" v-if="users.editing.originalData.state === 1" @click="setUserState(2)"><fmt:message key="modal.admin.users.editing.button.unblock_user"/></button>
                            <button class="mdx-md-button button-pink button-bordered" data-ripple="red" v-if="users.editing.originalData.state === 2" @click="setUserState(1)"><fmt:message key="modal.admin.users.editing.button.block_user"/></button>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal"><fmt:message key="modal.admin.users.editing.footer.button.close"/></button>
                    <button type="button" class="mdx-flat-button button-blue" :class="{disabled: Object.keys(userUpdateChangedData).length === 0}" @click="updateUser()"><fmt:message key="modal.admin.users.editing.footer.button.save"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="invoiceDetails_modal" tabindex="1" role="dialog" aria-labelledby="invoiceDetails_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable" role="document">
            <div class="modal-content" v-if="invoices.details">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.manager.categories.invoices.title"/> <span class="flat-chip status-chip" data-status-code="6">{{invoices.details.code}}</span></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-item">
                            <span class="flat-chip invoice-status-chip" v-for="status in invoices.details.statusList" :data-status-code="status">{{invoiceStatuses[status].name}}</span>
                        </div>
                        <div class="mc-part-title">
                            <fmt:message key="modal.manager.categories.invoices.invoice_info"/>:
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.code"/>: <strong>{{invoices.details.code}}</strong>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.vehicle_name"/>: <strong>{{invoices.details.brand}} {{invoices.details.model}}</strong>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.city"/>: <strong>{{cities[invoices.details.city].name}}</strong>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.price"/>: <strong>{{invoices.details.price}}$</strong>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.client_email"/>: <strong>{{invoices.details.clientEmail}}</strong>
                        </div>
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.invoices.dates_range"/>: <span class="flat-chip status-chip" data-status-code="1">{{invoices.details.datesRange.start}}</span>to <span class="flat-chip status-chip" data-status-code="2">{{invoices.details.datesRange.end}}</span>
                        </div>
                        <div class="mc-item mc-driver-wrapper">
                            <fmt:message key="modal.manager.categories.invoices.driver"/>:
                            <div class="driver-chip" v-if="invoices.details.driver">
                                <span v-if="invoices.details.driver.avatar" class="driver-avatar cover-bg-type" :style="{backgroundImage: 'url(${avatarsDir}/'+invoices.details.driver.avatar+')'}"></span>
                                <span class="driver-code">{{invoices.details.driver.email}}</span></div>
                            <strong v-else class="ml-2"> <fmt:message key="modal.manager.categories.invoices.without_driver"/></strong>
                        </div>
                        <div class="mdx-divider solid mt-4 mb-4"></div>
                        <div class="mc-part-title">
                            <fmt:message key="pages.passport.title"/>:
                        </div>
                        <div class="mc-item">
                            <div class="row">
                                <div class="passport-block col-xl-6 col-md-6 col-xs-12">
                                    <div class="title">
                                        <fmt:message key="pages.passport.firstname"/> / <fmt:message key="pages.passport.surname"/> / <fmt:message key="pages.passport.patronymic"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.firstname}} / {{invoices.details.passport.surname}} / {{invoices.details.passport.patronymic}}
                                    </div>
                                </div>
                                <div class="passport-block col-xl-5 col-md-5 col-xs-12">
                                    <div class="title">
                                        <fmt:message key="pages.passport.date_of_birth"/> / <fmt:message key="pages.passport.date_of_issue"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.date_of_birth}} / {{invoices.details.passport.date_of_issue}}
                                    </div>
                                </div>
                                <div class="passport-block col-xl-4 col-md-4 col-xs-12">
                                    <div class="title">
                                        <fmt:message key="pages.passport.doc_number"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.doc_number}}
                                    </div>
                                </div>
                                <div class="passport-block col-xl-2 col-md-2 col-xs-12">
                                    <div class="title">
                                        <fmt:message key="pages.passport.rntrc"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.rntrc}}
                                    </div>
                                </div>
                                <div class="passport-block col-xl-3 col-md-3 col-xs-12">
                                    <div class="title">
                                        <fmt:message key="pages.passport.authority"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.authority}}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="mdx-divider solid mt-4 mb-4"></div>
                        <div class="mc-part-title">
                            <fmt:message key="modal.manager.categories.invoices.repairs.title"/>:
                            <div v-if="!invoices.details.repairInvoices.length" class="micro-caution"><fmt:message key="modal.manager.categories.invoices.repairs.caution"/></div>
                        </div>
                        <div class="mc-item">
                            <div class="accordion repairs-list accordion-flush mb-4" id="repairs-accordion" v-if="invoices.details.repairInvoices.length">
                                <div class="accordion-item" v-for="(repairInvoice, repIndex) in invoices.details.repairInvoices">
                                    <h2 class="accordion-header" :id="'heading'+repIndex">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" :data-bs-target="'#collapse'+repIndex" aria-expanded="true" :aria-controls="'collapse'+repIndex">
                                            <fmt:message key="modal.manager.categories.invoices.repairs.assigned_on"/> {{repairInvoice.tsCreated}}
                                            <span class="flat-chip invoice-status-chip ml-4" data-status-code="1" v-if="repairInvoice.isPaid"><fmt:message key="modal.manager.categories.invoices.repairs.status.paid"/></span>
                                            <span class="flat-chip invoice-status-chip ml-4" data-status-code="4" v-if="!repairInvoice.isPaid && !doesInvoiceDateExpired(repairInvoice.expirationDate)"><fmt:message key="modal.manager.categories.invoices.repairs.status.active"/></span>
                                            <span class="flat-chip invoice-status-chip ml-4" data-status-code="5" v-if="!repairInvoice.isPaid && doesInvoiceDateExpired(repairInvoice.expirationDate)"><fmt:message key="modal.manager.categories.invoices.repairs.status.expired"/></span>
                                        </button>
                                    </h2>
                                    <div :id="'collapse'+repIndex" class="accordion-collapse collapse" :aria-labelledby="'heading'+repIndex" data-bs-parent="#repairs-accordion">
                                        <div class="accordion-body">
                                            <div class="mb-3"><fmt:message key="modal.manager.categories.invoices.repairs.price"/>: <span class="flat-chip status-chip" data-status-code="1">{{repairInvoice.price}}$</span></div>
                                            <div class="mb-3">
                                                <strong class="mb-2"><fmt:message key="modal.manager.categories.invoices.repairs.comment"/>:</strong>
                                                <div v-if="repairInvoice.comment">{{repairInvoice.comment}}</div>
                                                <div v-else><fmt:message key="modal.manager.categories.invoices.repairs.comment.not_mentioned"/></div>
                                            </div>
                                            <div class="mb-3">
                                                <strong class="mb-2"><fmt:message key="modal.manager.categories.invoices.repairs.exp_date"/>:</strong>
                                                <div>{{repairInvoice.expirationDate}}</div>
                                            </div>
                                            <div>
                                                <button class="mdx-flat-button button-pink" @click="deleteRepairInvoice(repairInvoice.id)"><fmt:message key="modal.manager.categories.invoices.repairs.button.delete.p1"/> <span v-if="repairInvoice.isPaid" style="margin-left: 5px"> <fmt:message key="modal.manager.categories.invoices.repairs.button.delete.p2"/></span></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div>
                                <button class="mdx-flat-button button-blue" @click="openCreateRepairInvoiceModal()"><fmt:message key="modal.manager.categories.invoices.repairs.button.create"/></button>
                            </div>
                        </div>
                        <div class="mdx-divider solid mb-4 mt-4"></div>
                        <template v-if="invoices.details.statusList.includes(2)">
                            <div class="mc-part-title">
                                <fmt:message key="modal.manager.categories.invoices.rejection.title"/>
                            </div>
                            <div class="mc-item">
                                <textarea v-if="invoices.details.rejectionReason" disabled class="form-control" :value="invoices.details.rejectionReason"></textarea>
                                <div v-else class="micro-caution"><fmt:message key="modal.manager.categories.invoices.rejection.not_specified"/></div>
                            </div>
                        </template>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-red button-bordered" data-bs-dismiss="modal" v-if="!invoices.details.statusList.includes(2)" @click="openRejectInvoiceModal()"><fmt:message key="modal.manager.categories.invoices.footer.button.reject_invoice"/></button>
                    <button type="button" class="mdx-md-button button-reversed button-bordered" data-bs-dismiss="modal"><fmt:message key="modal.manager.categories.invoices.footer.button.close"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="createRepairInvoice_modal" tabindex="1" role="dialog" aria-labelledby="createRepairInvoice_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.manager.categories.repair_invoices.title"/><span class="flat-chip status-chip" data-status-code="6">{{invoices.repairInvoice.originCode}}</span></span></h5>
                    <button type="button" class="close" @click="closeCreateRepairInvoiceModal()" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-item">
                            <fmt:message key="modal.manager.categories.repair_invoices.target_invoice"/>: <span class="flat-chip status-chip" data-status-code="6">{{invoices.repairInvoice.originCode}}</span>
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" :data-validation-highlight="!invoices.repairInvoice.price.isValid" v-model="invoices.repairInvoice.price.value" placeholder="<fmt:message key='modal.manager.categories.repair_invoices.penalty_price'/>">
                        </div>
                        <div class="mc-item">
                            <Datepicker v-model="invoices.repairInvoice.expirationDate.date"
                                        :flow="['year', 'month', 'calendar']"
                                        placeholder="<fmt:message key='modal.manager.categories.repair_invoices.exp_date'/>"
                                        :format="getFormattedDate"
                                        <c:if test="${requestScope.get('endTheme') eq VisualThemes.DARK.getFileName()}">dark</c:if>
                                        input-class-name="form-control rejectInvoiceDatepicker"
                                        :min-date="new Date() + 1"
                                        locale="<crrt:lang/>"
                                        auto-apply
                                        :enable-time-picker="false"
                                        :state="invoices.repairInvoice.expirationDate.isValid"
                            />
                        </div>
                        <div class="mc-item">
                            <textarea class="form-control" placeholder="<fmt:message key='modal.manager.categories.repair_invoices.comment'/>" v-model="invoices.repairInvoice.comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed button-bordered" @click="closeCreateRepairInvoiceModal()"><fmt:message key="modal.manager.categories.repair_invoices.footer.button.close"/></button>
                    <button type="button" class="mdx-md-button button-blue button-bordered" @click="createRepairInvoice()"><fmt:message key="modal.manager.categories.repair_invoices.footer.button.create"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="rejectInvoice_modal" tabindex="1" role="dialog" aria-labelledby="rejectInvoices_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.manager.categories.reject_invoice.title"/> <span class="flat-chip status-chip" data-status-code="6">{{invoices.rejection.originCode}}</span></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" @click="closeRejectInvoiceModal()" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            <fmt:message key="modal.manager.categories.reject_invoice.target_invoice"/>: <span class="flat-chip status-chip" data-status-code="6">{{invoices.rejection.originCode}}</span>
                        </div>
                        <div class="mc-item">
                            <textarea class="form-control" v-model="invoices.rejection.reason" placeholder="<fmt:message key="modal.manager.categories.reject_invoice.reason"/>">

                            </textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal" @click="closeRejectInvoiceModal()"><fmt:message key="modal.manager.categories.reject_invoice.footer.button.close"/></button>
                    <button type="button" class="mdx-flat-button button-pink" @click="rejectInvoice()"><fmt:message key="modal.manager.categories.reject_invoice.footer.button.reject"/></button>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="components/loader.jspf"%>
<%@include file="components/footerLinks.jspf"%>
<script src="https://unpkg.com/@vuepic/vue-datepicker@latest"></script>
</body>
</html>