<%@ page import="com.github.DiachenkoMD.web.utils.JSJS" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.github.DiachenkoMD.entities.dto.invoices.PanelInvoice" %>
<%@ page import="com.github.DiachenkoMD.web.utils.CryptoStore" %>
<%@ page import="java.util.List" %>
<%@ page import="com.github.DiachenkoMD.entities.dto.PaginationResponse" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="crrt" uri="crrt" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

<%@ include file="components/generals.jspf"%>

<%
    // Preparing invoices list object to use on the page
    Gson gson = (Gson) application.getAttribute("gson");
    PaginationResponse<PanelInvoice> paginationResponse = (PaginationResponse<PanelInvoice>) request.getAttribute("paginationResponse");

    List<PanelInvoice> foundInvoices = paginationResponse.getResponseData();

    double totalElements = paginationResponse.getTotalElements();
    int elementsPerPage = request.getParameter("elementsPerPage") != null
            ? Integer.parseInt(request.getParameter("elementsPerPage"))
            : 15;
    pageContext.setAttribute("availablePages", (int) Math.ceil(totalElements / elementsPerPage));
    pageContext.setAttribute("foundInvoices", foundInvoices);
%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <!-- Design libs -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${assets}modules/argon/argon.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">
    <link rel="stylesheet" href="${assets}modules/loaders/loaders.css">
    <link rel="stylesheet" href="https://unpkg.com/@vuepic/vue-datepicker@latest/dist/main.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/colorize.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/admin.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>Managers | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dayjs/1.11.4/dayjs.min.js"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/manager.js" defer></script>

    <script>
        const contextPath = '${imagesDir}';
        const loaded = {
            "cities": <%=JSJS.CitiesList((String) pageContext.getAttribute("lang"))%>,
            "invoiceStatuses": <%=JSJS.InvoiceStatusesList((String) pageContext.getAttribute("lang"))%>,
        };
        const invoicesList = ''
    </script>
