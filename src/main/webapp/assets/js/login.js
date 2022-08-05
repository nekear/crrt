const { createApp } = Vue;

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*",
    "name_pattern": "^[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+$"
}


const app = createApp({
    data() {
        return {
            input_list:{
                email: {
                    inputData: "",

                    isFocused: false,
                    shouldHighlight: false,
                    placeholder: "",
                    type: "email",
                    isNecessary: true,

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
                password: {
                    inputData: "",
                    isFocused: false,

                    shouldHighlight: false,
                    placeholder: "",
                    type: "password",
                    isNecessary: true,
                }
            }
        }
    },
    created(){
        // Trick for inputs localization
        Object.keys(js_localization.inputs).forEach(key => {

            // Translating checks
            const checks = this.input_list[key].checks;

            if(checks){
                checks.forEach((check, index) => {
                    check.message = js_localization.inputs[key].checks[index];
                });
            }

            // Translating placeholders
            this.input_list[key].placeholder = js_localization.inputs[key].placeholder;
        });

        // Setting up watchers

        // Watcher to look after inputs` changes
        Object.keys(this.input_list).forEach(key => {
            this.$watch(`input_list.${key}.inputData`, (newValue, oldValue) => {
                const container = this.input_list[key];
                const currentValue = newValue; // Current value of the changed input

                // Looping through validation "checks" (as I called them) and deciding whether they are satisfied or not
                if("checks" in container){
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
                }

                this.input_list[key].shouldHighlight = !this.doesPassValidation(key);
            });
        });
    },
    methods:{
        doesPassValidation(input_id, is_soft = true){
            const currentInput = this.input_list[input_id];

            if(currentInput){
                // Returning answer will be based on:
                // - input content (does it filled)
                // - do all "high" checks passed
                // - is it necessary to fill the field

                let shouldHighlight = false;

                if(currentInput.inputData.length > 0){
                    if("checks" in currentInput){
                        for(let check of currentInput.checks){
                            if(check.configs.level === "high" && !check.isValid) {
                                shouldHighlight = true;
                                break;
                            }
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
        login(){
            console.log(js_localization);

            let isEverythingOkay = true;
            const inputs_list = this.input_list;
            Object.keys(inputs_list).forEach(el => {
                const doesPass = this.doesPassValidation(el, false);
                if(!doesPass)
                    isEverythingOkay = false;
                this.input_list[el].shouldHighlight = !doesPass;
            });

            if(isEverythingOkay){
                axios.post('http://localhost:8080/crrt_war/login', {
                    email: this.input_list.email.inputData,
                    password: this.input_list.password.inputData
                })
                .then(function (response) {
                    console.log(response);
                    Notiflix.Notify.success(js_localization.notiflix.login_success);
                    document.location.href = "http://localhost:8080/crrt_war/profile";
                })
                .catch(function (error) {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },
        closeAlert(){
            this.error.isVisible = false;
        }
    }
}).mount('#app');