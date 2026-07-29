package com.sprint.security;

import com.sprint.model.UserSession;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * SPRINT 11-bis - Logique d'autorisation : permissions, niveaux, variables de rôle.
 */
public class RoleManager {

    /** L'utilisateur possède-t-il la permission ? (anonyme si non authentifié) */
    public static boolean hasPermission(UserSession user, String permission) {
        if (user == null || !user.isAuthenticated()) {
            return Role.ANONYME.hasPermission(permission);
        }
        return user.getRoles().stream()
                .anyMatch(roleName -> Role.fromString(roleName).hasPermission(permission));
    }

    /** L'utilisateur atteint-il au moins le niveau de rôle demandé ? */
    public static boolean hasMinimumLevel(UserSession user, Role minimumRole) {
        if (user == null || !user.isAuthenticated()) {
            return Role.ANONYME.hasLevelOrHigher(minimumRole);
        }
        return user.getRoles().stream()
                .anyMatch(roleName -> Role.fromString(roleName).hasLevelOrHigher(minimumRole));
    }

    /** Accès autorisé à une ressource nécessitant un rôle donné. */
    public static boolean canAccess(UserSession user, String requiredRole) {
        if (!Role.isValidRole(requiredRole)) {
            return false;
        }
        return hasMinimumLevel(user, Role.fromString(requiredRole));
    }

    /** Rôle le plus élevé de l'utilisateur (ANONYME si non authentifié). */
    public static Role getHighestRole(UserSession user) {
        if (user == null || !user.isAuthenticated()) {
            return Role.ANONYME;
        }
        return user.getRoles().stream()
                .map(Role::fromString)
                .max(Comparator.comparingInt(Role::getLevel))
                .orElse(Role.ANONYME);
    }

    /** Autorisation d'une action sur une ressource (ex: USER + EDIT -> USER_EDIT). */
    public static boolean isAccessAuthorized(UserSession user, String resource, String action) {
        String permission = resource.toUpperCase() + "_" + action.toUpperCase();
        return hasPermission(user, permission);
    }

    /** Variables spécifiques au rôle courant (exposées à la vue). */
    public static Map<String, Object> getRoleSpecificVariables(UserSession user) {
        Map<String, Object> variables = new HashMap<>();
        Role highestRole = getHighestRole(user);

        variables.put("currentRole", highestRole.name());
        variables.put("roleLevel", highestRole.getLevel());
        variables.put("roleDisplayName", highestRole.getDisplayName());
        variables.put("isAuthenticated", user != null && user.isAuthenticated());

        String[] specificVars = {"maxFileSize", "canComment", "canBanUsers",
                "canManageSystem", "accessLevel"};
        for (String varName : specificVars) {
            Object value = highestRole.getRoleSpecificVariable(varName);
            if (value != null) {
                variables.put(varName, value);
            }
        }
        return variables;
    }

    /** Injecte les variables de rôle dans la Map de session. */
    public static void updateRoleVariablesInSession(UserSession user, Map<String, Object> session) {
        if (session != null) {
            session.putAll(getRoleSpecificVariables(user));
        }
    }
}
