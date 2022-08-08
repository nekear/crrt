Notiflix.Notify.init({
    position: "center-bottom",
    fontFamily: 'Montserrat',
    closeButton: true,
    failure: {
        background: 'rgba(255,55,95, .2)',
        notiflixIconColor: 'rgb(255,55,95)'
    },

    success: {
        background: 'rgba(48,209,88, .2)',
        notiflixIconColor: 'rgb(48,209,88)'
    },
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