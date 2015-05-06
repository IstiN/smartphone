$(document).ready(function(){

    $.ajax({
        	  url: "/config",
        	  data: {

        	  },
        	  success: function(data) {
				var container = document.getElementById('jsoneditor');

				var options = {
					mode: 'code',
					modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
					error: function (err) {
					  alert(err.toString());
					}
				};

			  	var editor = new JSONEditor(container, options, data);

				$('#save').click(function(){
					$.ajax({
        	  			url: "/config",
        	  			method: "POST",
        	  			data: {
							"value" : editor.getText()
        	  			},
        	  			success : function(data) {
        	  				if (data.success) {
        	  					alert('config saved');
        	  				} else {
        	  					alert('Error!' + data);}
        	  				}
					});
				});
        	  }

        });
})