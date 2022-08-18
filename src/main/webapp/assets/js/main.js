const { createApp } = Vue;

const app = createApp({
   data() {
      return {
         isLoggedIn: false,
         offers:{
            search: {
               collector:{
                  inputValue: null,
                  datesRange: null,
                  segment: 0,
                  city: 0,
                  pricesRange:{
                     min: null,
                     max: null
                  },
                  itemsPerPage: 15,
               },
               filters:{
                  segment: 0,
                  city: 0,
                  pricesRange:{
                     min: null,
                     max: null
                  },
                  tags: [],
                  orderBy: []
               },
               pagination: {
                  currentPage: 1,
                  itemsPerPage: 15
               }
            },
            list: [
               // {
               //    id: "id1=",
               //    brand: "Mercedes",
               //    model: "Benz",
               //    segment: 1,
               //    price: 25,
               //    city: 1
               // }
            ],
            indexedConstraints: []
         },
         cities: {},
         segments: {}
      }
   },
   created(){
      this.isLoggedIn = isLoggedIn;
      Object.assign(this.segments, loaded.segments);
      Object.assign(this.cities, loaded.cities);
   },
   mounted(){
      axios({
         method: "get",
         url: `http://localhost:8080/crrt_war/offers`,
         silent: true
      })
       .then(response => {
          console.log(response);
          this.offers.list = response.data;
       })
       .catch(error => {
          console.log(error);
       });
   },
   computed: {
      offers_list: function () {
         let tags = this.offers.search.filters.tags;

         const filtersRef = this.offers.search.filters;

         const filterCity = parseInt(filtersRef.city);
         const filterSegment = parseInt(filtersRef.segment);
         const filterMinPrice = parseInt(filtersRef.pricesRange.min);
         const filterMaxPrice = parseInt(filtersRef.pricesRange.max);

         let result = this.offers.list.filter(
             offer => {

                let isInIndexedConstraint = true;
                if(this.offers.indexedConstraints.length){
                   isInIndexedConstraint = this.offers.indexedConstraints.includes(offer.id);
                }

                const brand = offer.brand.toLowerCase();
                const model = offer.model.toLowerCase();

                const vehicleName = brand + " " + model;

                let doesIncludesSomeTag = !tags.length;
                for(let tagIndex = 0; tagIndex < tags.length && !doesIncludesSomeTag; tagIndex++){
                   doesIncludesSomeTag = vehicleName.includes(tags[tagIndex]);
                }

                const cityFilterPassed = filterCity ? offer.city ===  filterCity : true;
                const segmentFilterPassed = filterSegment ? offer.segment === filterSegment : true;
                const minFilterPassed = filterMinPrice ? offer.price >= filterMinPrice : true;
                const maxFilterPassed = filterMaxPrice ? offer.price <= filterMaxPrice : true;

                return doesIncludesSomeTag && isInIndexedConstraint && cityFilterPassed && segmentFilterPassed && minFilterPassed && maxFilterPassed;
             }
         );

         // Dynamic sorting by multiple fields
         if(filtersRef.orderBy.length > 0){
            result.sort((a, b) => {
               const name1 = a.brand + " " + a.model;
               const name2 = b.brand + " " + b.model;

               const segment1 = this.segments[a.segment].name;
               const segment2 = this.segments[b.segment].name;

               let result = 0;

               for(let index = 0; index < filtersRef.orderBy.length && result === 0; index++){
                  const order = filtersRef.orderBy[index].type;

                  if(filtersRef.orderBy[index].name === "carName"){
                     result = name1.localeCompare(name2) * (order === "asc" ? 1 : -1);
                  }else if(filtersRef.orderBy[index].name === "segment"){
                     result = segment1.localeCompare(segment2) * (order === "asc" ? 1 : -1);
                  }else if(filtersRef.orderBy[index].name === "price"){
                     result = (a.price - b.price) * (order === "asc" ? 1 : -1);
                  }
               }

               return result;
            });
         }

         return result;
      },
      offers_list_paginated: function(){
         let resultWithPagination = [];

         const currentPage = this.offers.search.pagination.currentPage;
         const itemsPerPage = this.offers.search.pagination.itemsPerPage;
         const result = this.offers_list;

         for(let i = (currentPage-1)*itemsPerPage; i < currentPage*itemsPerPage; i++){
            if(i < result.length){
               resultWithPagination.push(result[i]);
            }
         }

         console.log(result, resultWithPagination);
         return resultWithPagination;
      },

      offers_pages: function(){
         return Math.ceil(this.offers_list.length / this.offers.search.pagination.itemsPerPage);
      },
   },
   methods:{

      addSearchableTag(){
         this.offers.search.filters.tags.push(this.offers.search.collector.inputValue.toLowerCase());
         this.offers.search.collector.inputValue = "";
      },

      removeSearchTag(index){
         this.offers.search.filters.tags.splice(index, 1);
      },


      getPriceLevel(price){
         if(price < 50)
            return 1
         if(price < 75)
            return 2

         return 3
      },

      format(dates){
         const dateStart = dates[0];
         const dateEnd = dates[1];


         return `${this.getFormattedDate(dateStart)} - ${this.getFormattedDate(dateEnd)}`;
      },

      getFormattedDate(date){
         return  dayjs(date).format('DD.MM.YYYY');
      },

      goToOffersPage(pageIndex){
         this.offers.search.pagination.currentPage = pageIndex;
      },

      applyFilters(){
         if(this.offers.search.collector.datesRange != null){
            const start = dayjs(this.offers.search.collector.datesRange[0]).format("YYYY-MM-DD");
            const end = dayjs(this.offers.search.collector.datesRange[1]).format("YYYY-MM-DD");

            axios({
               method: "get",
               url: `http://localhost:8080/crrt_war/filtered_offers`,
               params:{
                  start: start,
                  end: end
               }
            })
             .then(response => {
                  console.log(response);
                  this.offers.indexedConstraints = response.data;
             })
             .catch(error => {
                console.log(error);
             });
         }else{
            this.offers.indexedConstraints = [];
         }

         this.offers.search.filters.segment = this.offers.search.collector.segment;
         this.offers.search.filters.city = this.offers.search.collector.city;
         Object.assign(this.offers.search.filters.pricesRange, this.offers.search.collector.pricesRange);
         this.offers.search.pagination.itemsPerPage = this.offers.search.collector.itemsPerPage;
      },

      getRentRefUrl(rentId){
         let url = new URLSearchParams("");

         url.set("ref", rentId);

         return url.toString();
      },

      redirectToRent(rentId){
         if(this.isLoggedIn)
            window.location.href= "rent?"+this.getRentRefUrl(rentId);
      }
   }
});

app.component("Datepicker", VueDatePicker);
app.component("Sorter", sorterComponent);
app.mount('#app');



$(".find_car-button").on("click", (e) => {
   e.preventDefault();

   $('html, body').animate({
      scrollTop: $("#app").offset().top
   }, 0, 'swing');
});



$(document).on("click", ".copy-item", function(){
   const tooltipContainer = $(this).find(".micro-caution");
   let textToCopy = $(this).find(".content .text").text().trim();
   navigator.clipboard.writeText(textToCopy).then(function() {
      tooltipContainer.css("opacity", 1);
      setTimeout(function (){
         tooltipContainer.css("opacity", 0);
      }, 3000);
   }, function(err) {
      console.error('Async: Could not copy text: ', err);
   });
});


