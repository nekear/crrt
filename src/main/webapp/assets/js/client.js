const { createApp } = Vue;

dayjs.extend(window.dayjs_plugin_isBetween);
dayjs.extend(window.dayjs_plugin_customParseFormat);

const app = createApp({
    data() {
        return {
            user:{
                balance: 0,
            },
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

        this.user.balance = userBalance;
    },
    mounted(){
        // Getting invoices list (without loaded = in silent mode)
        axios({
            method: "get",
            url: `http://localhost:8080/crrt_war/client/invoices`,
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
                    const code = invoice.code.toLowerCase();
                    const date_start = invoice.datesRange.start;
                    const date_end = invoice.datesRange.end;



                    return brand.includes(searchValue) ||
                        model.includes(searchValue) ||
                        code.includes(searchValue) ||

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

        isInvoiceCancellationAvailable(){
            if(!this.invoices.details)
                return false;

            return !(this.isActiveInvoice(this.invoices.details.datesRange.start, this.invoices.details.datesRange.end)
                || dayjs().isAfter(this.invoices.details.datesRange.end)
                || this.invoices.details.statusList.includes(2)
                || this.invoices.details.statusList.includes(3));
        }
    },
    methods:{
        isActiveInvoice(date1, date2){
            return dayjs().isBetween(date1, date2, "day", "[]");
        },

        goToInvoicesPage(pageIndex){
            this.invoices.search.pagination.currentPage = pageIndex;
        },

        doesInvoiceDateExpired(date){
            return dayjs(date, "YYYY-MM-DD").add(2, 'day').isBefore(dayjs());
        },

        openInvoiceDetailsModal(invoice_id){
            console.log(invoice_id);
            axios.get(`http://localhost:8080/crrt_war/client/invoice`, {
                params:{
                    invoice_id: invoice_id
                }
            })
            .then(response => {
                console.log(response);
                $("#invoiceDetails_modal").modal("show");
                this.invoices.details = response.data;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
            $("#invoiceDetails_modal").modal("show");
        },

        payRepairInvoice(repairInvoiceId){
            axios.put('http://localhost:8080/crrt_war/client/repairInvoice', {
                id: repairInvoiceId
            })
            .then(response => {
                console.log(response);

                // Getting repairment invoice price to subtract it visually on page
                let currentRepairmentInvoicePrice = 0;

                for(let index in this.invoices.details.repairInvoices){
                    let currentInvoices = this.invoices.details.repairInvoices[index];
                    if(currentInvoices.id === repairInvoiceId){
                        currentRepairmentInvoicePrice = currentInvoices.price;
                        break;
                    }
                }

                // Actually subtracting money and updating its representation in header dropdown
                this.user.balance -= currentRepairmentInvoicePrice;
                document.getElementById("balance-amount").innerText = this.user.balance.toFixed(1);

                // Updating current invoice entity
                this.invoices.details = response.data;

                // Updating invoice entity in the list
                for(let index in this.invoices.list){
                    let currentInvoice = this.invoices.list[index];
                    if(currentInvoice.id === this.invoices.details.id){
                        currentInvoice.statusList = this.invoices.details.statusList;
                        break;
                    }
                }
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        cancelInvoice(){
            axios.delete('http://localhost:8080/crrt_war/client/invoice', {
                data: {
                    id: this.invoices.details.id,
                }
            })
            .then(response => {

                console.log(response);

                // Updating invoice entity in the list
                for(let index in this.invoices.list){
                    let currentInvoice = this.invoices.list[index];
                    if(currentInvoice.id === this.invoices.details.id){
                        currentInvoice.statusList.push(JSON.parse(response.data.status));
                        break;
                    }
                }

                document.getElementById("balance-amount").innerText = response.data.newBalance.toFixed(2);

                $("#invoiceDetails_modal").modal("hide");
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        }
    }
}).mount('#app');