</head>
<body>
<div id="app">
    <%@include file="components/header.jspf"%>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container">
                    <div class="mb-3 canvas-tools">
                        <div>
                            <h5>Invoices list</h5>
                            <h6>Found invoices amount: <span>{{invoices.search.pagination.totalFoundEntities}}</span></h6>
                            <div>
                                <button class="mdx-md-button button-blue button-bordered" @click="performInvoicesSearch(1)">
                                    Search
                                </button>
                            </div>
                        </div>
                        <div>
                            <div>
                                <label for="invoices-pag-sel">Invoices per page: </label>
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
                                <input type="text" placeholder="Code" class="form-control" v-model="invoices.search.filters.code">
                            </th>
                            <th>
                                <input type="text" placeholder="Name" class="form-control" v-model="invoices.search.filters.carName">
                                <div class="sort-marker" :data-sort-type="getSortOrder('carName')" @click="increaseSort('carName')">
                                    <div class="sort-wrap">
                                        <div class="sort-asc-arrow"></div>
                                        <div v-if="invoices.search.orderBy.length > 1">{{getSortIndex('carName')}}</div>
                                        <div class="sort-desc-arrow"></div>
                                    </div>
                                </div>
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
                                                placeholder="Dates range"/>
                                </div>
                                <div class="sort-marker" :data-sort-type="getSortOrder('datesRange')" @click="increaseSort('datesRange')">
                                    <div class="sort-wrap">
                                        <div class="sort-asc-arrow"></div>
                                        <div v-if="invoices.search.orderBy.length > 1">{{getSortIndex('datesRange')}}</div>
                                        <div class="sort-desc-arrow"></div>
                                    </div>
                                </div>
                            </th>
                            <th class="sortableTh">
                                Price
                                <div class="sort-marker" :data-sort-type="getSortOrder('price')" @click="increaseSort('price')">
                                    <div class="sort-wrap">
                                        <div class="sort-asc-arrow"></div>
                                        <div v-if="invoices.search.orderBy.length > 1">{{getSortIndex('price')}}</div>
                                        <div class="sort-desc-arrow"></div>
                                    </div>
                                </div>
                            </th>
                            <th>
                                <input type="text" placeholder="Driver email or -" class="form-control" v-model="invoices.search.filters.driverEmail">
                            </th>
                            <th>
                                <input type="email" placeholder="Client email" class="form-control" v-model="invoices.search.filters.clientEmail">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="invoices.search.filters.status">
                                    <option v-for="(status, index) in invoiceStatuses" :key="index" :value="index">{{status.name}}</option>
                                </select>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="invoice" items="${foundInvoices}">

                            <tr  @click="openInvoiceDetailsModal('${CryptoStore.encrypt(String.valueOf(invoice.id))}')">
                                <td>${invoice.code}</td>
                                <td>${invoice.brand} ${invoice.model}</td>
                                <td><span class="flat-chip status-chip" data-status-code="1">${invoice.datesRange.start}</span><span class="flat-chip status-chip" data-status-code="2">${invoice.datesRange.end}</span></td>
                                <td>${invoice.price}$</td>
                                <td>
                                    <c:if test="${not empty invoice.driver}">
                                        <div class="driver-chip">
                                            <c:if test="${not empty invoice.driver.avatar}">
                                                <span class="driver-avatar cover-bg-type" style="background-image: url('${avatarsDir}/${invoice.driver.avatar}')"></span>
                                            </c:if>
                                            <span class="driver-code">${invoice.driver.email}</span>
                                        </div>
                                    </c:if>
                                </td>
                                <td>${invoice.clientEmail}</td>
                                <td class="status-column">
                                    <c:forEach var="status" items="${invoice.statusList}">
                                        <span class="flat-chip invoice-status-chip" data-status-code="${status.id()}"><fmt:message key="invoice_status.${status.keyword()}"/> </span>
                                    </c:forEach>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <div>
                        <ul class="pagination">
                            <li class="page-item" v-for="n in ${availablePages}"><a class="page-link" href="#" :class="{active: n == invoices.search.pagination.currentPage}" @click="goToInvoicesPage(n)">{{n}}</a></li>
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
                            <span class="flat-chip invoice-status-chip" v-for="status in invoices.details.statusList" :data-status-code="status">{{invoiceStatuses[status].name}}</span>
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
                            Client email: <strong>{{invoices.details.clientEmail}}</strong>
                        </div>
                        <div class="mc-item">
                            Dates range: <span class="flat-chip status-chip" data-status-code="1">{{invoices.details.datesRange.start}}</span>to <span class="flat-chip status-chip" data-status-code="2">{{invoices.details.datesRange.end}}</span>
                        </div>
                        <div class="mc-item mc-driver-wrapper">
                            Driver:
                            <div class="driver-chip" v-if="invoices.details.driver">
                                <span class="driver-avatar cover-bg-type" :style="{backgroundImage: 'url(${avatarsDir}/'+invoices.details.driver.avatar+')'}"></span>
                                <span class="driver-code">{{invoices.details.driver.email}}</span></div>
                            <strong v-else class="ml-2"> without driver</strong>
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
                        <div class="mc-part-title">
                            Repairs:
                            <div v-if="!invoices.details.repairInvoices.length" class="micro-caution">This invoice doesn`t have any coupled repairment invoices yet...</div>
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
                                                <button class="mdx-flat-button button-pink" @click="deleteRepairInvoice(repairInvoice.id)">Delete <span v-if="repairInvoice.isPaid" class="ml-1">and refund</span></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div>
                                <button class="mdx-flat-button button-blue" @click="openCreateRepairInvoiceModal()">Create repair invoice</button>
                            </div>
                        </div>
                        <div class="mdx-divider solid mb-4 mt-4"></div>
                        <template v-if="invoices.details.statusList.includes(2)">
                            <div class="mc-part-title">
                                Rejection reason
                            </div>
                            <div class="mc-item">
                                <textarea disabled class="form-control" :value="invoices.details.rejectionReason"></textarea>
                            </div>
                        </template>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-red button-bordered" data-bs-dismiss="modal" v-if="!invoices.details.statusList.includes(2)" @click="openRejectInvoiceModal()">Reject invoice</button>
                    <button type="button" class="mdx-md-button button-reversed button-bordered" data-bs-dismiss="modal" @click="closeInvoiceDetailsModal()">Закрити</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="createRepairInvoice_modal" tabindex="1" role="dialog" aria-labelledby="createRepairInvoice_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action">Creating repairment invoice for <span class="flat-chip status-chip" data-status-code="6">{{invoices.repairInvoice.originCode}}</span></span></h5>
                    <button type="button" class="close" @click="closeCreateRepairInvoiceModal()" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-item">
                            Target invoice code: <span class="flat-chip status-chip" data-status-code="6">{{invoices.repairInvoice.originCode}}</span>
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" :data-validation-highlight="!invoices.repairInvoice.price.isValid" v-model="invoices.repairInvoice.price.value" placeholder="Penalty price" ref="">
                        </div>
                        <div class="mc-item">
                            <Datepicker v-model="invoices.repairInvoice.expirationDate.date"
                                        :flow="['year', 'month', 'calendar']"
                                        placeholder="Expiration date"
                                        :format="getFormattedDate"
                                        dark
                                        input-class-name="form-control"
                                        :min-date="new Date()"
                                        auto-apply
                                        :enable-time-picker="false"
                                        :state="invoices.repairInvoice.expirationDate.isValid"
                            />
                        </div>
                        <div class="mc-item">
                            <textarea class="form-control" placeholder="Comment" v-model="invoices.repairInvoice.comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed button-bordered" @click="closeCreateRepairInvoiceModal()">Close</button>
                    <button type="button" class="mdx-md-button button-blue button-bordered" @click="createRepairInvoice()">Create</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="rejectInvoice_modal" tabindex="1" role="dialog" aria-labelledby="rejectInvoices_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action">Rejection of invoice <span class="flat-chip status-chip" data-status-code="6">{{invoices.rejection.originCode}}</span></span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" @click="closeRejectInvoiceModal()" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            Target invoice code: <span class="flat-chip status-chip" data-status-code="6">{{invoices.rejection.originCode}}</span>
                        </div>
                        <div class="mc-item">
                            <textarea class="form-control" v-model="invoices.rejection.reason" placeholder="Reason">

                            </textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal" @click="closeRejectInvoiceModal()">Close</button>
                    <button type="button" class="mdx-flat-button button-pink" @click="rejectInvoice()">Reject</button>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="components/loader.jspf"%>
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="${assets}modules/argon/argon.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
<script src="https://unpkg.com/@vuepic/vue-datepicker@latest"></script>
</body>
</html>