package com.sprint.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPRINT 11 - Gestionnaire de session basé sur une Map (et non HttpSession).
 *
 * Les sessions sont stockées côté serveur dans une Map globale, indexées par un
 * identifiant de session. Chaque session est elle-même une Map<String, Object>.
 */
public class SessionManager {

    // Stockage global : idSession -> données de session
    private static final Map<String, Map<String, Object>> sessionStore = new ConcurrentHashMap<>();

    // Attribut de requête où l'on mémorise l'id de session courant
    private static final String SESSION_ID_ATTR = "SESSION_ID";

    /**
     * Récupère (ou crée) une session pour la requête.
     */
    public static Map<String, Object> getSession(HttpServletRequest req, String sessionName, boolean create) {
        String sessionId = getOrCreateSessionId(req);
        Map<String, Object> mainSession = sessionStore.computeIfAbsent(sessionId, k -> new HashMap<>());

        if (!"default".equals(sessionName)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> named = (Map<String, Object>) mainSession.computeIfAbsent(
                    sessionName, k -> new HashMap<>());
            return named;
        }
        return mainSession;
    }

    /** Session par défaut. */
    public static Map<String, Object> getSession(HttpServletRequest req) {
        return getSession(req, "default", true);
    }

    private static String getOrCreateSessionId(HttpServletRequest req) {
        String sessionId = (String) req.getAttribute(SESSION_ID_ATTR);
        if (sessionId == null) {
            sessionId = req.getParameter("sessionId");
            if (sessionId == null) {
                sessionId = generateSessionId();
            }
            req.setAttribute(SESSION_ID_ATTR, sessionId);
        }
        return sessionId;
    }

    private static String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_"
                + Integer.toHexString((int) (Math.random() * 0xFFFF));
    }

    public static String getSessionId(HttpServletRequest req) {
        return getOrCreateSessionId(req);
    }

    /**
     * Copie les données de session dans les attributs de requête,
     * pour les rendre accessibles dans la vue JSP.
     */
    public static void copyToRequestAttributes(HttpServletRequest req) {
        Map<String, Object> session = getSession(req, "default", false);
        if (session != null) {
            for (Map.Entry<String, Object> entry : session.entrySet()) {
                req.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }
}
