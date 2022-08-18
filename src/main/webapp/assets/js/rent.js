const { createApp } = Vue;

dayjs.extend(window.dayjs_plugin_customParseFormat);

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*",
    "name_pattern": "^[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+$"
}

const js_localization = {
    inputs: {
        firstname: {
            "placeholder": "Firstname",
            checks: [

            ]
        }

    }
}


const app = createApp({
    data() {
        return {
            rent: {
                id: "fdasfas=",
                brand: "Mercedes",
                model: "Amg g7",
                city: 1,
                segment: 1,
                price: 1000,
                images: [
                    {
                        id: "adsf324",
                        fileName: 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1740&q=80',
                    },
                    {
                        id: "adsf324",
                        fileName: 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1650&q=80'
                    }
                ]
            },
            datepickerRef: null,
            clientData:{
                datesRange: "",
                isWithDriver: false,
                input_list:{
                    firstname: {
                        inputData: "",
                        isFocused: false,
                        isNecessary: true,

                        shouldHighlight: false,
                        placeholder: "Firstname",
                        type: "text",

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    type: "name_pattern"
                                },
                                message: "Firstname should have correct format",
                                isValid: false
                            }
                        ]
                    },
                    surname: {
                        inputData: "",
                        isFocused: false,
                        isNecessary: true,

                        shouldHighlight: false,
                        placeholder: "Surname",
                        type: "text",

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    type: "name_pattern"
                                },
                                message: "Surname should have correct format",
                                isValid: false
                            }
                        ]
                    },
                    patronymic: {
                        inputData: "",
                        isFocused: false,
                        isNecessary: true,

                        shouldHighlight: false,
                        placeholder: "Patronymic",
                        type: "text",

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    type: "name_pattern"
                                },
                                message: "Patronymic should have correct format",
                                isValid: false
                            }
                        ]
                    },
                    date_of_birth: {
                        inputData: "",
                        isFocused: false,

                        shouldHighlight: false,
                        placeholder: "Date of birth",
                        type: "date",
                        isNecessary: true,

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    pattern: "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])"
                                },
                                message: "Date of birth should have correct format",
                                isValid: false
                            },
                        ]
                    },
                    date_of_issue: {
                        inputData: "",
                        isFocused: false,

                        shouldHighlight: false,
                        placeholder: "Date of issue",
                        type: "date",
                        isNecessary: true,

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    pattern: "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])"
                                },
                                message: "Date of issue should have correct format",
                                isValid: false
                            },
                        ]
                    },
                    doc_number: {
                        inputData: "",
                        isFocused: false,

                        shouldHighlight: false,
                        placeholder: "Document number",
                        type: "number",
                        isNecessary: true,

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    pattern: "^\\d{9}$"
                                },
                                message: "Document number must contain 9 numbers",
                                isValid: false
                            },
                        ]
                    },
                    rntrc: {
                        inputData: "",
                        isFocused: false,

                        shouldHighlight: false,
                        placeholder: "RNTRC",
                        type: "number",
                        isNecessary: true,

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    pattern: "^\\d{10}$"
                                },
                                message: "RNTRC must contain 10 numbers",
                                isValid: false
                            },
                        ]
                    },
                    authority: {
                        inputData: "",
                        isFocused: false,

                        shouldHighlight: false,
                        placeholder: "Authority",
                        type: "number",
                        isNecessary: true,

                        checks: [
                            {
                                configs:{
                                    level: "high",
                                    pattern: "^\\d{4}$"
                                },
                                message: "Authority must contain 4 numbers",
                                isValid: false
                            },
                        ]
                    }
                },
            },
            cities: {
                0: {
                    name: "All cities"
                },
                1: {
                    name: "Kyiv"
                },
                2: {
                    name: "Lviv"
                }
            },
            finalPrice: null,
            segments: {
                0: {
                    name: "All segments"
                },
                1: {
                    name: "E-segment"
                },
                2: {
                    name: "F-segment"
                },
                3: {
                    name: "S-segment"
                }
            },
            user: {
                balance: 0
            }
        }
    },
    created(){
        this.user.balance = userBalance;

        // Trick for inputs localization
        // Object.keys(js_localization.inputs).forEach(key => {
        //
        //     // Translating checks
        //     const checks = this.input_list[key].checks;
        //
        //     checks.forEach((check, index) => {
        //         check.message = js_localization.inputs[key].checks[index];
        //     });
        //
        //     // Translating placeholders
        //     this.input_list[key].placeholder = js_localization.inputs[key].placeholder;
        // });

        // Watcher to look after inputs` changes in user creation modal
        Object.keys(this.clientData.input_list).forEach(key => {
            this.$watch(`clientData.input_list.${key}.inputData`, (newValue, oldValue) => {
                const container = this.clientData.input_list[key];
                const currentValue = newValue.toString(); // Current value of the changed input

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

                this.clientData.input_list[key].shouldHighlight = !this.doesPassValidation(key);
            });
        });
    },

    mounted(){

        let disabledDates = [];

        if(carData){
            this.rent = carData.data;
            disabledDates = carData.disabledDates;
        }

        var input = document.getElementById('rent-range-datepicker');
        this.datepickerRef = new HotelDatepicker(input, {
            format: "DD.MM.YYYY",
            startOfWeek: "monday",
            endDate: new Date().setMonth(new Date().getMonth() + 2),
            disabledDates: disabledDates,
            clearButton: true,
            showTopbar: false,
            i18n: {
                selected: 'Your rent:',
                night: 'Day',
                nights: 'Days',
                button: 'Close',
                clearButton: 'Clear',
                submitButton: 'Submit',
                'checkin-disabled': 'Renting disabled',
                'checkout-disabled': 'Renting disabled',
                'day-names-short': ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
                'day-names': ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
                'month-names-short': ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                'month-names': ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                'error-more': 'Date range should not be more than 1 night',
                'error-more-plural': 'Date range should not be more than %d nights',
                'error-less': 'Date range should not be less than 1 night',
                'error-less-plural': 'Date range should not be less than %d nights',
                'info-more': 'Please select a date range of at least 1 night',
                'info-more-plural': 'Please select a date range of at least %d nights',
                'info-range': 'Please select a date range between %d and %d nights',
                'info-range-equal': 'Please select a date range of %d nights',
                'info-default': 'Please select a date range'
            },
            onSelectRange: function() {
                console.log("Yeah!");
                vm.setRantingDates();
            }
        });

    },
    computed: {

    },
    methods:{

        async setWithDriverStatus($event){
            const status = $event.target.checked;

            const dateStart = this.clientData.datesRange.start;
            const dateEnd = this.clientData.datesRange.end;

            let resultStatus = false;

            if(status && dateStart && dateEnd){
                const formattedStart = dayjs(dateStart).format("YYYY-MM-DD");
                const formattedEnd = dayjs(dateEnd).format("YYYY-MM-DD");

                await axios.get(`http://localhost:8080/crrt_war/rent/data`, {
                    params:{
                        start: formattedStart,
                        end: formattedEnd,
                        city: this.rent.city
                    }
                })
                .then(response => {
                    console.log(response);
                    if(response.data){
                        resultStatus = true;
                    }else{
                        Notiflix.Notify.info("Sorry, but we don`t have any available drivers on the specified dates interval!");
                    }
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }

            this.clientData.isWithDriver = resultStatus;
            $event.target.checked = resultStatus;
        },

        setRantingDates(){
            const datepickerInput = document.getElementById("rent-range-datepicker");
            datepickerInput.classList.remove("highlight");
            const dates = datepickerInput.value.split(" - ");

            const dateStart = dayjs(dates[0], "DD.MM.YYYY");
            const dateEnd = dayjs(dates[1], "DD.MM.YYYY");

            this.clientData.datesRange = {
                start: dateStart,
                end: dateEnd
            }

            this.finalPrice = (this.datepickerRef.getNights() + 1) * this.rent.price;
        },
        doesPassValidation(input_id, is_soft = true){
            const currentInput = this.clientData.input_list[input_id];

            if(currentInput){
                // Returning answer will be based on:
                // - input content (does it filled)
                // - do all "high" checks passed
                // - is it necessary to fill the field

                let shouldHighlight = false;

                if(currentInput.inputData.toString().length > 0){
                    for(let check of currentInput.checks){
                        if(check.configs.level === "high" && !check.isValid) {
                            shouldHighlight = true;
                            break;
                        }
                    }
                }else{
                    if(!is_soft && currentInput.isNecessary)
                        shouldHighlight = true;
                }

                return !shouldHighlight;

            }else{
                console.error(`${input_id} in method doesPassValidation is not defined!`);
                return false;
            }
        },

        getPriceLevel(price){
            if(price < 50)
                return 1
            if(price < 75)
                return 2

            return 3
        },

        payInvoice(){
            let isEverythingOkay = true;
            const inputs_list = this.clientData.input_list;

            const resPassportData = {};

            Object.keys(inputs_list).forEach(el => {
                const doesPass = this.doesPassValidation(el, false);
                resPassportData[el] = this.clientData.input_list[el].inputData;
                if(!doesPass)
                    isEverythingOkay = false;
                this.clientData.input_list[el].shouldHighlight = !doesPass;
            });


            console.log(resPassportData);

            if(isEverythingOkay){
                const formattedStart = dayjs(this.clientData.datesRange.start).format("YYYY-MM-DD");
                const formattedEnd = dayjs(this.clientData.datesRange.end).format("YYYY-MM-DD");

                const finalRentData = {
                    passport: resPassportData,
                    datesRange: {
                        start: formattedStart,
                        end: formattedEnd
                    },
                    isWithDriver: this.clientData.isWithDriver,
                    carId: this.rent.id
                }

                axios.post('http://localhost:8080/crrt_war/rent/data', {
                    ...finalRentData
                })
                .then(function (response) {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    window.location.href = "http://localhost:8080/crrt_war/client";
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        }

    }
});


const vm = app.mount('#app');