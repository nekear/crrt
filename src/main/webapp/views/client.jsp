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
    <link rel="stylesheet" href="${assets}css/themes/${requestScope.get("endTheme")}.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/inside.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/client.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>My invoices | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/isBetween.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.5/plugin/customParseFormat.min.js"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/client.js" defer></script>

    <script>
        const contextPath = '${imagesDir}';
        const loaded = {
            "cities": <%=JSJS.CitiesList((String) pageContext.getAttribute("lang"))%>,
            "invoiceStatuses": <%=JSJS.InvoiceStatusesList((String) pageContext.getAttribute("lang"), true)%>,
        };
        const userBalance = ${user.balance};
    </script>
</head>
<body>
<div id="app">
    <%@include file="components/header.jspf"%>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container">
                    <div class="mb-3">
                        <h5>Ваші замовлення</h5>
                        <h6>Статус замовлення можна переглянути у стовпчику "Статуси" або натиснувши кнопку "Детальніше".</h6>
                    </div>
                    <div class="mdx-divider solid">
                    </div>
                    <div class="mb-3 mt-3">
                        <h5>Пошук:</h5>
                        <input type="text" class="form-control" id="client-invoices-search" placeholder="Enter here invoice code, vehicle name or date..." v-model="invoices.search.filters.value">
                    </div>
                    <table class="client-invoices-table table table-bordered" style="margin-bottom: 0 !important;">
                        <thead class="cit-header">
                        <tr>
                            <th>
                                Code
                            </th>
                            <th>
                                Vehicle name
                            </th>
                            <th>
                                Renting dates
                            </th>
                            <th>
                                Price
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
                            <td>
                                {{invoice.code}}
                                <span class="flat-chip status-chip" data-status-code="1" v-if="isActiveInvoice(invoice.datesRange.start, invoice.datesRange.end)">Active</span>
                            </td>
                            <td>{{invoice.brand}} {{invoice.model}}</td>
                            <td>
                                <span class="flat-chip status-chip" data-status-code="1">{{invoice.datesRange.start}}</span>
                                <span class="flat-chip status-chip" data-status-code="2">{{invoice.datesRange.end}}</span>
                            </td>
                            <td>
                                {{invoice.price}} $
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
                                <button class="mdx-md-button button-blue button-bordered" @click="openInvoiceDetailsModal(invoice.id)">Details</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class='banner-alert danger' v-if="!invoices_list_paginated.length">
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
                    <div class="mt-3">
                        <ul class="pagination">
                            <li class="page-item" v-for="n in invoices_pages"><a class="page-link" href="#" :class="{active: n == invoices.search.pagination.currentPage}" @click.prevent="goToInvoicesPage(n)">{{n}}</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="invoiceDetails_modal" tabindex="1" role="dialog" aria-labelledby="invoiceDetails_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable" role="document">
            <div class="modal-content" v-if="invoices.details">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action">Details of invoice <span class="flat-chip status-chip" data-status-code="6">{{invoices.details.code}}</span></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close" @click="closeInvoiceDetailsModal()">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-item">
                            <span class="flat-chip invoice-status-chip" v-for="status in invoices.details.statusList" :data-status-code="status" v-show="status != 6">{{invoiceStatuses[status].name}}</span>
                        </div>
                        <div class="mc-part-title">
                            Invoice information:
                        </div>
                        <div class="mc-item">
                            Code: <strong>{{invoices.details.code}}</strong>
                        </div>
                        <div class="mc-item">
                            Vehicle name: <strong>{{invoices.details.brand}} {{invoices.details.model}}</strong>
                        </div>
                        <div class="mc-item">
                            City: <strong>{{cities[invoices.details.city].name}}</strong>
                        </div>
                        <div class="mc-item">
                            Price: <strong>{{invoices.details.price}}$</strong>
                        </div>
                        <div class="mc-item">
                            Dates range: <span class="flat-chip status-chip" data-status-code="1">{{invoices.details.datesRange.start}}</span>to <span class="flat-chip status-chip" data-status-code="2">{{invoices.details.datesRange.end}}</span>
                        </div>
                        <div class="mc-item mc-driver-wrapper">
                            With driver:
                            <div>
                                <span class="ml-2 flat-chip status-chip" data-status-code="1" v-if="invoices.details.statusList.includes(6)">Yes</span>
                                <span class="ml-2 flat-chip status-chip" data-status-code="2" v-else>No</span>
                            </div>
                        </div>
                        <div class="mdx-divider solid mt-4 mb-4"></div>
                        <div class="mc-part-title">
                            Passport data:
                        </div>
                        <div class="mc-item">
                            <div class="row">
                                <div class="passport-block col-6">
                                    <div class="title">
                                        Firstname / Surname / Patronymic
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.firstname}} / {{invoices.details.passport.surname}} / {{invoices.details.passport.patronymic}}
                                    </div>
                                </div>
                                <div class="passport-block col-5">
                                    <div class="title">
                                        Date of birth / Date of issue
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.date_of_birth}} / {{invoices.details.passport.date_of_issue}}
                                    </div>
                                </div>
                                <div class="passport-block col-4">
                                    <div class="title">
                                        Document number
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.doc_number}}
                                    </div>
                                </div>
                                <div class="passport-block col-2">
                                    <div class="title">
                                        RNTRC
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.rntrc}}
                                    </div>
                                </div>
                                <div class="passport-block col-2">
                                    <div class="title">
                                        Authority
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.authority}}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="mdx-divider solid mt-4 mb-4"></div>
                        <template v-if="invoices.details.repairInvoices && invoices.details.repairInvoices.length">
                            <div class="mc-part-title">
                                Repairs:
                            </div>
                            <div class="mc-item">
                                <div class="accordion repairs-list accordion-flush mb-4" id="repairs-accordion" v-if="invoices.details.repairInvoices.length">
                                    <div class="accordion-item" v-for="(repairInvoice, repIndex) in invoices.details.repairInvoices">
                                        <h2 class="accordion-header" :id="'heading'+repIndex">
                                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" :data-bs-target="'#collapse'+repIndex" aria-expanded="true" :aria-controls="'collapse'+repIndex">
                                                Assigned on {{repairInvoice.tsCreated}}
                                                <span class="flat-chip invoice-status-chip ml-4" data-status-code="1" v-if="repairInvoice.isPaid">Paid</span>
                                                <span class="flat-chip invoice-status-chip ml-4" data-status-code="4" v-if="!repairInvoice.isPaid && !doesInvoiceDateExpired(repairInvoice.expirationDate)">Active</span>
                                                <span class="flat-chip invoice-status-chip ml-4" data-status-code="5" v-if="!repairInvoice.isPaid && doesInvoiceDateExpired(repairInvoice.expirationDate)">Expired</span>
                                            </button>
                                        </h2>
                                        <div :id="'collapse'+repIndex" class="accordion-collapse collapse" :aria-labelledby="'heading'+repIndex" data-bs-parent="#repairs-accordion">
                                            <div class="accordion-body">
                                                <div class="mb-3">Price: <span class="flat-chip status-chip" data-status-code="1">{{repairInvoice.price}}$</span></div>
                                                <div class="mb-3">
                                                    <strong class="mb-2">Comment:</strong>
                                                    <div v-if="repairInvoice.comment">{{repairInvoice.comment}}</div>
                                                    <div v-else>not mentioned</div>
                                                </div>
                                                <div class="mb-3">
                                                    <strong class="mb-2">Expiration date:</strong>
                                                    <div>{{repairInvoice.expirationDate}}</div>
                                                </div>
                                                <div>
                                                    <button class="mdx-flat-button button-pink" :class="{disabled: repairInvoice.price > user.balance}" @click="payRepairInvoice(repairInvoice.id)" v-if="!repairInvoice.isPaid">Pay</button>
                                                    <div class="micro-caution-alert mt-2" v-if="repairInvoice.price > user.balance && !repairInvoice.isPaid">You don`t have enough money on balance!</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </template>
                        <template v-if="invoices.details.repairInvoices && invoices.details.repairInvoices.length || invoices.details.statusList && invoices.details.statusList.includes(2)">
                            <div class="mdx-divider solid mb-4 mt-4"></div>
                        </template>
                        <template v-if="invoices.details.statusList.includes(2)">
                            <div class="mc-part-title">
                                Rejection reason
                            </div>
                            <div class="mc-item">
                                <textarea v-if="invoices.details.rejectionReason" disabled class="form-control" :value="invoices.details.rejectionReason"></textarea>
                                <div v-else class="micro-caution">Not specified...</div>
                            </div>
                        </template>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-flat-button button-pink" @click="cancelInvoice()" v-if="isInvoiceCancellationAvailable">Cancel invoice</button>
                    <button type="button" class="mdx-md-button button-reversed button-bordered" data-bs-dismiss="modal">Закрити</button>
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