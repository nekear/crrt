const { createApp } = Vue;

var recaptchaLoadCallback = function() {
    grecaptcha.render('recaptchaEl', {
        sitekey : '6LeThTkhAAAAAI7ynKvUTVf_wPPfOc8Lkpz88QNi',
        theme: 'dark',
        callback: function(recaptchaCode){
            app.isRecaptchaSubmitted = true;
        }
    });
};

const app = createApp({
    data() {
        return {
            doesAgreeWithTerms: false,
            isRecaptchaSubmitted: false,
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
                                type: "min-digits",
                                value: "1"
                            },
                            message: "",
                            isValid: false
                        },
                    ]
                }
            }
        }
    },
    created(){
        // Trick for inputs localization
        Object.keys(js_localization.inputs).forEach(key => {

            // Translating checks
            const checks = this.input_list[key].checks;

            checks.forEach((check, index) => {
               check.message = js_localization.inputs[key].checks[index];
            });

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

                this.input_list[key].shouldHighlight = !this.doesPassValidation(key);
            });
        });
    },
    computed: {
        showRecaptcha: function (){
            let isEverythingOkay = true;
            const inputs_list = this.input_list;
            for(let el in inputs_list){
                if(!this.doesPassValidation(el, false)){
                    isEverythingOkay = false;
                    break
                }
            }

            return isEverythingOkay || this.isRecaptchaSubmitted;
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

        registerSubmit(){
            let isEverythingOkay = true;
            const inputs_list = this.input_list;
            Object.keys(inputs_list).forEach(el => {
                const doesPass = this.doesPassValidation(el, false);
                if(!doesPass)
                    isEverythingOkay = false;
                this.input_list[el].shouldHighlight = !doesPass;
            });

            if(isEverythingOkay)
                this.$refs.registrationForm.submit();
        }
    }
}).mount('#app');

const recaptchaFunc = function (code){
    console.log("Code is", code);
}