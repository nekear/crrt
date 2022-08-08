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
    <link rel="stylesheet" href="${assets}modules/argon/argon.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${assets}modules/notiflix/notiflix-3.2.5.min.css">
    <link rel="stylesheet" href="${assets}modules/loaders/loaders.css">
    <!--  Custom  -->
    <link rel="stylesheet" href="${assets}css/themes/dark_theme.css">
    <link rel="stylesheet" href="${assets}css/globals.css">
    <link rel="stylesheet" href="${assets}css/colorize.css">
    <link rel="stylesheet" href="${assets}css/mdx.css">
    <link rel="stylesheet" href="${assets}css/admin.css">

    <link rel="stylesheet" href="${assets}css/media.css">
    <title>Admin | CRRT.</title>

    <!--  Jquery  -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>

    <!--  Custom  -->
    <script src="${assets}modules/notiflix/notiflix-3.2.5.min.js"></script>
    <script src="${assets}js/mdx.js"></script>
    <script src="${assets}js/global.js" defer></script>
    <script src="${assets}js/admin.js" defer></script>
</head>
<body>
<div id="app">
    <%@include file="components/header.jspf"%>
    <div class="page-data">
        <div class="view-content">
            <div class="sized__container">
                <div class="canvas-container unstyled greetings-banner">
                    <h3>🎉 Welcome, <strong>xpert14world</strong></h3>
                </div>
                <div class="canvas-container">
                    <div class="row">
                        <div class="col-lg-4 stat-card">
                            <div class="stat-card-icon">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="8" y="9" width="3" height="10" rx="1.5" fill="currentColor"/>
                                    <rect opacity="0.5" x="13" y="5" width="3" height="14" rx="1.5" fill="currentColor"/>
                                    <rect x="18" y="11" width="3" height="8" rx="1.5" fill="currentColor"/>
                                    <rect x="3" y="13" width="3" height="6" rx="1.5" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="stat-card-title">
                                {{stats[0]}} $
                            </div>
                            <div class="stat-car-description">
                                General earnings
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
                                {{stats[1]}} $
                            </div>
                            <div class="stat-car-description">
                                Clear profit
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
                                {{stats[2]}} $
                            </div>
                            <div class="stat-car-description">
                                Invoices
                            </div>
                        </div>
                    </div>
                    <div style="text-align: center">
                        <button class="mdx-md-button button-bordered button-indigo button-w-icon rotating-icon mt-4" data-ripple="#7D7AFFFF">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M14.5 20.7259C14.6 21.2259 14.2 21.826 13.7 21.926C13.2 22.026 12.6 22.0259 12.1 22.0259C9.5 22.0259 6.9 21.0259 5 19.1259C1.4 15.5259 1.09998 9.72592 4.29998 5.82592L5.70001 7.22595C3.30001 10.3259 3.59999 14.8259 6.39999 17.7259C8.19999 19.5259 10.8 20.426 13.4 19.926C13.9 19.826 14.4 20.2259 14.5 20.7259ZM18.4 16.8259L19.8 18.2259C22.9 14.3259 22.7 8.52593 19 4.92593C16.7 2.62593 13.5 1.62594 10.3 2.12594C9.79998 2.22594 9.4 2.72595 9.5 3.22595C9.6 3.72595 10.1 4.12594 10.6 4.02594C13.1 3.62594 15.7 4.42595 17.6 6.22595C20.5 9.22595 20.7 13.7259 18.4 16.8259Z" fill="currentColor"/>
                                <path opacity="0.3" d="M2 3.62592H7C7.6 3.62592 8 4.02592 8 4.62592V9.62589L2 3.62592ZM16 14.4259V19.4259C16 20.0259 16.4 20.4259 17 20.4259H22L16 14.4259Z" fill="currentColor"/>
                            </svg>
                            <span @click="reloadStats()">Reload</span>
                        </button>
                    </div>
                </div>
                <div class="canvas-container">
                    <div class="mb-3">
                        <h5>Cars list</h5>
                        <h6>Overall cars number: <span>80</span></h6>
                        <div><button class="mdx-md-button button-green button-bordered" @click="openCarCreateModal()">Create new</button></div>
                    </div>
                    <table class="car-park-table table table-bordered">
                        <thead class="cpt-header">
                        <tr>
                            <th>
                                <input type="text" class="form-control" placeholder="Brand" v-model="cars.search.brand">
                            </th>
                            <th>
                                <input type="text" class="form-control" placeholder="Model" v-model="cars.search.model">
                            </th>
                            <th>
                                <select class="form-control form-select" v-model="cars.search.segment">
                                    <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                                </select>
                            </th>
                            <th>
                                <input type="number" class="form-control" placeholder="Min price" v-model="cars.search.price">
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
                        <tr v-for="car in cars_list" :key="car.id">
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
                                <button class="mdx-md-button button-green button-bordered" @click="openCarEditModal(car.id)">Редагувати</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="carEdit_modal" tabindex="1" role="dialog" aria-labelledby="carEdit_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action">Editing car information</span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            Car photos:
                        </div>
                        <form action="#" class="car-add-photo-form">
                            <input type="file" style="display: none" class="car-add-photo-input" name="car-image">
                        </form>
                        <div class="car-photos-container mb-2">
                            <div class="mc-photo cover-bg-type"  v-for="photoItem in cars.workingOn.photos" :key="photoItem.id"
                                 :style="{backgroundImage: 'url('+photoItem.file+')'}"
                                 :class="{'mc-photo-focused': focusedPhoto && focusedPhoto.id === photoItem.id}"
                                 @click="focusOnPhoto(photoItem.id)"
                            ></div>
                            <div class="mc-add-photo">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect opacity="0.5" x="11.364" y="20.364" width="16" height="2" rx="1" transform="rotate(-90 11.364 20.364)" fill="currentColor"/>
                                    <rect x="4.36396" y="11.364" width="16" height="2" rx="1" fill="currentColor"/>
                                </svg>
                            </div>
                        </div>
                        <div class="car-photos-container-actions">
                            <template v-if="focusedPhoto">
                                <a :href="focusedPhoto.file" target="_blank">
                                    <button class="mdx-md-button button-blue button-bordered button-w-icon">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path opacity="0.3" d="M4.7 17.3V7.7C4.7 6.59543 5.59543 5.7 6.7 5.7H9.8C10.2694 5.7 10.65 5.31944 10.65 4.85C10.65 4.38056 10.2694 4 9.8 4H5C3.89543 4 3 4.89543 3 6V19C3 20.1046 3.89543 21 5 21H18C19.1046 21 20 20.1046 20 19V14.2C20 13.7306 19.6194 13.35 19.15 13.35C18.6806 13.35 18.3 13.7306 18.3 14.2V17.3C18.3 18.4046 17.4046 19.3 16.3 19.3H6.7C5.59543 19.3 4.7 18.4046 4.7 17.3Z" fill="currentColor"/>
                                            <rect x="21.9497" y="3.46448" width="13" height="2" rx="1" transform="rotate(135 21.9497 3.46448)" fill="currentColor"/>
                                            <path d="M19.8284 4.97161L19.8284 9.93937C19.8284 10.5252 20.3033 11 20.8891 11C21.4749 11 21.9497 10.5252 21.9497 9.93937L21.9497 3.05029C21.9497 2.498 21.502 2.05028 20.9497 2.05028L14.0607 2.05027C13.4749 2.05027 13 2.52514 13 3.11094C13 3.69673 13.4749 4.17161 14.0607 4.17161L19.0284 4.17161C19.4702 4.17161 19.8284 4.52978 19.8284 4.97161Z" fill="currentColor"/>
                                        </svg>
                                        <span>Open</span>
                                    </button>
                                </a>
                                <button class="mdx-md-button button-red button-bordered ml-2 button-w-icon" @click="deletePhoto(focusedPhoto.id)">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 9C5 8.44772 5.44772 8 6 8H18C18.5523 8 19 8.44772 19 9V18C19 19.6569 17.6569 21 16 21H8C6.34315 21 5 19.6569 5 18V9Z" fill="currentColor"/>
                                        <path opacity="0.5" d="M5 5C5 4.44772 5.44772 4 6 4H18C18.5523 4 19 4.44772 19 5V5C19 5.55228 18.5523 6 18 6H6C5.44772 6 5 5.55228 5 5V5Z" fill="currentColor"/>
                                        <path opacity="0.5" d="M9 4C9 3.44772 9.44772 3 10 3H14C14.5523 3 15 3.44772 15 4V4H9V4Z" fill="currentColor"/>
                                    </svg>

                                    <span>Delete</span>
                                </button>
                            </template>
                        </div>
                        <div class="mdx-divider mb-4"></div>
                        <div class="mc-part-title">
                            General information:
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control required" placeholder="Brand" v-model="cars.workingOn.brand">
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control" placeholder="Model" v-model="cars.workingOn.model">
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" placeholder="Price per hour" v-model="cars.workingOn.price">
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.segment">
                                <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                            </select>
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.city">
                                <option v-for="(city, index) in cities" :key="index" :value="index">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal" @click="closeCarEditing()">Закрити</button>
                    <button type="button" class="mdx-flat-button button-pink" @click="deleteCar()">Видалити</button>
                    <button type="button" class="mdx-flat-button button-blue" @click="updateCar()">Зберегти</button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="carCreate_modal" tabindex="1" role="dialog" aria-labelledby="carCreate_modal" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><span class="inTitle-action">Creating new car</span></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <i class="material-icons">close</i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="ce-data-container">
                        <div class="mc-part-title">
                            Car photos:
                            <div class="mc-caution mt-2">
                                Currently added: {{cars.workingOn.loadedPhotosPackNumber}}
                            </div>
                        </div>
                        <form action="#" class="car-add-photo-form">
                            <input type="file" style="display: none" class="car-add-photo-input" name="car-image" multiple>
                        </form>
                        <button class="mc-add-photo mdx-md-button button-green button-w-icon button-bordered mb-3">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect opacity="0.5" x="11.364" y="20.364" width="16" height="2" rx="1" transform="rotate(-90 11.364 20.364)" fill="currentColor"/>
                                <rect x="4.36396" y="11.364" width="16" height="2" rx="1" fill="currentColor"/>
                            </svg>
                            <span>Add photos</span>
                        </button>
                        <div class="mc-part-title">
                            General information:
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control required" placeholder="Brand" v-model="cars.workingOn.brand">
                        </div>
                        <div class="mc-item">
                            <input type="text" class="form-control" placeholder="Model" v-model="cars.workingOn.model">
                        </div>
                        <div class="mc-item">
                            <input type="number" class="form-control" placeholder="Price per hour" v-model="cars.workingOn.price">
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.segment">
                                <option v-for="(segment, index) in segments" :key="index" :value="index">{{segment.name}}</option>
                            </select>
                        </div>
                        <div class="mc-item">
                            <select class="form-control form-select" v-model="cars.workingOn.city">
                                <option v-for="(city, index) in cities" :key="index" :value="index">{{city.name}}</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="mdx-md-button button-reversed" data-bs-dismiss="modal">Закрити</button>
                    <button type="button" class="mdx-flat-button button-blue" @click="createCar()">Зберегти</button>
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
</body>
</html>