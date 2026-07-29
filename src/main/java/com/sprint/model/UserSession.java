package com.sprint.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SPRINT 11 - Représente un utilisateur en session, avec sa liste de rôles.
 *
 * On stocke/relit cette instance depuis la Map de session (clé "userSession").
 * Le sprint 11-bis s'appuie dessus pour la gestion des rôles.
 */
public class UserSession {

    private String userId;
    private String username;
    private String email;
    private List<String> roles;
    private long loginTime;
    private long lastActivity;
    private boolean authenticated;

    public UserSession() {
        this.roles = new ArrayList<>();
        this.loginTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.authenticated = false;
    }

    public UserSession(String userId, String username, String email) {
        this();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    /**
     * Reconstruit un UserSession à partir de la Map de session (ou null).
     */
    public static UserSession fromSessionMap(Map<String, Object> sessionMap) {
        if (sessionMap == null || !sessionMap.containsKey("userSession")) {
            return null;
        }
        Object obj = sessionMap.get("userSession");
        if (obj instanceof UserSession) {
            UserSession user = (UserSession) obj;
            user.updateLastActivity();
            return user;
        }
        return null;
    }

    /** Sauvegarde cette instance dans la Map de session. */
    public void saveToSessionMap(Map<String, Object> sessionMap) {
        if (sessionMap != null) {
            sessionMap.put("userSession", this);
        }
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public void addRole(String role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    /** Authentifie l'utilisateur avec la liste de rôles fournie. */
    public void authenticate(List<String> userRoles) {
        this.authenticated = true;
        this.roles.clear();
        if (userRoles != null) {
            this.roles.addAll(userRoles);
        }
        this.loginTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
    }

    public void logout() {
        this.authenticated = false;
        this.roles.clear();
    }

    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    public boolean isExpired(long timeoutMillis) {
        return (System.currentTimeMillis() - lastActivity) > timeoutMillis;
    }

    public boolean isExpired() {
        return isExpired(30 * 60 * 1000); // 30 minutes
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return new ArrayList<>(roles); }
    public void setRoles(List<String> roles) {
        this.roles = (roles != null) ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public long getLoginTime() { return loginTime; }
    public long getLastActivity() { return lastActivity; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    @Override
    public String toString() {
        return "UserSession{userId='" + userId + "', username='" + username
                + "', roles=" + roles + ", authenticated=" + authenticated + '}';
    }
}
