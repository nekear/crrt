const { createApp } = Vue;

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*",
    "name_pattern": "^[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+$"
}
// const loaded = {
//     segments: {
//         0: {
//           name: "All segments"
//         },
//         1: {
//             name: "E-segment"
//         },
//         2: {
//             name: "F-segment"
//         },
//         3: {
//             name: "S-segment"
//         }
//     },
//     cities: {
//         0:{
//             name: "All cities"
//         },
//         1: {
//             name: "Kyiv"
//         },
//         2: {
//             name: "Lviv"
//         }
//     }
// }

const carsWorkingOnObjectProto = {
    id: null,
    images: [], // for editing
    brand: "",
    model: "",
    segment: 0,
    price: "",
    city: 0
};

const app = createApp({
    data() {
        return {
            tabs:{
                panel: {
                    active: "users"
                }
            },
            cars:{
                selectedPhotosNumber: 0,
                pagination: {
                    currentPage: 1,
                    itemsPerPage: 15,
                },
                imageFocusPrivate: {
                  id: ""
                },
                workingOn: {},
                workingOnHighlighter:{

                },
                search: {
                    brand: "",
                    model: "",
                    segment: 0,
                    price: "",
                    city: 0
                },
                list: []
            },
            users: {
                search:{
                    filters: {
                        email: "",
                        firstname: "",
                        surname: "",
                        patronymic: "",
                        role: 0,
                        state: 0
                    },
                    pagination: {
                        itemsPerPage: 15,
                        availablePages: 1,
                        currentPage: 1
                    }
                },
                list: [
                    {
                        id: 0,
                        email: "xpert14world@gmail.com",
                        firstname: "Mykhailo",
                        surname: "Diachenko",
                        patronymic: "Dmytrovich",
                        role: 1,
                        isBlocked: 0,
                        isChecked: 0
                    }
                ]
            },
            segments: {},
            cities: {},
            roles: {},
            accountStates:{},
            stats: []
        }
    },
    created(){
        Object.assign(this.segments, loaded.segments);
        Object.assign(this.cities, loaded.cities);
        Object.assign(this.roles, loaded.roles);
        Object.assign(this.accountStates, loaded.accountStates);

        // Assigning prototype
        Object.assign(this.cars.workingOn, carsWorkingOnObjectProto);
    },

    watch:{
      "tabs.panel.active"(newState, oldState){

      }
    },

    mounted(){
        // Getting stats
        axios({
            method: "get",
            url: 'http://localhost:8080/crrt_war/admin/stats',
            silent: true
        })
        .then(function (response) {
            console.log(response);
            app.stats = response.data;
        })
        .catch(function (error) {
            console.log(error);
        });

        // Getting cars list
        axios({
            method: "get",
            url: `http://localhost:8080/crrt_war/admin/cars`,
            silent: true
        })
        .then(function (response) {
            console.log(response);
            app.cars.list = response.data;
        })
        .catch(function (error) {
            console.log(error);
        });
    },

    computed: {
        cars_list: function () {
            const searchFilters = this.cars.search;

            let result = this.cars.list.filter(
                car => {
                    const f_brand = searchFilters.brand.toLowerCase();
                    const f_model = searchFilters.model.toLowerCase();

                    const c_brand = car.brand.toLowerCase();
                    const c_model = car.model.toLowerCase();

                    const f_segment = parseInt(searchFilters.segment);
                    const c_segment = parseInt(car.segment);

                    const f_price = parseInt(searchFilters.price);
                    const c_price = parseInt(car.price);

                    const f_city = parseInt(searchFilters.city);
                    const c_city = parseInt(car.city);

                    return c_brand.includes(f_brand) &&
                           c_model.includes(f_model) &&
                           (f_segment ? c_segment === f_segment : true) &&
                           (f_price ? c_price >= f_price : true) &&
                           (f_city ? c_city === f_city : true);

                }
            );

            return result;
        },
        cars_list_paginated: function(){
            let resultWithPagination = [];

            const currentPage = this.cars.pagination.currentPage;
            const itemsPerPage = this.cars.pagination.itemsPerPage;
            const result = this.cars_list;

            for(let i = (currentPage-1)*itemsPerPage; i < currentPage*itemsPerPage; i++){
                if(i < result.length){
                    resultWithPagination.push(result[i]);
                }
            }

            console.log(result, resultWithPagination);
            return resultWithPagination;
        },
        cars_pages: function(){
            return Math.ceil(this.cars_list.length / this.cars.pagination.itemsPerPage);
        },

        focusedImage: function (){
            if(this.cars.imageFocusPrivate.id){
                let image = null;

                for(let item in this.cars.workingOn.images){
                    let current = this.cars.workingOn.images[item];
                    if(current.id === this.cars.imageFocusPrivate.id){
                        image = current;
                        break;
                    }
                }

                return image;
            }
        },

    },
    methods:{
        // ========= TABS ========= //
        activateTab(group, tabName){
            this.tabs[group].active = tabName;
        },

        // ========= USERS RELATED ========= //

        usersCheckboxAll(event){
          const setState = event.target.checked;

          this.users.list = this.users.list.map(user => {
              user.isChecked = setState;
              return user;
          });
        },

        goToUsersPage(pageIndex){
          console.log("Tried to go to " + pageIndex);
        },

        users_sc_role(role){
            this.users.search.filters.role = parseInt(this.users.search.filters.role) === parseInt(role) ? 0 : parseInt(role);
        },


        performUsersSearch(){
            let searchRequestObject = {
                askedPage: 1,
                elementsPerPage: this.users.search.pagination.itemsPerPage,
                usersFilters: this.users.search.filters
            };

            axios.get(`http://localhost:8080/crrt_war/admin/users`, {
                params:{
                    data: searchRequestObject
                }
            })
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

























        // ======== STATS RELATED ======== //
        reloadStats(){
            axios.get('http://localhost:8080/crrt_war/admin/stats')
                .then(function (response) {
                    console.log(response);
                    app.stats = response.data;
                })
                .catch(function (error) {
                    console.log(error);
                });
        },

        // ======== CARS RELATED ======== //

        // ----> Fields validation
        isValidCarFields(prefix){
            let isAllValid = true;
            for(let field in this.cars.workingOn){
                let doesPass = true;
                if(field === "id" || field === "images")
                    continue;

                if(field === "segment" || field === "city"){
                    if(parseInt(app.cars.workingOn[field]) < 1){
                        doesPass = false;
                    }
                }else{
                    if(!(app.cars.workingOn[field]+"").length){
                        doesPass = false;
                    }
                }

                if(!doesPass){
                    app.$refs[prefix+field].classList.add("highlight");
                    isAllValid = false;
                }else{
                    app.$refs[prefix+field].classList.remove("highlight");
                }
            }
            return isAllValid;
        },
        cleanCarFieldHighlight(prefix){
            const fieldsToClean = ["brand", "model", "segment", "price", "city"];

            for(let index in fieldsToClean){
                let field = fieldsToClean[index];
                app.$refs[prefix+field].classList.remove("highlight");
            }
        },

        // ----> For search
        //sc stands for Status chip
        cars_sc_segment(segment){
            this.cars.search.segment = parseInt(this.cars.search.segment) === parseInt(segment) ? 0 : segment;
        },
        cars_sc_city(city){
            this.cars.search.city = parseInt(this.cars.search.city) === parseInt(city) ? 0 : city;
        },

        // ---> Car editing
        openCarEditModal(car_id){
            this.cleanCarFieldHighlight("car_edit-");

            axios.get(`http://localhost:8080/crrt_war/admin/car`, {
                params:{
                    id: car_id
                }
            })
            .then(function (response) {
                console.log(response);
                app.cars.workingOn = response.data;
                $("#carEdit_modal").modal("show");
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },
        focusOnImage(image_id){
            this.cars.imageFocusPrivate.id = this.cars.imageFocusPrivate.id === image_id ? "" : image_id;
        },
        deleteImage(image_id){
            axios.delete('http://localhost:8080/crrt_war/admin/carImages', {
                data: {
                    id: image_id
                }
            })
                .then(function (response) {
                    console.log(response);

                    app.cars.workingOn.images = app.cars.workingOn.images.filter(i => {
                        return i.id !== image_id;
                    });
                    Notiflix.Notify.success(response.data);
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
        },
        deleteCar(){
            axios.delete('http://localhost:8080/crrt_war/admin/car', {
                data: {
                    id: app.cars.workingOn.id
                }
            })
                .then(function (response) {
                    console.log(response);

                    app.cars.list = app.cars.list.filter(i => {
                        return i.id !== app.cars.workingOn.id;
                    });

                    $("#carEdit_modal").modal("hide");

                    Notiflix.Notify.success(response.data);
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
        },
        updateCar(){
            const isAllValid = this.isValidCarFields("car_edit-");

            if(isAllValid){
                axios.put('http://localhost:8080/crrt_war/admin/car', {
                    ...app.cars.workingOn
                })
                    .then(function (response) {
                        app.cleanCarFieldHighlight("car_edit-");
                        console.log(response);

                        delete app.cars.workingOn.images;
                        for(let i in app.cars.list){
                            let current = app.cars.list[i];
                            if(current.id === app.cars.workingOn.id){
                                app.cars.list[i] = JSON.parse(JSON.stringify(app.cars.workingOn));
                                break;
                            }
                        }

                        Notiflix.Notify.success(response.data);
                        $("#carEdit_modal").modal("hide");
                    })
                    .catch(function (error) {
                        console.log(error);
                        Notiflix.Notify.failure(error.response.data);
                    });
            }
        },

        // ---> Car creating
        openCarCreateModal(){
            this.cleanCarFieldHighlight("car_create-");
            Object.assign(this.cars.workingOn, carsWorkingOnObjectProto); // cleaning up
            $("#carCreate_modal").modal("show");
        },
        createCar(){
            // Validating
            const isAllValid = this.isValidCarFields("car_create-");

            if(isAllValid){
                let formData = new FormData();

                let filesInput = this.$refs["create_car-images-input"];

                for(let i = 0; i < filesInput.files.length; i++){
                    formData.append(filesInput.files[i].name, filesInput.files[i]);
                }

                formData.append("document", new Blob([JSON.stringify(app.cars.workingOn)], {type: 'application/json'}));

                axios({
                    method: 'post',
                    url: `http://localhost:8080/crrt_war/admin/car`,
                    data: formData
                })
                    .then(function (response) {
                        app.cleanCarFieldHighlight("car_create-");

                        console.log(response);
                        app.cars.workingOn.id = response.data.carId;
                        delete app.cars.workingOn.images;
                        app.cars.list.push(JSON.parse(JSON.stringify(app.cars.workingOn)));

                        Object.assign(app.cars.workingOn, carsWorkingOnObjectProto);
                        Notiflix.Notify.success(response.data.message);
                        $("#carCreate_modal").modal("hide");
                    })
                    .catch(function (error) {
                        console.log(error);
                        Notiflix.Notify.failure(error.response.data);
                    });
            }
        },
        updateSelectedPhotosCounter(){
            let filesInput = this.$refs["create_car-images-input"];
            this.cars.selectedPhotosNumber = filesInput.files.length;
        },

        // ---> Pagination
        goToCarsPage(pageIndex){
            this.cars.pagination.currentPage = pageIndex;
        },

    }
}).mount('#app');

// Uploading new car photo (to existing entity)
$(document).on('click','#carEdit_modal .mc-add-photo',function (e) {
    $("#carEdit_modal .car-add-photo-input").click();
});
$(document).on("change","#carEdit_modal .car-add-photo-input", function () {
    $("#carEdit_modal .car-add-photo-form").submit();
});
$(document).on('submit','#carEdit_modal .car-add-photo-form', function(e) {
    e.preventDefault();
    const formData = new FormData(this);

    formData.append("document", new Blob([JSON.stringify({car_id: app.cars.workingOn.id})], {type: 'application/json'}));

    axios({
        method: 'post',
        url: `http://localhost:8080/crrt_war/admin/carImages`,
        data: formData
    })
    .then(function (response) {
        app.cars.workingOn.images.push(response.data);
    })
    .catch(function (error) {
        console.log(error);
        Notiflix.Notify.failure(error.response.data);
    });
});

// Uploading new car photos (to new entity)
$(document).on('click','#carCreate_modal .mc-add-photo',function (e) {
    $("#carCreate_modal .car-add-photo-input").click();
});

const filters = {
    email: "",
    firstname: "",
    surname: "",
    patronymic: "",
    role: 0,
    isBlocked: 0
}

function loadUsersList(filters){

}