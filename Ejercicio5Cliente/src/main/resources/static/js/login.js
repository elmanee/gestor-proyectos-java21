function validarLogin(){
	
	/*alert($("#email").val());
	alert($("#password").val());*/
	let email = document.getElementById("exampleInputEmail1");
	if($("#exampleInputEmail1").val() == "" && $("#exampleInputPassword1").val() == ""){
		let myToast = document.querySelector('#alerta1');
		let toast = new bootstrap.Toast(myToast);
		toast.show();
	}
	else if($("#exampleInputEmail1").val() == ""){
		document.getElementById("exampleInputEmail1").focus();
		let myToast = document.querySelector('#alerta2');
		let toast = new bootstrap.Toast(myToast);
		toast.show();
	}
	else if(email.checkValidity() == false){
		document.getElementById("exampleInputEmail1").focus();
		let myToast = document.querySelector('#alerta3');
		let toast = new bootstrap.Toast(myToast);
		toast.show();
	}
	else if($("#exampleInputPassword1").val() == ""){
		document.getElementById("exampleInputEmail1").focus();
		let myToast = document.querySelector('#alerta4');
		let toast = new bootstrap.Toast(myToast);
		toast.show();
	}
	else{

		$.ajax({
			url : "/verificarLogin",
			contentType:"application/json",
			data : JSON.stringify({correo_electronico: $("#exampleInputEmail1").val(), contrasena:$("#exampleInputPassword1").val()}),
			type : "POST",
			success: function(data){
				console.log(data);
				if(data.nombre == '000'){
					document.getElementById("exampleInputEmail1").focus();
					let myToast = document.querySelector('#alerta5');
					let toast = new bootstrap.Toast(myToast);
					toast.show();
				}else{
					localStorage.setItem("usuario", JSON.stringify(data));
					window.location.href = 'inicio';
				}
			}
		});
	}

}