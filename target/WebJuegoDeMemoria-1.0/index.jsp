<%@page import="sena.adso.webjuegodememoria.Carta"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Juego de Memoria</title>
        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
        <style>
            :root {
                --primary-color: #4CAF50;
                --primary-hover: #45a049;
                --background-color: #f0f0f0;
                --text-color: #333;
                --shadow-color: rgba(0, 0, 0, 0.1);
            }

            body {
                font-family: Arial, sans-serif;
                margin: 0;
                padding: 20px;
                min-height: 100vh;
                background-color: var(--background-color);
                display: flex;
                justify-content: center;
                align-items: center;
            }

            .contenedor {
                text-align: center;
                background-color: white;
                padding: 20px;
                border-radius: 10px;
                box-shadow: 0 0 10px var(--shadow-color);
                width: 100%;
                max-width: 600px;
            }

            #juego {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 10px;
                margin: 20px auto;
                max-width: 500px;
            }

            .carta {
                background-color: var(--primary-color);
                aspect-ratio: 1;
                border-radius: 8px;
                display: flex;
                justify-content: center;
                align-items: center;
                cursor: pointer;
                font-size: 24px;
                color: white;
                transition: background-color 0.3s;
                min-height: 80px;
            }

            .carta:hover {
                background-color: var(--primary-hover);
            }

            .carta span {
                font-weight: bold;
            }

            .oculto {
                display: none;
            }

            button {
                padding: 10px 20px;
                font-size: 16px;
                background-color: var(--primary-color);
                color: white;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                transition: background-color 0.3s;
            }

            button:hover {
                background-color: var(--primary-hover);
            }
        </style>
    </head>
    <body>
        <div class="contenedor">
            <h1>Juego de Memoria</h1>
            <div id="juego">
                <%
                    List<Carta> cartas = (List<Carta>) request.getAttribute("cartas");
                    if (cartas != null) {
                        for (int i = 0; i < cartas.size(); i++) {
                            Carta carta = cartas.get(i);
                %>
                <div class="carta" data-index="<%= i%>" onclick="voltearCarta(this.getAttribute('data-index'))">
                    <span class="<%= carta.isRevelada() ? "" : "oculto"%>"><%= carta.getId()%></span>
                    <span class="<%= carta.isRevelada() ? "oculto" : ""%>">?</span>
                </div>
                <%
                    }
                } else {
                %>
                <p>Error: No se pudieron cargar las cartas</p>
                <%
                    }
                %>
            </div>
            <p>Intentos: <span id="intentos">${intentos}</span></p>
            <button onclick="confirmarReinicio()">Reiniciar Juego</button>
        </div>

        <script>
            let bloqueado = false;
            const servletPath = '/juego'; // Ruta actualizada del servlet
            const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

            function mostrarError(mensaje) {
                Swal.fire({
                    icon: 'error',
                    title: '¡Ups!',
                    text: mensaje,
                    confirmButtonColor: '#4CAF50'
                });
            }

            async function voltearCarta(index) {
                if (bloqueado)
                    return;

                try {
                    bloqueado = true;
                    const response = await fetch(contextPath + servletPath + '?accion=voltear&index=' + index);
                    if (!response.ok)
                        throw new Error('Error en la respuesta del servidor');

                    const data = await response.json();
                    actualizarTablero(data.cartas);
                    document.getElementById('intentos').textContent = data.intentos;

                    if (data.juegoGanado) {
                        setTimeout(() => {
                            mostrarVictoria(data.intentos);
                        }, 500);
                    }
                } catch (error) {
                    mostrarError('Error al voltear la carta: ' + error.message);
                    console.error('Error:', error);
                } finally {
                    bloqueado = false;
                }
            }

            function confirmarReinicio() {
                Swal.fire({
                    title: '¿Reiniciar juego?',
                    text: "¿Estás seguro de que quieres empezar de nuevo?",
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#4CAF50',
                    cancelButtonColor: '#d33',
                    confirmButtonText: 'Sí, reiniciar',
                    cancelButtonText: 'Cancelar'
                }).then((result) => {
                    if (result.isConfirmed) {
                        reiniciarJuego();
                    }
                });
            }

            function mostrarVictoria(intentos) {
                Swal.fire({
                    title: '¡Felicitaciones!',
                    text: '¡Has ganado en ' + intentos + ' intentos!',
                    icon: 'success',
                    confirmButtonColor: '#4CAF50',
                    showConfirmButton: true,
                    confirmButtonText: 'Jugar de nuevo'
                }).then((result) => {
                    if (result.isConfirmed) {
                        reiniciarJuego();
                    }
                });
            }

            async function reiniciarJuego() {
                if (bloqueado)
                    return;

                try {
                    bloqueado = true;
                    const response = await fetch(contextPath + servletPath + '?accion=reiniciar');
                    if (!response.ok)
                        throw new Error('Error al reiniciar el juego');

                    const data = await response.json();
                    actualizarTablero(data.cartas);
                    document.getElementById('intentos').textContent = data.intentos;

                    Swal.fire({
                        position: 'top-end',
                        icon: 'success',
                        title: 'Juego reiniciado',
                        showConfirmButton: false,
                        timer: 1500
                    });
                } catch (error) {
                    mostrarError('Error al reiniciar el juego: ' + error.message);
                    console.error('Error:', error);
                } finally {
                    bloqueado = false;
                }
            }

            function actualizarTablero(cartas) {
                const cartasDiv = document.querySelectorAll('.carta');
                cartas.forEach((carta, index) => {
                    const spanNumero = cartasDiv[index].querySelector('span:first-child');
                    const spanInterrogacion = cartasDiv[index].querySelector('span:last-child');

                    spanNumero.textContent = carta.id;
                    spanNumero.className = carta.revelada ? '' : 'oculto';
                    spanInterrogacion.className = carta.revelada ? 'oculto' : '';
                });
            }
        </script>
    </body>
</html>