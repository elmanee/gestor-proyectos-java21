document.addEventListener("DOMContentLoaded", function() {
            let formRegistro = document.getElementById('form-registro');

            formRegistro?.addEventListener('submit', function(event) {
                event.preventDefault(); // Prevenir el envío del formulario para validar primero

                let nombre = document.getElementById('nombre').value;
                let email = document.getElementById('email').value;
                let password = document.getElementById('password').value;
                let mensajeError = document.getElementById('mensaje-error');

                // Verificar si algún campo está vacío
                if (!nombre || !email || !password) {
                    mensajeError.innerText = "Por favor complete todos los campos.";
                    return;
                }

                // Si todos los campos están llenos, el formulario se envía
                // Aquí podrías agregar más validaciones si es necesario
                formRegistro.submit();
            });
            });