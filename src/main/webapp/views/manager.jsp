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
            "invoiceStatuses": <%=JSJS.InvoiceStatusesList((String) pageContext.getAttribute("lang"), false)%>,
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
                                <label for="invoices-pag-sel"><fmt:message key="page.manager.categories.invoices.pagination.title"/>: </label>
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
                                                <c:if test="${requestScope.get('endTheme') eq VisualThemes.DARK.getFileName()}">dark</c:if>
                                                hide-input-icon
                                                input-class-name="form-control invoices-dates-range-input"
                                                auto-apply
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
                    <h5 class="modal-title"><span class="inTitle-action"><fmt:message key="modal.manager.categories.invoices.title"/> <span class="flat-chip status-chip" data-status-code="6">{{invoices.details.code}}</span></span></h5>
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
                                <span class="driver-avatar cover-bg-type" :style="{backgroundImage: 'url(${avatarsDir}/'+invoices.details.driver.avatar+')'}"></span>
                                <span class="driver-code">{{invoices.details.driver.email}}</span></div>
                            <strong v-else class="ml-2"> <fmt:message key="modal.manager.categories.invoices.without_driver"/></strong>
                        </div>
                        <div class="mdx-divider solid mt-4 mb-4"></div>
                        <div class="mc-part-title">
                            <fmt:message key="pages.passport.title"/>:
                        </div>
                        <div class="mc-item">
                            <div class="row">
                                <div class="passport-block col-6">
                                    <div class="title">
                                        <fmt:message key="pages.passport.firstname"/> / <fmt:message key="pages.passport.surname"/> / <fmt:message key="pages.passport.patronymic"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.firstname}} / {{invoices.details.passport.surname}} / {{invoices.details.passport.patronymic}}
                                    </div>
                                </div>
                                <div class="passport-block col-5">
                                    <div class="title">
                                        <fmt:message key="pages.passport.date_of_birth"/> / <fmt:message key="pages.passport.date_of_issue"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.date_of_birth}} / {{invoices.details.passport.date_of_issue}}
                                    </div>
                                </div>
                                <div class="passport-block col-4">
                                    <div class="title">
                                        <fmt:message key="pages.passport.doc_number"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.doc_number}}
                                    </div>
                                </div>
                                <div class="passport-block col-2">
                                    <div class="title">
                                        <fmt:message key="pages.passport.rntrc"/>
                                    </div>
                                    <div class="description">
                                        {{invoices.details.passport.rntrc}}
                                    </div>
                                </div>
                                <div class="passport-block col-2">
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
                    <button type="button" class="mdx-md-button button-reversed button-bordered" data-bs-dismiss="modal" @click="closeInvoiceDetailsModal()"><fmt:message key="modal.manager.categories.invoices.footer.button.close"/></button>
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
                                        input-class-name="form-control"
                                        :min-date="new Date()"
                                        auto-apply
                                        locale="<crrt:lang/>"
                                        hide-input-icon
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
<!-- Design libs-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.0-beta1/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
<script src="https://unpkg.com/vue@3"></script>
<script src="https://unpkg.com/@vuepic/vue-datepicker@latest"></script>
</body>
</html>