$(document).ready(function() {
	$('#topbar').css({
		'left': 0,
		'position': 'fixed',
		'top': 0,
		'width': '100%',
	});

	$('a[href*=#]').click(function() {
	    if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') 
	        && location.hostname == this.hostname) {
            var $target = $(this.hash);
            $target = $target.length && $target || $('[name=' + this.hash.slice(1) +']');

            if ($target.length) {
                var targetOffset = $target.offset().top - $('#topbar').outerHeight() - 10;
                $('html,body').animate({scrollTop: targetOffset}, 500);
                return false;
            }
        }
    });

	$('#page').css('margin-top', $('#topbar').outerHeight() + 20);

	userAgent = navigator.userAgent.toLowerCase();
		if (userAgent.indexOf('android') == -1 || userAgent.indexOf('chrome') == -1) {
		$('#bg').css('height', $(window).outerHeight());

		$(window).resize(function() {
			$('#bg').css('height', $(window).outerHeight());
		});
	} else {

	}

});