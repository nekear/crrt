const { createApp } = Vue;

const validationPatterns = {
    "min-symbols": "\\w{$v$,}",
    "min-letters": ".*[a-zA-Z]{$v$,}.*",
    "name_pattern": "^[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+$"
}
const loaded = {
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
    cities: {
        0:{
            name: "All cities"
        },
        1: {
            name: "Kyiv"
        },
        2: {
            name: "Lviv"
        }
    }
}

const carsWorkingOnObjectProto = {
    id: "",
    photos: [
        {
            id: "1",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "2",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "3",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "4",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "5",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "6",
            file: "imgs/car_preview.jpg"
        },
        {
            id: "7",
            file: "imgs/car_preview.jpg"
        },
    ], // for editing
    loadedPhotosPackNumber: 0, // for creation
    brand: "",
    model: "",
    segment: 0,
    price: "",
    city: 0
};

const app = createApp({
    data() {
        return {
            cars:{
                photoFocusPrivate: {
                  id: ""
                },
                workingOn: {},
                search: {
                    brand: "",
                    model: "",
                    segment: 0,
                    price: "",
                    city: 0
                },
                list: [
                    {
                        id: 1,
                        brand: "Mercedes",
                        model: "Benz C122 G9",
                        segment: 1,
                        price: 2000,
                        city: 1
                    },
                    {
                        id: 2,
                        brand: "Audi",
                        model: "G9",
                        segment: 2,
                        price: 3000,
                        city: 2
                    }
                ]
            },
            segments: {},
            cities: {},
            stats: []
        }
    },
    created(){
        Object.assign(this.segments, loaded.segments);
        Object.assign(this.cities, loaded.cities);

        // Assigning prototype
        Object.assign(this.cars.workingOn, carsWorkingOnObjectProto);
    },

    mounted(){
        // Getting stats
        axios({
            method: "get",
            url: 'http://localhost:8080/crrt_war/admin/stats',
            silent: true
        })
        .then(function (response) {
            console.log(response);
            app.stats = response.data;
        })
        .catch(function (error) {
            console.log(error);
        });

        // Getting cars list
        axios({
            method: "get",
            url: `http://localhost:8080/crrt_war/admin/cars`,
            silent: true
        })
        .then(function (response) {
            console.log(response);
            app.cars.list = response.data;
        })
        .catch(function (error) {
            console.log(error);
        });
    },

    computed: {
        cars_list: function () {
            const searchFilters = this.cars.search;
            const result = this.cars.list.filter(
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

            console.log(result);

            return result;

        },
        focusedPhoto: function (){
            if(this.cars.photoFocusPrivate.id){
                let photo = null;

                for(let item in this.cars.workingOn.photos){
                    let current = this.cars.workingOn.photos[item];
                    if(current.id === this.cars.photoFocusPrivate.id){
                        photo = current;
                        break;
                    }
                }

                return photo;
            }
        },
    },
    methods:{
        //sc stands for Status chip
        cars_sc_segment(segment){
            this.cars.search.segment = parseInt(this.cars.search.segment) === parseInt(segment) ? 0 : segment;
        },
        cars_sc_city(city){
            this.cars.search.city = parseInt(this.cars.search.city) === parseInt(city) ? 0 : city;
        },

        // Car editing
        openCarEditModal(car_id){
            $("#carEdit_modal").modal("show");
        },
        openCarCreateModal(){
            loader(true);
            $("#carCreate_modal").modal("show");
        },

        focusOnPhoto(photo_id){
            this.cars.photoFocusPrivate.id = this.cars.photoFocusPrivate.id === photo_id ? "" : photo_id;
        },

        reloadStats(){
            axios.get('http://localhost:8080/crrt_war/admin/stats')
                .then(function (response) {
                    console.log(response);
                    app.stats = response.data;
                })
                .catch(function (error) {
                    console.log(error);
                });
        }


    }
}).mount('#app');

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
    console.log("Going to submit!");
});

// Uploading new car photos (to new entity)
$(document).on('click','#carCreate_modal .mc-add-photo',function (e) {
    $("#carCreate_modal .car-add-photo-input").click();
});
$(document).on("change","#carCreate_modal .car-add-photo-input", function () {
    $("#carCreate_modal .car-add-photo-form").submit();
});
$(document).on('submit','#carCreate_modal .car-add-photo-form', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    app.cars.workingOn.loadedPhotosPackNumber = $("#carCreate_modal .car-add-photo-input").get(0).files.length;
    console.log(formData);
});