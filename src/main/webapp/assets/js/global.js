
// Vue components
const sorterComponent = {
    props: ["name", "orderBy"],
    data() {
        return {
            count: 0
        }
    },

    methods: {
        getSortOrder(name){
            let sortOrder = "null";
            for(let index in this.orderBy){
                if(this.orderBy[index].name === name){
                    sortOrder = this.orderBy[index].type;
                    break;
                }
            }
            return sortOrder;
        },

        getSortIndex(name){
            let sortIndex = null;
            for(let index in this.orderBy){
                if(this.orderBy[index].name === name){
                    sortIndex = index;
                    break;
                }
            }
            return sortIndex ? parseInt(sortIndex) + 1 : null;
        },

        increaseSort(name){
            let wasFound = false;
            for(let index in this.orderBy){
                let current = this.orderBy[index];
                if(current.name === name){
                    if(current.type === "asc"){
                        current.type = "desc";
                    }else if(current.type === "desc"){
                        this.orderBy.splice(index, 1);
                    }
                    wasFound = true;
                    break;
                }
            }

            if(!wasFound){
                this.orderBy.push({name: name, type: "asc"});
            }
        },
    },

    template:   `<div class="sort-marker" :data-sort-type="getSortOrder(name)" @click="increaseSort(name)">
                     <div class="sort-wrap">
                         <div class="sort-asc-arrow"></div>
                         <div v-if="orderBy.length > 1">{{getSortIndex(name)}}</div>
                         <div class="sort-desc-arrow"></div>
                     </div>
                 </div>`
};

// Notiflix styles

Notiflix.Notify.init({
    position: "center-bottom",
    fontFamily: 'Montserrat',
    closeButton: true,
    timeout: 3000,

    failure: {
        background: 'rgba(var(--a-systemPink_default), .4)',
        notiflixIconColor: 'var(--systemPink_default)',
        textColor: 'var(--ascentColor)'
    },

    success: {
        background: 'rgba(var(--a-systemGreen_default), .4)',
        notiflixIconColor: 'var(--systemGreen_default)',
        textColor: 'var(--ascentColor)'
    },

    warning: {
        background: 'rgba(var(--a-systemOrange_default), .4)',
        notiflixIconColor: 'var(--systemOrange_default)',
        textColor: 'var(--ascentColor)'
    },

    info: {
        background: 'rgba(var(--a-systemBlue_default), .4)',
        notiflixIconColor: 'var(--systemBlue_default)',
        textColor: 'var(--ascentColor)'
    },
});

Notiflix.Confirm.init({
   backgroundColor: 'var(--systemGray6_default)',
   titleColor: 'var(--systemGray_accessible)',
   messageColor: 'var(--systemGray_accessible)'
});


$(document).on("change", "#theme-checkbox", function (e) {
    const currentElIsChecked = $(this).is(':checked');

    let theme_name = currentElIsChecked ? "white_theme" : "dark_theme";

    $("body").addClass("effect_transitions");

    $("link[href*='/css/themes/']").each(function(i, el){
        const currentHref = el.href;
        const splitHref = currentHref.split("/");

        splitHref.splice(splitHref.length-1, 1); // deleting theme name
        splitHref.push(theme_name+".css");


        el.href = splitHref.join("/");
    });

    setTimeout(() => {
        $("body").removeClass("effect_transitions");
    }, 250);


    axios({
            method: "put",
            url: 'http://localhost:8080/crrt_war/profile/theme',
            silent: true
        })
        .then(response => {
            console.log(response);
        })
        .catch(error => {
            console.log(error);
        });
});

// Loader settings
function loader(action) {
    if(action == true){
        $('.loader').fadeIn();
    }else{
        $('.loader').fadeOut();
    }
}

// Axios middleware for showing / hiding loader + ability to pass "silent" parameter to hide loader on request
axios.interceptors.request.use(function (config) {
    console.log(config);

    if(!("silent" in config) || !config.silent)
        loader(true);

    return config;
}, function (error) {
    loader(false);
    return Promise.reject(error);
});

axios.interceptors.response.use(function (response) {
    loader(false);
    return response;
}, function (error) {
    loader(false);
    return Promise.reject(error);
});

// Method for getting right word endings
function declOfNum(n, text_forms) {
    n = Math.abs(n) % 100; const n1 = n % 10;
    if (n > 10 && n < 20) { return text_forms[2]; }
    if (n1 > 1 && n1 < 5) { return text_forms[1]; }
    if (n1 == 1) { return text_forms[0]; }
    return text_forms[2];
}
