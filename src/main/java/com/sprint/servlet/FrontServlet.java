package com.sprint.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.sprint.annotation.Test;
import com.sprint.util.AnnotationScanner;

/**
 * SPRINT 3 - Contrôleur frontal (Front Controller).
 *
 * Toutes les requêtes passent par ce servlet (mappé sur "/"). Au démarrage
 * (init), il scanne les classes @Controller, lit les méthodes annotées @Test
 * et CONSERVE la table de routage (URL -> méthode) pour toute la durée de vie
 * de l'application.
 */
@WebServlet("/")
public class FrontServlet extends HttpServlet {

    // Table de routage : URL -> méthode du contrôleur. Conservée dès l'init().
    private final Map<String, Method> routeMap = new HashMap<>();
    // Instance du contrôleur associée à chaque méthode (pour l'invocation).
    private final Map<Method, Object> controllerInstances = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        // Le scan se déclenche UNE SEULE FOIS, au démarrage de l'application.
        initialiserRoutes();
    }

    /**
     * Scanne les contrôleurs et enregistre les routes dans routeMap.
     */
    private void initialiserRoutes() throws ServletException {
        try {
            List<Class<?>> controllerClasses = AnnotationScanner.findControllers("com.sprint.controller");

            for (Class<?> controllerClass : controllerClasses) {
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                for (Method method : controllerClass.getDeclaredMethods()) {
                    Test test = method.getAnnotation(Test.class);
                    if (test != null && !test.value().isEmpty()) {
                        String url = test.value();
                        routeMap.put(url, method);
                        controllerInstances.put(method, controllerInstance);
                        System.out.println("Route enregistrée: " + url + " -> "
                                + controllerClass.getSimpleName() + "." + method.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation des routes", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        traiterRequete(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        traiterRequete(req, resp);
    }

    /**
     * SPRINT 4 : on exécute la méthode associée à l'URL, puis on récupère sa
     * valeur de retour. Si c'est une chaîne de caractères (String), on l'écrit
     * directement dans la réponse.
     */
    private void traiterRequete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());

        Method method = routeMap.get(path);
        if (method == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Route non trouvée: " + path);
            return;
        }

        try {
            // 1. Récupérer l'instance du contrôleur puis exécuter la méthode
            Object controller = controllerInstances.get(method);
            Object result = method.invoke(controller);

            // 2. Traiter la valeur de retour
            traiterResultat(result, resp);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'exécution de la route " + path, e);
        }
    }

    /**
     * SPRINT 4 : gestion du résultat retourné par le contrôleur.
     * Pour l'instant, on ne sait traiter que le type String.
     */
    private void traiterResultat(Object result, HttpServletResponse resp) throws IOException {
        if (result instanceof String) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write((String) result);
        }
    }
}
