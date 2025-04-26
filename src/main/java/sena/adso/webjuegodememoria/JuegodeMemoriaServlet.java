package sena.adso.webjuegodememoria;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/juego")
public class JuegodeMemoriaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Servlet iniciado corretamente");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("Petición recibida: " + request.getRequestURI());
        HttpSession session = request.getSession();
        String accion = request.getParameter("accion");

        //Si no hay acción, iniciar el juegp
        if (accion == null || accion.isEmpty()) {
            System.out.println("Iniciando nuevo juego");
            iniciarJuego(session, request, response);
            return;
        }

        //Para acciones específicas
        switch (accion) {
            case "voltear":
                manejarVolteo(session, request, response);
                break;
            case "reiniciar":
                manejarReinicio(session, request, response);
                break;
            default:
                iniciarJuego(session, request, response);
        }
    }

    private void iniciarJuego(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            System.out.println("Creando nuevas cartas");
            List<Carta> cartas = crearCartas();

            //Guardar en sesión
            session.setAttribute("cartas", cartas);
            session.setAttribute("intentos", 0);

            //Guardar en request para JSP
            request.setAttribute("cartas", cartas);
            request.setAttribute("intentos", 0);

            System.out.println("Cartas creadas: " + cartas.size());

            //Redirigir al JSP
            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (IOException | ServletException e) {
            System.err.println("Error al iniciar juego: " + e.getMessage());
            throw new ServletException("Error al iniciar el juego", e);
        }
    }

    private List<Carta> crearCartas() {
        List<Carta> cartas = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cartas.add(new Carta(String.valueOf(i)));
            cartas.add(new Carta(String.valueOf(i)));
        }
        Collections.shuffle(cartas);
        return cartas;
    }

    private void manejarVolteo(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            List<Carta> cartas = (List<Carta>) session.getAttribute("cartas");
            Integer intentos = (Integer) session.getAttribute("intentos");

            if (cartas == null || intentos == null) {
                throw new IllegalStateException("Datos de sesión no encontrados");
            }

            String indexParam = request.getParameter("index");
            if (indexParam == null || indexParam.isEmpty()) {
                throw new IllegalArgumentException("Índice no proporcionado");
            }

            int index;
            try {
                index = Integer.parseInt(indexParam);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Índice inválido" + indexParam);
            }

            if (index >= 0 && index < cartas.size()) {
                Carta cartaSeleccionada = cartas.get(index);
                if (!cartaSeleccionada.isEmparejada()) {
                    cartaSeleccionada.setRevelada(true);

                    List<Carta> cartasReveladas = obtenerCartasReveladas(cartas);
                    if (cartasReveladas.size() == 2) {
                        intentos++;
                        session.setAttribute("intentos", intentos);

                        if (sonPareja(cartasReveladas.get(0), cartasReveladas.get(1))) {
                            cartasReveladas.get(0).setEmparejada(true);
                            cartasReveladas.get(1).setEmparejada(true);
                        } else {
                            for (Carta c : cartasReveladas) {
                                if (!c.isEmparejada()) {
                                    c.setRevelada(false);
                                }
                            }
                        }
                    }

                }
            } else {
                throw new IllegalArgumentException("Índice fuera de rango: " + index);
            }
            enviarRespuestaJSON(response, cartas, intentos);
        } catch (Exception e) {
            System.err.println("Error en manejarVolteo: " + e.getMessage());
            enviarError(response, "Error al voltear carta: " + e.getMessage());
        }
    }

    private void manejarReinicio(HttpSession session, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            System.out.println("Reiniciando juego");
            List<Carta> cartas = crearCartas();
            session.setAttribute("cartas", cartas);
            session.setAttribute("intentos", 0);
            enviarRespuestaJSON(response, cartas, 0);
        } catch (Exception e) {
            System.out.println("Error en manejarReinicio: " + e.getMessage());
            e.printStackTrace();
            enviarError(response, "Error al reiniciar el juego: " + e.getMessage());
        }
    }

    private List<Carta> obtenerCartasReveladas(List<Carta> cartas) {
        List<Carta> reveladas = new ArrayList<>();
        for (Carta c : cartas) {
            if (c.isRevelada() && !c.isEmparejada()) {
                reveladas.add(c);
            }
        }
        return reveladas;
    }

    private boolean sonPareja(Carta c1, Carta c2) {
        return c1.getId().equals(c2.getId());
    }

    private void enviarRespuestaJSON(HttpServletResponse response, List<Carta> cartas,
            Integer intentos) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        RespuestaJuego respuesta = new RespuestaJuego(cartas, intentos,cartas.stream().allMatch(Carta::isEmparejada));
        response.getWriter().write(gson.toJson(respuesta));
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(new ErrorResponse(mensaje)));
    }
    
   private static class RespuestaJuego { 
       private final List<Carta> cartas;
       private final int intentos;
       private final boolean juegoGanado;

        public RespuestaJuego(List<Carta> cartas, int intentos, boolean juegoGanado) {
            this.cartas = cartas;
            this.intentos = intentos;
            this.juegoGanado = juegoGanado;
        }      
   } 
   
    private static class ErrorResponse { 
           private final String error;

            public ErrorResponse(String error) {
                this.error = error;
            }          
       }
}
