$(function() {

  $(".numbers-row").prepend('<div class="dec button">-</div>');
  $(".numbers-row").append('<div class="inc button">+</div>');

  $(".button").on("click", function() {

    var $button = $(this);
    var oldValue = $("input#french-hens").val();

    if ($button.text() == "+") {
      if($("input#french-hens").val().length == 2) return;
  	  var newVal = parseFloat(oldValue) + 1;
  	} else {
	   // Don't allow decrementing below zero
      if (oldValue > 0) {
        var newVal = parseFloat(oldValue) - 1;
	    } else {
        newVal = 0;
      }
	  }

    $("input#french-hens").val(newVal);

  });
});