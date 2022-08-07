$(function () {
    // Dropdown Menu
    $('.mdx-dropdown').click(function () {
        $(this).attr('tabindex', 1).focus();
        $(this).toggleClass('active');
        $(this).find('.dropdown-menu').slideToggle(300);
    });
    $('.mdx-dropdown').focusout(function () {
        $(this).removeClass('active');
        $(this).find('.dropdown-menu').slideUp(300);
    });
    $(document).on('click',".mdx-dropdown .dropdown-menu li",function () {
        $(this).parents('.mdx-dropdown').find('span').html($(this).html());
        $(this).parents('.mdx-dropdown').find('input').attr('value', $(this).data('id'));
    });
    /*End Dropdown Menu*/

    $(document).on("mousedown", "[data-ripple]", function(e) {

        var selfElement = $(this); // Получаем текущий элемент

        var waveColor = $(this).data("ripple"); // Получаем цвет который был передан в "дату"

        var clickPosition = selfElement.css("position"); // Берем у текущего элемента стиль "position"

        var clickOffset = selfElement.offset(); // Получаем офсет элемента

        var x  = e.pageX - clickOffset.left; // Берем у него x
        var y  = e.pageY - clickOffset.top; // Берем у него y

        var min = Math.min(this.offsetHeight,this.offsetWidth,100); // Подсчет минимального из данных чисел, для начального размера

        var ripple = $("<div>", {class:'ripple',appendTo:selfElement}); // Добавляем блок с классом ripple в текущий элемент

        if(!clickPosition || clickPosition==="static") { // Вычисляем позицию
            selfElement.css({position:"relative"});
        }

        // Добавляем "волну"
        $("<div>",{
            class:'rippleWave',
            css:{
                background:waveColor,
                left:x - (min/2),
                top:y - (min/2),
                width:min,
                height:min
            },
            appendTo:ripple,
            on : {
                animationend : function(){
                    ripple.remove();
                }
            }
        });

    });

    $(document).on("click", ".mdx-password-show", e => {
        let currentEl = $(e.currentTarget);
        if(currentEl.data("password-status") === "shown"){
            currentEl.data("password-status", "hidden");
            currentEl.html("visibility_off");
            currentEl.parent().find($("input")).attr("type", "password");
        }else if(currentEl.data("password-status") === "hidden"){
            currentEl.data("password-status", "shown");
            currentEl.html("visibility");
            currentEl.parent().find($("input")).attr("type", "text");
        }
    });
});
