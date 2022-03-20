console.log("this is script file");

const toggleSidebar= () => {
	if ($(".sidebar").is(":visible")) {
		//true
		//to close


		$(".sidebar").css("display","none");

		$(".content").css("margin-left", "0%");
	}else{
		//false
		//to show


		$(".sidebar").css("display","block");

		$(".content").css("margin-left", "20%");



	}

};

