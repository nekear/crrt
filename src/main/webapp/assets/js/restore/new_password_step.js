const { createApp } = Vue;


const app = createApp({
    data() {
        return {
            input_list:{
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
                                type: "min-digits",
                                value: "1"
                            },
                            message: "",
                            isValid: false
                        },
                    ]
                }
            },
            restorePassword: null
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
    computed:{
        allowRestoration: function (){
            return this.doesPassValidation("password", false);
        }
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

        performRestore(){
            const newPassword = this.input_list.password.inputData;
            axios.put('http://localhost:8080/crrt_war/restore', {
                password: newPassword,
                token: jwtToken
            })
            .then(response => {
                console.log(response);

                this.restorePassword = {
                    status: true,
                    message: response.data
                }

                window.location.href = "/crrt_war/login";
            })
            .catch(error => {
                console.log(error);
                this.restorePassword = {
                    status: false,
                    message: error.response.data
                }
            });
        }

    }
}).mount('#app');