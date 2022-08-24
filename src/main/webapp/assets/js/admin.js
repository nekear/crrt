
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
                    active: "cars"
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
            invoices: {
                search:{
                    filters: {
                        code: "",
                        carName: "", // brand + model
                        datesRange: null,
                        driverEmail: "",
                        clientEmail: "",
                        status: 0
                    },
                    pagination: {
                        itemsPerPage: 15,
                        availablePages: 1,
                        currentPage: 1,
                        totalFoundEntities: 0
                    },
                    orderBy: []
                },
                list: [],
                details: null,
                repairInvoice:{
                    originId: "",
                    originCode: "",
                    price: {
                        value: 0,
                        isValid: true
                    },
                    comment: "",
                    expirationDate: {
                        date: null,
                        isValid: true
                    }
                },
                rejection:{
                  originCode: null,
                  reason: null
                },
            },
            segments: {},
            cities: {},
            roles: {},
            accountStates:{},
            invoiceStatuses:{},
            stats: []
        }
    },
    created(){
        Object.assign(this.segments, loaded.segments);
        Object.assign(this.cities, loaded.cities);
        Object.assign(this.roles, loaded.roles);
        Object.assign(this.accountStates, loaded.accountStates);
        Object.assign(this.invoiceStatuses, loaded.invoiceStatuses);

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
              if(newState === "invoices"){
                  if(this.invoices.list.length === 0){
                      this.performInvoicesSearch(1);
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
        .then(response => {
            console.log(response);
            this.stats = response.data;
        })
        .catch(error => {
            console.log(error);
        });

        // Getting cars list
        axios({
            method: "get",
            url: `http://localhost:8080/crrt_war/admin/cars`,
            silent: true
        })
        .then(response => {
            console.log(response);
            this.cars.list = response.data;
        })
        .catch(error => {
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
        },

        getMonthStart(){
            return new dayjs().startOf("month").format('DD.MM.YYYY');
        }

    },
    methods:{

        // ========= USEFUL FUNCTIONS ========= //
        activateTab(group, tabName){
            this.tabs[group].active = tabName;
        },

        getFormattedDate(date){
            return  dayjs(date).format('DD.MM.YYYY');
        },

        format(dates){
            const dateStart = dates[0];
            const dateEnd = dates[1];


            return `${this.getFormattedDate(dateStart)} - ${this.getFormattedDate(dateEnd)}`;
        },





        // ========= INVOICES RELATED ========= //

        getSortOrder(name){
            let sortOrder = "null";
            for(index in this.invoices.search.orderBy){
                if(this.invoices.search.orderBy[index].name === name){
                    sortOrder = this.invoices.search.orderBy[index].type;
                    break;
                }
            }
            return sortOrder;
        },

        getSortIndex(name){
            let sortIndex = null;
            for(index in this.invoices.search.orderBy){
                if(this.invoices.search.orderBy[index].name === name){
                    sortIndex = index;
                    break;
                }
            }
            return sortIndex ? parseInt(sortIndex) + 1 : null;
        },

        increaseSort(name){
            console.log('increasing!');
            let wasFound = false;
            for(index in this.invoices.search.orderBy){
                let current = this.invoices.search.orderBy[index];
                if(current.name === name){
                    if(current.type === "asc"){
                        current.type = "desc";
                    }else if(current.type === "desc"){
                        this.invoices.search.orderBy.splice(index, 1);
                    }
                    wasFound = true;
                    break;
                }
            }

            if(!wasFound){
                this.invoices.search.orderBy.push({name: name, type: "asc"});
            }
        },

        doesInvoiceDateExpired(date){
            return dayjs(date, "YYYY-MM-DD").add(2, 'day').isBefore(dayjs());
        },




        performInvoicesSearch(pageIndex){
            let searchRequestObject = {
                askedPage: pageIndex,
                elementsPerPage: this.invoices.search.pagination.itemsPerPage,
                invoicesFilters: {},
            };

            Object.assign(searchRequestObject.invoicesFilters, this.invoices.search.filters);

            delete searchRequestObject.invoicesFilters.datesRange;

            if(this.invoices.search.filters.datesRange != null){
                searchRequestObject.invoicesFilters.datesRange = {};
                searchRequestObject.invoicesFilters.datesRange.start = dayjs(this.invoices.search.filters.datesRange[0]).format("YYYY-MM-DD");
                searchRequestObject.invoicesFilters.datesRange.end = dayjs(this.invoices.search.filters.datesRange[1]).format("YYYY-MM-DD");
            }

            if(this.invoices.search.orderBy.length){
                searchRequestObject.invoicesFilters.orderBy = this.invoices.search.orderBy;
            }

            console.log(searchRequestObject);

            axios.get(`http://localhost:8080/crrt_war/manage/invoices`, {
                params:{
                    data: searchRequestObject,
                }
            })
            .then(response => {
                console.log(response);
                this.invoices.search.pagination.currentPage = pageIndex;
                this.invoices.search.pagination.availablePages = Math.ceil(response.data.totalElements / this.invoices.search.pagination.itemsPerPage);
                this.invoices.search.pagination.totalFoundEntities = response.data.totalElements;
                this.invoices.list = response.data.entities;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        goToInvoicesPage(pageIndex){
            this.performInvoicesSearch(pageIndex);
        },


        openInvoiceDetailsModal(invoice_id){
            console.log(invoice_id);
            axios.get(`http://localhost:8080/crrt_war/manage/invoice`, {
                params:{
                    invoice_id: invoice_id
                }
            })
            .then(response => {
                console.log(response);
                $("#invoiceDetails_modal").modal("show");
                this.invoices.details = response.data;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });

        },

        deleteRepairInvoice(repairInvoiceId){
            axios.delete('http://localhost:8080/crrt_war/manage/repairInvoice', {
                data: {
                    repairId: repairInvoiceId
                }
            })
            .then(response => {
                console.log(response);
                this.invoices.details = response.data;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        createRepairInvoice(){
            let isAllValid = true;

            if(this.invoices.repairInvoice.price.value < 1){
                this.invoices.repairInvoice.price.isValid = false;
                isAllValid = false;
            }

            if(!this.invoices.repairInvoice.expirationDate.date){
                this.invoices.repairInvoice.expirationDate.isValid = false;
                isAllValid = false;
            }

            if(isAllValid){

                axios.post('http://localhost:8080/crrt_war/manage/repairInvoice', {
                    originId: this.invoices.repairInvoice.originId,
                    price: this.invoices.repairInvoice.price.value,
                    expirationDate: dayjs(this.invoices.repairInvoice.expirationDate.date).format("YYYY-MM-DD"),
                    comment: _(this.invoices.repairInvoice.comment)
                })
                .then(response => {
                    console.log(response);
                    this.invoices.details = response.data;
                    $("#createRepairInvoice_modal").modal("hide");
                    $("#invoiceDetails_modal").modal("show");
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        openCreateRepairInvoiceModal(){
            this.invoices.repairInvoice.price = {
                value: "",
                isValid: true
            }

            this.invoices.repairInvoice.expirationDate = {
                date: null,
                isValid: true
            }

            this.invoices.repairInvoice.comment = "";

            this.invoices.repairInvoice.originId = this.invoices.details.id;
            this.invoices.repairInvoice.originCode = this.invoices.details.code;

            $("#invoiceDetails_modal").modal("hide");
            $("#createRepairInvoice_modal").modal("show");
        },

        closeCreateRepairInvoiceModal(){
            $("#invoiceDetails_modal").modal("show");
            $("#createRepairInvoice_modal").modal("hide");
        },


        openRejectInvoiceModal(){
            this.invoices.rejection.originCode = this.invoices.details.code;
            this.invoices.rejection.reason = null;

            $("#rejectInvoice_modal").modal("show");
        },

        closeRejectInvoiceModal(){
            $("#invoiceDetails_modal").modal("show");
        },

        rejectInvoice(){
            const rejectionReason = _(this.invoices.rejection.reason);

            axios.delete('http://localhost:8080/crrt_war/manage/invoice', {
                data: {
                    id: this.invoices.details.id,
                    reason: rejectionReason
                }
            })
            .then(response => {
                console.log(response);
                this.invoices.details = response.data;
                $("#rejectInvoice_modal").modal("hide");
                $("#invoiceDetails_modal").modal("show");
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        generateInvoicesReport(){
            axios({
                method: "get",
                url: 'http://localhost:8080/crrt_war/manage/invoices/report',
                responseType: 'blob'
            })
            .then(response => {
                const fileName = response.headers["content-disposition"].split('filename=')[1].split(';')[0];
                const downloadUrl = window.URL.createObjectURL(new Blob([response.data]));

                const link = document.createElement('a');

                link.href = downloadUrl;

                link.setAttribute('download', fileName.substring(1, fileName.length-1)); //any other extension

                document.body.appendChild(link);

                link.click();

                link.remove();
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },



        // ========= USERS RELATED ========= //

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

        // ---> Checking all checkboxes
        usersCheckboxAll(event){
          const setState = event.target.checked;

          this.users.list = this.users.list.map(user => {
              user.isChecked = setState;
              return user;
          });
        },

        // ---> Useful thing to fastly set role filter
        users_sc_role(role){
            this.users.search.filters.role = parseInt(this.users.search.filters.role) === parseInt(role) ? 0 : parseInt(role);
        },


        // ---> Loading users list
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
            .then(response => {
                this.users.search.pagination.currentPage = pageIndex;
                this.users.search.pagination.availablePages = Math.ceil(response.data.totalElements / this.users.search.pagination.itemsPerPage);
                this.users.search.pagination.totalFoundEntities = response.data.totalElements;
                this.users.list = response.data.entities;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        // ---> For pagination
        goToUsersPage(pageIndex) {
            this.performUsersSearch(pageIndex);
        },

        // ---> Creating new user
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
                .then(response => {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    $("#userCreate_modal").modal("hide");
                    this.performUsersSearch(1);
                    this.cleanUpUserModal("creation");
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        // ---> Updating user
        openUserEditModal(user_id){

            axios.get(`http://localhost:8080/crrt_war/admin/user`, {
                params:{
                    id: user_id
                }
            })
            .then(response => {
                console.log(response);
                const targetDestination = this.users.editing;

                Object.keys(targetDestination.input_list).forEach(key => {
                    if(key in response.data){
                        targetDestination.input_list[key].inputData = response.data[key];
                    }
                });

                targetDestination.chosenRole = response.data.role;

                this.users.editing.originalData = response.data;

                $("#userEdit_modal").modal("show");
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },
        setUserState(state){
            axios.put('http://localhost:8080/crrt_war/admin/user/block', {
                id: this.users.editing.originalData.id,
                newState: state
            })
            .then(response => {
                console.log(response);
                this.users.editing.originalData.state = state;
                this.performUsersSearch(1);
            })
            .catch(error => {
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
                    id: this.users.editing.originalData.id,
                    ...changedData
                })
                .then(response => {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    $("#userEdit_modal").modal("hide");
                    this.performUsersSearch(1);
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        // ---> Deleting user
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
                        .then(response => {
                            console.log(response);
                            this.performUsersSearch(1);
                        })
                        .catch(error => {
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
                .then(response => {
                    console.log(response);
                    this.stats = response.data;
                })
                .catch(error => {
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
                    if(parseInt(this.cars.workingOn[field]) < 1){
                        doesPass = false;
                    }
                }else{
                    if(!(this.cars.workingOn[field]+"").length){
                        doesPass = false;
                    }
                }

                if(!doesPass){
                    this.$refs[prefix+field].classList.add("highlight");
                    isAllValid = false;
                }else{
                    this.$refs[prefix+field].classList.remove("highlight");
                }
            }
            return isAllValid;
        },
        cleanCarFieldHighlight(prefix){
            const fieldsToClean = ["brand", "model", "segment", "price", "city"];

            for(let index in fieldsToClean){
                let field = fieldsToClean[index];
                this.$refs[prefix+field].classList.remove("highlight");
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
            .then(response => {
                console.log(response);
                this.cars.workingOn = response.data;
                $("#carEdit_modal").modal("show");
            })
            .catch(error => {
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
            .then(response => {
                console.log(response);

                this.cars.workingOn.images = this.cars.workingOn.images.filter(i => {
                    return i.id !== image_id;
                });
                Notiflix.Notify.success(response.data);
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },
        deleteCar(){
            axios.delete('http://localhost:8080/crrt_war/admin/car', {
                data: {
                    id: this.cars.workingOn.id
                }
            })
                .then(response => {
                    console.log(response);

                    this.cars.list = this.cars.list.filter(i => {
                        return i.id !== this.cars.workingOn.id;
                    });

                    $("#carEdit_modal").modal("hide");

                    Notiflix.Notify.success(response.data);
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
        },
        updateCar(){
            const isAllValid = this.isValidCarFields("car_edit-");

            if(isAllValid){
                axios.put('http://localhost:8080/crrt_war/admin/car', {
                    ...this.cars.workingOn
                })
                    .then(response => {
                        this.cleanCarFieldHighlight("car_edit-");
                        console.log(response);

                        delete this.cars.workingOn.images;
                        for(let i in this.cars.list){
                            let current = this.cars.list[i];
                            if(current.id === this.cars.workingOn.id){
                                this.cars.list[i] = JSON.parse(JSON.stringify(this.cars.workingOn));
                                break;
                            }
                        }

                        Notiflix.Notify.success(response.data);
                        $("#carEdit_modal").modal("hide");
                    })
                    .catch(error => {
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


                formData.append("document", new Blob([JSON.stringify(this.cars.workingOn)], {type: 'application/json'}));

                axios({
                    method: 'post',
                    url: `http://localhost:8080/crrt_war/admin/car`,
                    data: formData
                })
                    .then(response => {
                        this.cleanCarFieldHighlight("car_create-");

                        console.log(response);
                        this.cars.workingOn.id = response.data.carId;
                        delete this.cars.workingOn.images;
                        this.cars.list.push(JSON.parse(JSON.stringify(this.cars.workingOn)));

                        Object.assign(this.cars.workingOn, carsWorkingOnObjectProto);
                        Notiflix.Notify.success(response.data.message);
                        $("#carCreate_modal").modal("hide");
                    })
                    .catch(error => {
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
});
app.component("Datepicker", VueDatePicker);
app.component("Sorter", sorterComponent);
const vm = app.mount('#app');

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

    console.log(vm.cars);

    formData.append("document", new Blob([JSON.stringify({car_id: vm.cars.workingOn.id})], {type: 'application/json'}));

    axios({
        method: 'post',
        url: `http://localhost:8080/crrt_war/admin/carImages`,
        data: formData
    })
    .then(function (response) {
        if(!vm.cars.workingOn.images)
            vm.cars.workingOn.images = [];

        vm.cars.workingOn.images.push(response.data);
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

function _(str){
    if(!str)
        return null;

    return str.trim().length > 0 ? str.trim() : null;
}

$(document).on("hide.bs.modal", "#invoiceDetails_modal", function (e){
    vm.performInvoicesSearch(vm.invoices.search.pagination.currentPage);
});