package com.sprint.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.sprint.annotation.RequestParam;
import com.sprint.annotation.Test;
import com.sprint.model.ModelView;
import com.sprint.util.AnnotationScanner;
import com.sprint.util.PathPattern;

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
    // SPRINT 6 : motifs d'URL paramétrées (ex: "/etudiant/{id}").
    private final Map<String, PathPattern> pathPatterns = new HashMap<>();

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

                        // SPRINT 6 : si l'URL contient un paramètre {..}, on
                        // enregistre un motif pour la reconnaître à l'exécution.
                        if (url.contains("{")) {
                            pathPatterns.put(url, new PathPattern(url));
                        }

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

        // SPRINT 6 : la résolution accepte désormais les URL paramétrées.
        Method method = trouverMethode(path);
        if (method == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Route non trouvée: " + path);
            return;
        }

        try {
            // 1. Récupérer l'instance du contrôleur
            Object controller = controllerInstances.get(method);

            // 2. Construire les arguments (valeurs des {param} de l'URL + @RequestParam)
            Object[] args = extraireArguments(method, path, req);

            // 3. Exécuter la méthode
            Object result = method.invoke(controller, args);

            // 4. Traiter la valeur de retour
            traiterResultat(result, req, resp);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'exécution de la route " + path, e);
        }
    }

    /**
     * SPRINT 6 : retrouve la méthode pour une URL, d'abord par correspondance
     * exacte, puis via les motifs paramétrés ("/etudiant/{id}").
     */
    private Method trouverMethode(String path) {
        if (routeMap.containsKey(path)) {
            return routeMap.get(path);
        }
        for (Map.Entry<String, PathPattern> entry : pathPatterns.entrySet()) {
            if (entry.getValue().matches(path)) {
                return routeMap.get(entry.getKey());
            }
        }
        return null;
    }

    /**
     * SPRINT 6 : associe les {param} de l'URL aux paramètres de même nom.
     * SPRINT 6-bis : gère l'annotation @RequestParam (nom explicite + existence
     * obligatoire) et l'injection de HttpServletRequest/Response.
     */
    private Object[] extraireArguments(Method method, String path, HttpServletRequest req) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Map<String, String> pathParams = extraireParametresChemin(path);

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            // Injection directe des objets techniques
            if (type == HttpServletRequest.class) {
                args[i] = req;
                continue;
            }

            // SPRINT 6-bis : paramètre annoté @RequestParam
            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                String name = rp.value().isEmpty() ? param.getName() : rp.value();

                String value = pathParams.containsKey(name)
                        ? pathParams.get(name)
                        : req.getParameter(name);

                // Vérification de l'existence de l'argument
                if (value == null && rp.required()) {
                    throw new IllegalArgumentException("Paramètre requis manquant: " + name);
                }
                args[i] = convertToType(value, type);
                continue;
            }

            // SPRINT 6 : paramètre de chemin de même nom (get(id) <- {id})
            if (pathParams.containsKey(param.getName())) {
                args[i] = convertToType(pathParams.get(param.getName()), type);
            } else {
                // sinon, on tente un paramètre de requête du même nom
                String value = req.getParameter(param.getName());
                args[i] = (value != null) ? convertToType(value, type) : getDefaultValue(type);
            }
        }
        return args;
    }

    /**
     * Retrouve le motif qui correspond à l'URL et en extrait les paramètres.
     */
    private Map<String, String> extraireParametresChemin(String path) {
        for (PathPattern pattern : pathPatterns.values()) {
            if (pattern.matches(path)) {
                return pattern.extractParameters(path);
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Convertit une valeur textuelle vers le type Java attendu par le paramètre.
     */
    private Object convertToType(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    /**
     * Valeur par défaut pour un paramètre non renseigné (évite les NullPointer
     * sur les types primitifs).
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == char.class) return '\0';
        return null;
    }

    /**
     * SPRINT 4  : retour String -> écrit (PRINT) directement dans la réponse.
     * SPRINT 4-bis : retour ModelView -> forward vers la page JSP correspondante.
     * SPRINT 5  : les données du ModelView (Map) sont transférées à la vue.
     */
    private void traiterResultat(Object result, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (result instanceof String) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write((String) result);
        } else if (result instanceof ModelView) {
            ModelView modelView = (ModelView) result;

            // Transférer chaque donnée du ModelView vers les attributs de la requête
            // pour qu'elle soit accessible dans la JSP.
            if (modelView.getData() != null) {
                for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            String viewPath = "/WEB-INF/views/" + modelView.getView() + ".jsp";
            RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
            dispatcher.forward(req, resp);
        }
    }
}
