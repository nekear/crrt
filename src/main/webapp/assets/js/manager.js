
const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            invoices: {
                search:{
                    filters: {
                        code: "",
                        carName: "", // brand + model
                        datesRange: null,
                        driverEmail: "",
                        clientEmail: "",
                        status: 0
                    },
                    pagination: {
                        itemsPerPage: 15,
                        currentPage: 1
                    },
                    orderBy: []
                },
                details: null,
                repairInvoice:{
                    originId: "",
                    originCode: "",
                    price: {
                        value: 0,
                        isValid: true
                    },
                    comment: "",
                    expirationDate: {
                        date: null,
                        isValid: true
                    }
                },
                rejection:{
                  originCode: null,
                  reason: null
                },
            },
            cities: {},
            invoiceStatuses:{}
        }
    },
    created(){
        Object.assign(this.cities, loaded.cities);
        Object.assign(this.invoiceStatuses, loaded.invoiceStatuses);
    },

    mounted(){
        let url = new URLSearchParams(window.location.search);
        for(const entry of url.entries()) {
            console.log(entry[0], checkJSON(entry[1]) ? JSON.parse(entry[1]) : entry[1]);
        }

        // Getting pagination data from url
        if(url.has("elementsPerPage")){
            this.invoices.search.pagination.itemsPerPage = parseInt(url.get("elementsPerPage"));
        }

        if(url.has("askedPage")){
            this.invoices.search.pagination.currentPage = parseInt(url.get("askedPage"));
        }

        // Getting main filters from url (except dates range)
        for(let key in this.invoices.search.filters){
            if(key !== "datesRange" && url.has(key)){
                const currentItem = url.get(key);
                this.invoices.search.filters[key] = checkJSON(currentItem) ? JSON.parse(currentItem) : currentItem;
            }
        }

        // Getting dates range
        if(url.has("datesRange")){
            const currentItem = url.get("datesRange");
            const objValue = JSON.parse(currentItem);
            this.invoices.search.filters.datesRange = []
            this.invoices.search.filters.datesRange.push(new Date(objValue.start));
            this.invoices.search.filters.datesRange.push(new Date(objValue.end));
        }

        // Getting order by
        if(url.has("orderBy")){
            const currentItem = url.get("orderBy");
            this.invoices.search.orderBy = JSON.parse(currentItem);
        }
    },

    methods:{
        getSortOrder(name){
            let sortOrder = "null";
            for(index in this.invoices.search.orderBy){
                if(this.invoices.search.orderBy[index].name === name){
                    sortOrder = this.invoices.search.orderBy[index].type;
                    break;
                }
            }
            return sortOrder;
        },

        getSortIndex(name){
            let sortIndex = null;
            for(index in this.invoices.search.orderBy){
                if(this.invoices.search.orderBy[index].name === name){
                    sortIndex = index;
                    break;
                }
            }
            return sortIndex ? parseInt(sortIndex) + 1 : null;
        },

        increaseSort(name){
            console.log('increasing!');
            let wasFound = false;
            for(index in this.invoices.search.orderBy){
                let current = this.invoices.search.orderBy[index];
                if(current.name === name){
                    if(current.type === "asc"){
                        current.type = "desc";
                    }else if(current.type === "desc"){
                        this.invoices.search.orderBy.splice(index, 1);
                    }
                    wasFound = true;
                    break;
                }
            }

            if(!wasFound){
                this.invoices.search.orderBy.push({name: name, type: "asc"});
            }
        },

        doesInvoiceDateExpired(date){
            return dayjs(date, "YYYY-MM-DD").add(2, 'day').isBefore(dayjs());
        },

        getFormattedDate(date){
            return  dayjs(date).format('DD.MM.YYYY');
        },

        format(dates){
            const dateStart = dates[0];
            const dateEnd = dates[1];


            return `${this.getFormattedDate(dateStart)} - ${this.getFormattedDate(dateEnd)}`;
        },


        performInvoicesSearch(pageIndex){
            let filtersCloneTmp = {
                ...this.invoices.search.filters
            };

            delete filtersCloneTmp.datesRange;

            let url = new URLSearchParams("");

            url.set("askedPage", pageIndex);
            url.set("elementsPerPage", this.invoices.search.pagination.itemsPerPage);

            console.log(filtersCloneTmp);

            for(let key in filtersCloneTmp){
                const cleanedValue = _(filtersCloneTmp[key]);
                if(cleanedValue){
                    url.set(key,cleanedValue);
                }
            }

            if(this.invoices.search.filters.datesRange != null){
                let datesRange = {
                    start: dayjs(this.invoices.search.filters.datesRange[0]).format("YYYY-MM-DD"),
                    end: dayjs(this.invoices.search.filters.datesRange[1]).format("YYYY-MM-DD")
                };

                url.set("datesRange", JSON.stringify(datesRange));
            }

            if(this.invoices.search.orderBy.length){
                url.set("orderBy", JSON.stringify(this.invoices.search.orderBy));
            }

            window.location.href = window.location.origin+window.location.pathname+"?"+url.toString();

        },

        goToInvoicesPage(pageIndex){
            this.performInvoicesSearch(pageIndex);
        },


        openInvoiceDetailsModal(invoice_id){
            console.log(invoice_id);
            axios.get(`/manage/invoice`, {
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

        },

        closeInvoiceDetailsModal(){
            this.performInvoicesSearch(this.invoices.search.pagination.currentPage);
        },

        deleteRepairInvoice(repairInvoiceId){
            axios.delete('/manage/repairInvoice', {
                data: {
                    originId: this.invoices.details.id,
                    repairId: repairInvoiceId
                }
            })
            .then(response => {
                console.log(response);
                this.invoices.details = response.data;
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        createRepairInvoice(){
            let isAllValid = true;

            if(this.invoices.repairInvoice.price.value < 1){
                this.invoices.repairInvoice.price.isValid = false;
                isAllValid = false;
            }

            if(!this.invoices.repairInvoice.expirationDate.date){
                this.invoices.repairInvoice.expirationDate.isValid = false;
                isAllValid = false;
            }

            if(isAllValid){

                axios.post('/manage/repairInvoice', {
                    originId: this.invoices.repairInvoice.originId,
                    price: this.invoices.repairInvoice.price.value,
                    expirationDate: dayjs(this.invoices.repairInvoice.expirationDate.date).format("YYYY-MM-DD"),
                    comment: _(this.invoices.repairInvoice.comment)
                })
                .then(response => {
                    console.log(response);
                    this.invoices.details = response.data;
                    $("#createRepairInvoice_modal").modal("hide");
                    $("#invoiceDetails_modal").modal("show");
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },

        openCreateRepairInvoiceModal(){
            this.invoices.repairInvoice.price = {
                value: "",
                isValid: true
            }

            this.invoices.repairInvoice.expirationDate = {
                date: null,
                isValid: true
            }

            this.invoices.repairInvoice.comment = "";

            this.invoices.repairInvoice.originId = this.invoices.details.id;
            this.invoices.repairInvoice.originCode = this.invoices.details.code;

            $("#invoiceDetails_modal").modal("hide");
            $("#createRepairInvoice_modal").modal("show");
        },

        closeCreateRepairInvoiceModal(){
            $("#invoiceDetails_modal").modal("show");
            $("#createRepairInvoice_modal").modal("hide");
        },


        openRejectInvoiceModal(){
            this.invoices.rejection.originCode = this.invoices.details.code;
            this.invoices.rejection.reason = null;

            $("#rejectInvoice_modal").modal("show");
        },

        closeRejectInvoiceModal(){
            $("#invoiceDetails_modal").modal("show");
        },

        rejectInvoice(){
            const rejectionReason = _(this.invoices.rejection.reason);

            axios.delete('/manage/invoice', {
                data: {
                    id: this.invoices.details.id,
                    reason: rejectionReason
                }
            })
            .then(response => {
                console.log(response);
                this.invoices.details = response.data;

                $("#rejectInvoice_modal").modal("hide");
                $("#invoiceDetails_modal").modal("show");
            })
            .catch(error => {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        generateInvoicesReport(){
            axios({
                method: "get",
                url: '/manage/invoices/report',
                responseType: 'blob'
            })
                .then(response => {
                    const fileName = response.headers["content-disposition"].split('filename=')[1].split(';')[0];
                    const downloadUrl = window.URL.createObjectURL(new Blob([response.data]));

                    const link = document.createElement('a');

                    link.href = downloadUrl;

                    link.setAttribute('download', fileName.substring(1, fileName.length-1)); //any other extension

                    document.body.appendChild(link);

                    link.click();

                    link.remove();
                })
                .catch(error => {
                    console.log(error);
                    Notiflix.Notify.failure(error.response.data);
                });
        },
    }
});
app.component("Datepicker", VueDatePicker);
app.component("Sorter", sorterComponent);
const vm = app.mount('#app');


function _(str){
    if(!str)
        return null;

    if(typeof str === 'number')
        return str;

    return str.trim().length > 0 ? str.trim() : null;
}

function checkJSON(jsonStr) {
    try {
        JSON.parse(jsonStr);
    } catch (e) {
        return false;
    }
    return true;
}