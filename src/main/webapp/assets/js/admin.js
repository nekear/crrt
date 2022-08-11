const { createApp } = Vue;

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*",
    "name_pattern": "^[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+$"
}


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
                        currentPage: 1,
                        totalFoundEntities: 0
                    }
                },
                list: [],
                creation:{
                    input_list:{
                        email: {
                            inputData: "",

                            isFocused: false,
                            shouldHighlight: false,
                            isNecessary: true,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        pattern: "[a-zA-Z_0-9]+\\@[a-zA-Z_0-9]+\\.[a-zA-Z]+"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        firstname: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        surname: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        patronymic: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        password: {
                            inputData: "",
                            isFocused: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "password",
                            isNecessary: true,

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "min-symbols",
                                        value: "4",
                                    },
                                    message: "",
                                    isValid: false
                                },
                                {
                                    configs:{
                                        level: "high",
                                        type: "min-letters",
                                        value: "1"
                                    },
                                    message: "",
                                    isValid: false
                                },
                            ]
                        }
                    },
                    chosenRole: 0
                },
                editing:{
                    originalData:{},
                    input_list:{
                        email: {
                            inputData: "",

                            isFocused: false,
                            shouldHighlight: false,
                            isNecessary: true,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        pattern: "[a-zA-Z_0-9]+\\@[a-zA-Z_0-9]+\\.[a-zA-Z]+"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        firstname: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        surname: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        patronymic: {
                            inputData: "",
                            isFocused: false,
                            isNecessary: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "text",

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "name_pattern"
                                    },
                                    message: "",
                                    isValid: false
                                }
                            ]
                        },
                        password: {
                            inputData: "",
                            isFocused: false,

                            shouldHighlight: false,
                            placeholder: "",
                            type: "password",
                            isNecessary: false,

                            checks: [
                                {
                                    configs:{
                                        level: "high",
                                        type: "min-symbols",
                                        value: "4",
                                    },
                                    message: "",
                                    isValid: false
                                },
                                {
                                    configs:{
                                        level: "high",
                                        type: "min-letters",
                                        value: "1"
                                    },
                                    message: "",
                                    isValid: false
                                },
                            ]
                        }
                    },
                    chosenRole: 0
                }
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

        // Trick for inputs localization
        Object.keys(js_localization.inputs).forEach(key => {

            // Translating checks
            const checksCreation = this.users.creation.input_list[key].checks;
            const checksEditing = this.users.editing.input_list[key].checks;

            checksCreation.forEach((check, index) => {
                check.message = js_localization.inputs[key].checks[index];
            });

            checksEditing.forEach((check, index) => {
                check.message = js_localization.inputs[key].checks[index];
            });

            // Translating placeholders
            this.users.creation.input_list[key].placeholder = js_localization.inputs[key].placeholder;
            this.users.editing.input_list[key].placeholder = js_localization.inputs[key].placeholder;
        });

        // Watcher to look after inputs` changes in user creation modal
        Object.keys(this.users.creation.input_list).forEach(key => {
            this.$watch(`users.creation.input_list.${key}.inputData`, (newValue, oldValue) => {
                const container = this.users.creation.input_list[key];
                const currentValue = newValue; // Current value of the changed input

                // Looping through validation "checks" (as I called them) and deciding whether they are satisfied or not
                container.checks.forEach(check => {
                    const configs = check.configs;
                    // so its type-based (not regex pattern)
                    if("type" in configs){
                        const regexString = validationPatterns[configs.type];
                        if(regexString){
                            const regex = new RegExp(("value" in configs) ? regexString.replace("$v$", configs.value) : regexString); // with replacement, because we want flexibility
                            check.isValid = regex.test(currentValue);
                        }else{
                            console.error(`[${key}] You don't have ${configs.type} specified at validationPatterns!`);
                        }
                    }else{ // me, as a dev, decided to use regex pattern
                        const regex = new RegExp(configs.pattern); // with replacement, because we want flexibility
                        check.isValid = regex.test(currentValue);
                    }
                });

                this.users.creation.input_list[key].shouldHighlight = !this.doesPassValidation(key, "creation");
            });
        });

        // Watcher to look after inputs` changes in user creation modal
        Object.keys(this.users.editing.input_list).forEach(key => {
            this.$watch(`users.editing.input_list.${key}.inputData`, (newValue, oldValue) => {
                const container = this.users.editing.input_list[key];
                const currentValue = newValue; // Current value of the changed input

                // Looping through validation "checks" (as I called them) and deciding whether they are satisfied or not
                container.checks.forEach(check => {
                    const configs = check.configs;
                    // so its type-based (not regex pattern)
                    if("type" in configs){
                        const regexString = validationPatterns[configs.type];
                        if(regexString){
                            const regex = new RegExp(("value" in configs) ? regexString.replace("$v$", configs.value) : regexString); // with replacement, because we want flexibility
                            check.isValid = regex.test(currentValue);
                        }else{
                            console.error(`[${key}] You don't have ${configs.type} specified at validationPatterns!`);
                        }
                    }else{ // me, as a dev, decided to use regex pattern
                        const regex = new RegExp(configs.pattern); // with replacement, because we want flexibility
                        check.isValid = regex.test(currentValue);
                    }
                });

                this.users.editing.input_list[key].shouldHighlight = !this.doesPassValidation(key, "editing");
            });
        });
    },

    watch:{
          "tabs.panel.active"(newState, oldState){
              if(newState === "users"){
                  if(this.users.list.length === 0){
                      this.performUsersSearch(1);
                  }
              }
          },
        "users.list"(newState, oldState){
          for(let key in newState){
              if(!("isChecked" in newState[key])){
                  newState[key].isChecked = false;
              }
          }
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

        userUpdateChangedData: function (){
            const inputListReference = this.users.editing.input_list;

            const email = inputListReference.email.inputData;
            const firstname = inputListReference.firstname.inputData;
            const surname = inputListReference.surname.inputData;
            const patronymic = inputListReference.patronymic.inputData;
            const password = inputListReference.password.inputData;
            const role = parseInt(this.users.editing.chosenRole);

            let resultUpdateData = {
                email: email,
                firstname: firstname,
                surname: surname,
                patronymic: patronymic,
                role: role
            };

            if(password.length)
                resultUpdateData.password = password;

            for(let key in resultUpdateData){
                if(resultUpdateData[key] === this.users.editing.originalData[key]){
                    delete resultUpdateData[key];
                }
            }

            return resultUpdateData;
        }

    },
    methods:{
        // ========= USEFUL FUNCTIONS ========= //
        activateTab(group, tabName){
            this.tabs[group].active = tabName;
        },

        doesPassValidation(input_id, targetName,is_soft = true){
            const currentInput = this.users[targetName].input_list[input_id];

            if(currentInput){
                // Returning answer will be based on:
                // - input content (does it filled)
                // - do all "high" checks passed
                // - is it necessary to fill the field

                let shouldHighlight = false;

                if(currentInput.inputData.length > 0){
                    for(let check of currentInput.checks){
                        if(check.configs.level === "high" && !check.isValid) {
                            shouldHighlight = true;
                            break;
                        }
                    }
                }else
                if(!is_soft && currentInput.isNecessary)
                    shouldHighlight = true;

                return !shouldHighlight;

            }else{
                console.error(`${input_id} in method doesPassValidation is not defined!`);
                return false;
            }
        },
        cleanUpUserModal(target){
            const targetDestination = this.users[target];

            Object.keys(targetDestination.input_list).forEach(key => {
                targetDestination.input_list[key].inputData = "";
                targetDestination.input_list[key].shouldHighlight = false;
            });

            targetDestination.chosenRole = 0;
        },

        // ========= USERS RELATED ========= //

        usersCheckboxAll(event){
          const setState = event.target.checked;

          this.users.list = this.users.list.map(user => {
              user.isChecked = setState;
              return user;
          });
        },

        users_sc_role(role){
            this.users.search.filters.role = parseInt(this.users.search.filters.role) === parseInt(role) ? 0 : parseInt(role);
        },


        performUsersSearch(pageIndex){
            let searchRequestObject = {
                askedPage: pageIndex,
                elementsPerPage: this.users.search.pagination.itemsPerPage,
                usersFilters: this.users.search.filters
            };

            axios.get(`http://localhost:8080/crrt_war/admin/users`, {
                params:{
                    data: searchRequestObject
                }
            })
            .then(function (response) {
                console.log(response.data);
                app.users.search.pagination.currentPage = pageIndex;
                app.users.search.pagination.availablePages = Math.ceil(response.data.totalElements / app.users.search.pagination.itemsPerPage);
                app.users.search.pagination.totalFoundEntities = response.data.totalElements;
                app.users.list = response.data.entities;
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        goToUsersPage(pageIndex) {
            this.performUsersSearch(pageIndex);
        },

        openUserCreateModal(){
            $("#userCreate_modal").modal("show");
        },

        createUser(){
            let doInputsOkay = true;
            const inputs_list = this.users.creation.input_list;
            Object.keys(inputs_list).forEach(el => {
                const doesPass = this.doesPassValidation(el, "creation",false);
                if(!doesPass)
                    doInputsOkay = false;
                this.users.creation.input_list[el].shouldHighlight = !doesPass;
            });

            let doSelectOkay = this.users.creation.chosenRole > 0;

            if(!doSelectOkay){
                this.$refs["user_create-role-select"].classList.add("highlight");
            }else{
                this.$refs["user_create-role-select"].classList.remove("highlight");
            }

            if(doInputsOkay && doSelectOkay){

                let requestUserCreationData = {
                    email: this.users.creation.input_list.email.inputData,
                    password: this.users.creation.input_list.password.inputData,
                    role: this.users.creation.chosenRole
                }

                const firstname = this.users.creation.input_list.firstname.inputData;
                const surname = this.users.creation.input_list.surname.inputData;
                const patronymic = this.users.creation.input_list.patronymic.inputData;

                if(firstname)
                    requestUserCreationData.firstname = firstname;
                if(surname)
                    requestUserCreationData.firstname = surname;
                if(patronymic)
                    requestUserCreationData.firstname = patronymic;

                axios.post('http://localhost:8080/crrt_war/admin/user', {
                    ...requestUserCreationData
                })
                .then(function (response) {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    $("#userCreate_modal").modal("hide");
                    app.performUsersSearch(1);
                    app.cleanUpUserModal("creation");
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        openUserEditModal(user_id){

            axios.get(`http://localhost:8080/crrt_war/admin/user`, {
                params:{
                    id: user_id
                }
            })
            .then(function (response) {
                console.log(response);
                const targetDestination = app.users.editing;

                Object.keys(targetDestination.input_list).forEach(key => {
                    if(key in response.data){
                        targetDestination.input_list[key].inputData = response.data[key];
                    }
                });

                targetDestination.chosenRole = response.data.role;

                app.users.editing.originalData = response.data;

                $("#userEdit_modal").modal("show");
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        setUserState(state){
            axios.put('http://localhost:8080/crrt_war/admin/user/block', {
                id: app.users.editing.originalData.id,
                newState: state
            })
                .then(function (response) {
                    console.log(response);
                    app.users.editing.originalData.state = state;
                    app.performUsersSearch(1);
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
        },

        updateUser(){
            let doInputsOkay = true;
            const inputs_list = this.users.creation.input_list;
            Object.keys(inputs_list).forEach(el => {
                const doesPass = this.doesPassValidation(el, "editing",false);
                if(!doesPass)
                    doInputsOkay = false;
                this.users.editing.input_list[el].shouldHighlight = !doesPass;
            });

            let doSelectOkay = this.users.editing.chosenRole > 0;

            if(!doSelectOkay){
                this.$refs["user_edit-role-select"].classList.add("highlight");
            }else{
                this.$refs["user_edit-role-select"].classList.remove("highlight");
            }

            if(doInputsOkay && doSelectOkay) {

                const changedData = this.userUpdateChangedData;

                axios.put('http://localhost:8080/crrt_war/admin/user', {
                    id: app.users.editing.originalData.id,
                    ...changedData
                })
                .then(function (response) {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    $("#userEdit_modal").modal("hide");
                    app.performUsersSearch(1);
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        deleteSelectedUsers(){
            let selectedUsersIds = [];

            this.users.list.forEach((user, index) => {
               if(user.isChecked)
                   selectedUsersIds.push(user.id);
            });

            if(selectedUsersIds.length > 0){
                const udcl =  users_delete_conf_localization;

                Notiflix.Confirm.show(
                    udcl.title,
                    udcl.desc,
                    udcl.yes,
                    udcl.no,
                    () => {
                        axios.delete('http://localhost:8080/crrt_war/admin/user', {
                            data: {
                                ids: selectedUsersIds
                            }
                        })
                        .then(function (response) {
                            console.log(response);
                            app.performUsersSearch(1);
                        })
                        .catch(function (error) {
                            console.log(error);
                            Notiflix.Notify.failure(error.response.data);
                        });
                    }
                );
            }
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