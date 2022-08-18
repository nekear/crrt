
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
        background: 'rgba(255,55,95, .2)',
        notiflixIconColor: 'rgb(255,55,95)'
    },

    success: {
        background: 'rgba(48,209,88, .2)',
        notiflixIconColor: 'rgb(48,209,88)'
    },
});

Notiflix.Confirm.init({
   backgroundColor: 'var(--systemGray6_default)',
   titleColor: 'var(--systemGray_accessible)',
   messageColor: 'var(--systemGray_accessible)'
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
