const { createApp } = Vue;

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*"
}

const locale_input_tips = {
    email_sign:  [
        "Пошта має мати коректний вигляд"
    ],
    pass_sign:[
        "Пароль має містити не менше 4 символів",
        "Пароль має містити хоча б 1 літеру"
    ]
}

createApp({
    data() {
        return {
            validationHighlighter:{
                email_sign: false,
                pass_sign: false
            },

            input_tips:{
                email_sign: {
                    inputData: "",
                    isFocused: false,
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
                pass_sign: {
                    inputData: "",
                    isFocused: false,
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
            }
        }
    },
    created(){
        // Trick for inputs localization
        Object.keys(locale_input_tips).forEach(el => {
            const checks = this.input_tips[el].checks;

            checks.forEach((check, index) => {
               check.message = locale_input_tips[el][index];
            });
        });
    },
    watch:{
        input_tips: { // Watcher to look after inputs` changes
            handler(oldValue, newValue){
                Object.keys(newValue).forEach(key => {
                    const container = newValue[key];
                    const currentValue = container.inputData; // Current value of the changed input

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

                    this.validationHighlighter[key] = this.doesPassValidation(key);
                });
            },
            deep: true
        }
    },
    methods:{
        doesPassValidation(input_id, is_soft = true){
            const currentInput = this.input_tips[input_id];

            if(currentInput){
                // Returning answer will be based on:
                // - input content (does it filled)
                // - do all "high" checks passed

                let shouldHighlight = false;

                if(currentInput.inputData.length > 0){
                    for(let check of currentInput.checks){
                        if(check.configs.level === "high" && !check.isValid) {
                            shouldHighlight = true;
                            break;
                        }
                    }
                }else
                    if(!is_soft)
                        shouldHighlight = true;

                return shouldHighlight;

            }else{
                console.error(`${input_id} in method isValidInput is not defined!`);
                return false;
            }
        },

        login(){
            let isEverythingOkay = true;
            Object.keys(this.validationHighlighter).forEach(el => {
                const doesPass = this.doesPassValidation(el, false);
                if(!doesPass)
                    isEverythingOkay = false;

                this.validationHighlighter[el] = doesPass;
            });
        }
    }
}).mount('#app');