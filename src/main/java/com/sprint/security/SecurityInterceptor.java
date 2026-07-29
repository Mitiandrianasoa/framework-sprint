package com.sprint.security;

import com.sprint.annotation.RequirePermission;
import com.sprint.annotation.RequireRole;
import com.sprint.annotation.Secured;
import com.sprint.model.JsonResponse;
import com.sprint.model.ModelView;
import com.sprint.model.UserSession;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * SPRINT 11-bis - Intercepteur de sécurité.
 *
 * Vérifie, avant l'exécution d'une méthode de contrôleur, les annotations
 * @Secured / @RequireRole / @RequirePermission. Retourne :
 *   - null si l'accès est autorisé (on continue l'exécution),
 *   - un objet de "refus" (ModelView "access-denied") sinon.
 */
public class SecurityInterceptor {

    public static Object checkSecurity(Method method, UserSession user,
                                       Map<String, Object> session) {

        // 1. @Secured
        if (method.isAnnotationPresent(Secured.class)) {
            Object result = checkSecured(method.getAnnotation(Secured.class), user);
            if (result != null) return result;
        }

        // 2. @RequireRole
        if (method.isAnnotationPresent(RequireRole.class)) {
            Object result = checkRequireRole(method.getAnnotation(RequireRole.class), user);
            if (result != null) return result;
        }

        // 3. @RequirePermission
        if (method.isAnnotationPresent(RequirePermission.class)) {
            Object result = checkRequirePermission(method.getAnnotation(RequirePermission.class), user);
            if (result != null) return result;
        }

        // 4. Mettre à jour les variables de rôle dans la session
        RoleManager.updateRoleVariablesInSession(user, session);
        return null; // accès autorisé
    }

    private static Object checkSecured(Secured secured, UserSession user) {
        // Authentification requise ?
        if ("true".equals(secured.requireAuth()) && (user == null || !user.isAuthenticated())) {
            return accessDenied(secured.errorMessage(), secured.redirectOnError());
        }
        // Rôles autorisés
        if (secured.roles().length > 0) {
            boolean ok = false;
            for (Role role : secured.roles()) {
                if (RoleManager.canAccess(user, role.name())) { ok = true; break; }
            }
            if (!ok) return accessDenied(secured.errorMessage(), secured.redirectOnError());
        }
        // Niveau minimum
        if (secured.minimumLevel() != Role.ANONYME
                && !RoleManager.hasMinimumLevel(user, secured.minimumLevel())) {
            return accessDenied(secured.errorMessage(), secured.redirectOnError());
        }
        // Permissions
        if (secured.permissions().length > 0) {
            boolean ok = false;
            for (String permission : secured.permissions()) {
                if (RoleManager.hasPermission(user, permission)) { ok = true; break; }
            }
            if (!ok) return accessDenied(secured.errorMessage(), secured.redirectOnError());
        }
        return null;
    }

    private static Object checkRequireRole(RequireRole requireRole, UserSession user) {
        Role required = requireRole.value();
        boolean ok = requireRole.allowHigher()
                ? RoleManager.hasMinimumLevel(user, required)
                : RoleManager.canAccess(user, required.name());
        if (!ok) {
            String message = String.format(requireRole.message(), required.getDisplayName());
            return accessDenied(message, requireRole.redirect());
        }
        return null;
    }

    private static Object checkRequirePermission(RequirePermission requirePermission, UserSession user) {
        if (RoleManager.hasPermission(user, requirePermission.value())) {
            return null;
        }
        for (String alternative : requirePermission.alternatives()) {
            if (RoleManager.hasPermission(user, alternative)) {
                return null;
            }
        }
        String message = String.format(requirePermission.message(), requirePermission.value());
        return accessDenied(message, requirePermission.redirect());
    }

    /**
     * Construit la réponse d'accès refusé.
     * CORRECTION : on utilise addObject(...) (et non addData qui n'existe pas
     * dans ModelView) — c'était le bug qui empêchait la compilation.
     */
    private static Object accessDenied(String errorMessage, String redirectUrl) {
        if (isApiRequest()) {
            return JsonResponse.error(errorMessage, 403);
        }
        ModelView mv = new ModelView("access-denied");
        mv.addObject("errorMessage", errorMessage);
        mv.addObject("redirectUrl", redirectUrl);
        return mv;
    }

    private static boolean isApiRequest() {
        return false; // simplification : à raffiner selon l'en-tête Accept
    }
}
