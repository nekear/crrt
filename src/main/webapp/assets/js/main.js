document.addEventListener("DOMContentLoaded", function (){
   var input = document.getElementById('rent-range-datepicker');
   var datepicker = new HotelDatepicker(input, {
      format: "DD.MM.YYYY",
      startOfWeek: "monday",
      endDate: new Date().setMonth(new Date().getMonth() + 2),
      disabledDates: ['2022-07-30', '2022-07-31'],
      clearButton: true,
      topbarPosition: 'bottom'
   });

   // TODO:: ADD POPUP INFORMING ABOUT COPYING TO CLIPBOARD
   $(document).on("click", ".copy-item", function(){
      let textToCopy = $(this).find(".content .text").text().trim();
      navigator.clipboard.writeText(textToCopy).then(function() {
         console.log(`${textToCopy} was copied to clipboard!`);
      }, function(err) {
         console.error('Async: Could not copy text: ', err);
      });
   });
});