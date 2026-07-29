package com.sprint.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import com.sprint.model.UserSession;
import com.sprint.util.SessionManager;

/**
 * SPRINT 11 - Servlet de démonstration de la session basée sur Map.
 *
 * GET  : vérifie si des données de session existent et les expose à la vue.
 * POST : crée un utilisateur en session, ajoute une donnée, ou déconnecte.
 */
@WebServlet("/exist-map")
public class ExistMapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, Object> session = SessionManager.getSession(req);
        UserSession userSession = UserSession.fromSessionMap(session);

        // Rendre les données de session disponibles dans la vue
        SessionManager.copyToRequestAttributes(req);
        req.setAttribute("sessionId", SessionManager.getSessionId(req));
        req.setAttribute("sessionExists", !session.isEmpty());
        req.setAttribute("userAuthenticated", userSession != null && userSession.isAuthenticated());

        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().write("Session existe: " + (!session.isEmpty())
                + " | Utilisateur authentifié: "
                + (userSession != null && userSession.isAuthenticated()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, Object> session = SessionManager.getSession(req);
        String action = req.getParameter("action");

        if ("createUser".equals(action)) {
            String username = req.getParameter("username");
            String email = req.getParameter("email");
            String userId = "USER_" + System.currentTimeMillis();

            UserSession userSession = new UserSession(userId, username, email);
            String[] roles = req.getParameterValues("roles");
            if (roles != null) {
                for (String role : roles) {
                    userSession.addRole(role);
                }
            }
            userSession.authenticate(userSession.getRoles());
            userSession.saveToSessionMap(session);
            session.put("message", "Utilisateur créé: " + username);

        } else if ("logout".equals(action)) {
            UserSession userSession = UserSession.fromSessionMap(session);
            if (userSession != null) {
                userSession.logout();
                session.remove("userSession");
                session.put("message", "Utilisateur déconnecté");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/exist-map");
    }
}
