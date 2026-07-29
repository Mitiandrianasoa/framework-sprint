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

import com.sprint.annotation.Get;
import com.sprint.annotation.Post;
import com.sprint.annotation.RequestParam;
import com.sprint.annotation.Test;
import com.sprint.model.ModelView;
import com.sprint.util.AnnotationScanner;
import com.sprint.util.EntityBinder;
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
                    // SPRINT 7 : le verbe HTTP et l'URL proviennent de @Get, @Post ou @Test.
                    String httpMethod = extractHttpMethod(method);
                    String url = extractPath(method);

                    if (url != null && !url.isEmpty()) {
                        // La clé de routage inclut le verbe : "GET:/etudiant/{id}"
                        String key = httpMethod + ":" + url;
                        routeMap.put(key, method);
                        controllerInstances.put(method, controllerInstance);

                        // SPRINT 6 : si l'URL contient un paramètre {..}, on
                        // enregistre un motif pour la reconnaître à l'exécution.
                        if (url.contains("{")) {
                            pathPatterns.put(key, new PathPattern(url));
                        }

                        System.out.println("Route enregistrée: " + key + " -> "
                                + controllerClass.getSimpleName() + "." + method.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation des routes", e);
        }
    }

    /**
     * SPRINT 7 : détermine le verbe HTTP associé à une méthode selon son annotation.
     * @Get -> GET, @Post -> POST, @Test -> GET (par défaut).
     */
    private String extractHttpMethod(Method method) {
        if (method.isAnnotationPresent(Get.class)) return "GET";
        if (method.isAnnotationPresent(Post.class)) return "POST";
        if (method.isAnnotationPresent(Test.class)) return "GET";
        return null;
    }

    /**
     * SPRINT 7 : détermine l'URL associée à une méthode selon son annotation.
     */
    private String extractPath(Method method) {
        if (method.isAnnotationPresent(Get.class)) return method.getAnnotation(Get.class).value();
        if (method.isAnnotationPresent(Post.class)) return method.getAnnotation(Post.class).value();
        if (method.isAnnotationPresent(Test.class)) return method.getAnnotation(Test.class).value();
        return null;
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

        // SPRINT 7 : on lit le vrai verbe HTTP de la requête (GET, POST, ...).
        String httpMethod = req.getMethod();

        // SPRINT 6 : URL paramétrées ; SPRINT 7 : filtrées par verbe HTTP.
        Method method = trouverMethode(path, httpMethod);
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
     * SPRINT 6 : retrouve la méthode pour une URL (exacte puis motifs paramétrés).
     * SPRINT 7 : la recherche tient compte du verbe HTTP (clé "GET:/...").
     */
    private Method trouverMethode(String path, String httpMethod) {
        String key = httpMethod + ":" + path;

        // Correspondance exacte
        if (routeMap.containsKey(key)) {
            return routeMap.get(key);
        }

        // Correspondance via un motif paramétré, pour le même verbe HTTP
        String prefix = httpMethod + ":";
        for (Map.Entry<String, PathPattern> entry : pathPatterns.entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue().matches(path)) {
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

            // SPRINT 8 : si le paramètre est un objet métier (entité), on le
            // construit en liant les champs aux paramètres de la requête.
            if (EntityBinder.isEntity(type)) {
                try {
                    args[i] = EntityBinder.bindEntity(req, type);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur de binding de l'entité "
                            + type.getSimpleName(), e);
                }
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

            // SPRINT 8 : exposer le type de données détecté (string / map / ...)
            req.setAttribute("dataType", modelView.getDataType());

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
