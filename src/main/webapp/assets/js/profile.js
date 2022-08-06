const { createApp } = Vue;

const incomingUserData = {
    firstname: "Mykhailo",
    surname: "Diachenko",
    patronymic: "Dmitrievich",
    balance: 100
}

const app = createApp({
    data() {
        return {
            userDataCurrent:{},
            userDataPrevious:{},
            passwords:{
                old: "",
                new: ""
            },
            moneyReplenishmentNumber: 0
        }
    },
    created(){
        Object.assign(this.userDataCurrent, incomingUserData);
        Object.assign(this.userDataPrevious, incomingUserData);
    },
    computed:{
        wasUserDataChanged: function () {
            return JSON.stringify(this.userDataCurrent) === JSON.stringify(this.userDataPrevious);
        },

        isUpdatePasswordButtonBlocked: function () {
            if(this.passwords.old && this.passwords.new){
                return this.passwords.old === this.passwords.new;
            }else{
                return true;
            }
        },
    },
    methods:{
        replenishMoney(){
            this.userDataCurrent.balance += this.moneyReplenishmentNumber;
            this.moneyReplenishmentNumber = 0;
        }
    }
}).mount('#app');

// Uploading avatar
$(document).on('click','.avatar-add-photo-button',function (e) {
    e.preventDefault();
    $(".avatar-add-photo-input").click();
});
$(document).on("change",".avatar-add-photo-input", function () {
    $(".avatar-add-photo-form").submit();
});
$(document).on('submit','.avatar-add-photo-form', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
});