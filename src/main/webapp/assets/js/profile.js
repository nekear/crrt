const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            userDataCurrent:{},
            userDataPrevious:{},
            passwords:{
                old: "",
                new: ""
            },
            balanceReplenishmentNumber: 0,
            avatarUrl: ""
        }
    },
    created(){
        Object.assign(this.userDataCurrent, incomingUserData);
        Object.assign(this.userDataPrevious, incomingUserData);
        // Object.assign(this.current, currentTheme);
        // Object.assign(this.selected, currentTheme);
        this.avatarUrl = avatar;
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

        avatar: function (){
            return `background-image: url('${this.avatarUrl}')`;
        }
    },
    methods:{
        saveUserData(){
            let changedUserData = {};

            // Finding changed variables
            for(let field_name in this.userDataCurrent ){
                if(this.userDataPrevious[field_name] !== this.userDataCurrent[field_name]){
                    changedUserData[field_name] = this.userDataCurrent[field_name];
                }
            }

            if(Object.keys(changedUserData).length){
                axios.post('/profile/updateData', {
                    ...changedUserData
                })
                .then(function (response) {
                    console.log(response);
                    Notiflix.Notify.success(response.data);
                    Object.assign(app.userDataPrevious, app.userDataCurrent);
                })
                .catch(function (error) {
                    console.log(error);
                    Object.assign(app.userDataCurrent, app.userDataPrevious);
                    Notiflix.Notify.failure(error.response.data);
                });
            }
        },
        changePasswordAction(){
            axios.post('/profile/updatePassword', {
                old_password: app.passwords.old,
                new_password: app.passwords.new
            })
            .then(function (response) {
                console.log(response);
                app.passwords.old = "";
                app.passwords.new = "";
                Notiflix.Notify.success(response.data);
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },
        replenishBalance(){
            axios.post('/profile/replenish', {
                amount: app.balanceReplenishmentNumber
            })
            .then(function (response) {
                console.log(response);
                document.getElementById("balance-amount").textContent = response.data.newBalance.toFixed(1);
                app.balanceReplenishmentNumber = 0;
                Notiflix.Notify.success(response.data.message);
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },
        deleteAvatar(){
            axios.post('/profile/deleteAvatar')
            .then(function (response) {
                console.log(response);
                app.avatarUrl = response.data.avatar;
                document.getElementById("header-profile-avatar").style.backgroundImage = `url('${response.data.avatar}')`
                document.getElementById("avatar-file-input").value = null;
            })
            .catch(function (error) {
                console.log(error);
                Notiflix.Notify.failure(error.response.data);
            });
        },

        exitAccount(){
            window.location.href= baseUrl + '/exit';
        },
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
        console.log("submitting photo");
        axios.post('/profile/uploadAvatar', formData, {
            headers: {
                'Content-type': 'multipart/form-data'
            }
        })
        .then(function (response) {
            console.log(response);
            app.avatarUrl = response.data.avatar;
            document.getElementById("header-profile-avatar").style.backgroundImage = `url('${response.data.avatar}')`;
            document.getElementById("avatar-file-input").value = null;
        })
        .catch(function (error) {
            console.log(error);
            Notiflix.Notify.failure(error.response.data);
        });
});
