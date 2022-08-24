const { createApp } = Vue;

dayjs.extend(window.dayjs_plugin_isBetween);
dayjs.extend(window.dayjs_plugin_customParseFormat);
dayjs.extend(window.dayjs_plugin_isSameOrAfter);
dayjs.extend(window.dayjs_plugin_isSameOrBefore);



const app = createApp({
    data() {
        return {
            selectedCity: 1,
            currentCity: 1,
            invoices:{
                search:{
                    filters: {
                        value: ""
                    },
                    pagination: {
                        itemsPerPage: 15,
                        currentPage: 1
                    }
                },
                list: [],
                details: null
            },
            invoiceStatuses:{},
            cities: {}
        }
    },
    created(){
        Object.assign(this.cities, loaded.cities);
        Object.assign(this.invoiceStatuses, loaded.invoiceStatuses);

        this.currentCity = currentCityId;
        this.selectedCity = currentCityId;
    },
    mounted(){
        // Getting invoices list (without loaded = in silent mode)
        axios({
            method: "get",
            url: `http://localhost:8080/crrt_war/driver/invoices`,
            silent: true
        })
        .then(response => {
            console.log(response);
            this.invoices.list = response.data;
        })
        .catch(error => {
            console.log(error);
        });
    },

    watch:{
        'invoices.list': {
            handler (newValue, oldValue){
                const listRef = newValue;
                Object.keys(newValue).forEach(key => {
                    // Adding additional info (done mostly for caching purposes)
                    const additions = {};
                    additions.isAbleToSkip = this.isInvoiceSkipAvailable(listRef[key]);
                    additions.daysDiff = this.daysDiffData(listRef[key].datesRange.start);
                    additions.daysBetween = this.getDaysBetweenDates(listRef[key].datesRange.start, listRef[key].datesRange.end);
                    additions.isActive = this.isActiveInvoice(listRef[key].datesRange.start, listRef[key].datesRange.end) && !listRef[key].statusList.includes(2) && !listRef[key].statusList.includes(3);
                    listRef[key].additions = additions;
                });
            }
        }
    },

    computed: {

        invoices_list: function () {
            let searchValue = this.invoices.search.filters.value;

            if(!searchValue.trim())
                return this.invoices.list;

            searchValue = searchValue.toLowerCase();

            return this.invoices.list.filter(
                invoice => {
                    const brand = invoice.brand.toLowerCase();
                    const model = invoice.model.toLowerCase();
                    const date_start = invoice.datesRange.start;
                    const date_end = invoice.datesRange.end;



                    return brand.includes(searchValue) ||
                        model.includes(searchValue) ||

                        // To be able to choose between simple date selection and selection "between dates" values
                        dayjs(searchValue, 'YYYY-MM-DD', true).isValid() ?
                        dayjs(searchValue).isBetween(date_start, date_end, "day", "[]") :
                        (date_start.includes(searchValue) || date_end.includes(searchValue));

                }
            );
        },
        invoices_list_paginated: function(){
            let resultWithPagination = [];

            const currentPage = this.invoices.search.pagination.currentPage;
            const itemsPerPage = this.invoices.search.pagination.itemsPerPage;
            const result = this.invoices_list;

            for(let i = (currentPage-1)*itemsPerPage; i < currentPage*itemsPerPage; i++){
                if(i < result.length){
                    resultWithPagination.push(result[i]);
                }
            }

            console.log(result, resultWithPagination);
            return resultWithPagination;
        },

        invoices_pages: function(){
            return Math.ceil(this.invoices_list.length / this.invoices.search.pagination.itemsPerPage);
        },

        getMonthStart(){
            return new dayjs().startOf("month").format('DD.MM.YYYY');
        },
        getMonthEnd(){
            return new dayjs().endOf("month").format('DD.MM.YYYY');
        },
        getCurrentDate(){
            return new dayjs().format('DD.MM.YYYY');
        },

        driver(){
            let upcomingRents = 0;
            let estimatedSalary = 0;

            const monthStart = new dayjs().startOf("month");
            const monthEnd = new dayjs().endOf("month");

            const listRef = this.invoices.list;

            Object.keys(listRef).forEach(key => {
                // Calculating upcoming invoices and salary
                const dateStart = dayjs(listRef[key].datesRange.start);

                if(dateStart.isSameOrAfter(dayjs().add(1, 'day')))
                    upcomingRents++;

                if(dateStart.isBetween(monthStart, monthEnd, 'day', '[]'))
                    estimatedSalary += listRef[key].salary;
            });

            return {
                upcomingRents: upcomingRents,
                salaryThisMonth: estimatedSalary
            }
        }
    },
    methods:{
        daysDiffData(date){
            const dateParsed = dayjs(date);

            if(dateParsed.isSameOrBefore(dayjs()))
                return null;

            const inDays = dateParsed.diff(dayjs(), "day");

            console.log(inDays);

            return {
                days: inDays,
                suffix: declOfNum(inDays, js_localization.days)
            }
        },

        getDaysBetweenDates(date1, date2){
            return dayjs(date2).diff(dayjs(date1), "day");
        },


        isInvoiceSkipAvailable(invoiceData){
            if(!invoiceData)
                return false;

            return !(this.isActiveInvoice(invoiceData.datesRange.start, invoiceData.datesRange.end)
                || dayjs().isAfter(invoiceData.datesRange.end)
                || invoiceData.statusList.includes(2)
                || invoiceData.statusList.includes(3));
        },


        isActiveInvoice(date1, date2){
            return dayjs().isBetween(date1, date2, "day", "[]");
        },

        goToInvoicesPage(pageIndex){
            this.invoices.search.pagination.currentPage = pageIndex;
        },

        doesInvoiceDateExpired(date){
            return dayjs(date, "YYYY-MM-DD").add(2, 'day').isBefore(dayjs());
        },


        skipInvoice(invoiceId){
            axios.delete('http://localhost:8080/crrt_war/driver/invoices', {
                data: {
                    id: invoiceId
                }
            })
            .then(response => {
                console.log(response);

                if(response.data.status){
                    Notiflix.Notify.success(response.data.message);

                    // Deleting invoice from the list
                    for(let index in this.invoices.list){
                        if(this.invoices.list[index].id === invoiceId){
                            this.invoices.list.splice(index, 1);
                            break;
                        }
                    }
                }else{
                    Notiflix.Notify.warning(response.data.message);
                }
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        changeDriverCity(){
            axios.put('http://localhost:8080/crrt_war/driver/invoices', {
                cityId: this.selectedCity
            })
            .then(response => {
                console.log(response);

                this.currentCity = this.selectedCity;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        }
    }
}).mount('#app');