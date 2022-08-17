Notiflix.Notify.init({
    position: "center-bottom",
    fontFamily: 'Montserrat',
    closeButton: true,
    timeout: 3000,

    failure: {
        background: 'rgba(var(--a-systemPink_default), .2)',
        notiflixIconColor: 'var(--systemPink_default)',
        textColor: 'var(--ascentColor)'
    },

    success: {
        background: 'rgba(var(--a-systemGreen_default), .2)',
        notiflixIconColor: 'var(--systemGreen_default)',
        textColor: 'var(--ascentColor)'
    },

    warning: {
        background: 'rgba(var(--a-systemOrange_default), .2)',
        notiflixIconColor: 'var(--systemOrange_default)',
        textColor: 'var(--ascentColor)'
    },

    info: {
        background: 'rgba(var(--a-systemBlue_default), .2)',
        notiflixIconColor: 'var(--systemBlue_default)',
        textColor: 'var(--ascentColor)'
    },
});

Notiflix.Confirm.init({
   backgroundColor: 'var(--systemGray6_default)',
   titleColor: 'var(--systemGray_accessible)',
   messageColor: 'var(--systemGray_accessible)'
});

function loader(action) {
    if(action == true){
        $('.loader').fadeIn();
    }else{
        $('.loader').fadeOut();
    }
}

axios.interceptors.request.use(function (config) {
    console.log(config);

    if(!("silent" in config) || !config.silent)
        loader(true);

    return config;
}, function (error) {
    loader(false);
    return Promise.reject(error);
});

// Add a response interceptor
axios.interceptors.response.use(function (response) {
    loader(false);
    return response;
}, function (error) {
    loader(false);
    return Promise.reject(error);
});

function declOfNum(n, text_forms) {
    n = Math.abs(n) % 100; const n1 = n % 10;
    if (n > 10 && n < 20) { return text_forms[2]; }
    if (n1 > 1 && n1 < 5) { return text_forms[1]; }
    if (n1 == 1) { return text_forms[0]; }
    return text_forms[2];
}