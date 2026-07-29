package com.sprint.security;

import java.util.Arrays;
import java.util.List;

/**
 * SPRINT 11-bis - Rôles avec niveaux d'accès, permissions et variables spécifiques.
 *
 * Hiérarchie : ANONYME (0) &lt; USER (1) &lt; MODERATOR (2) &lt; ADMIN (3).
 */
public enum Role {

    ANONYME(0, "Anonyme",
            Arrays.asList("VIEW_PUBLIC", "ACCESS_LOGIN")),

    USER(1, "Utilisateur",
            Arrays.asList("VIEW_PUBLIC", "ACCESS_LOGIN", "VIEW_PROTECTED", "EDIT_PROFILE")),

    MODERATOR(2, "Modérateur",
            Arrays.asList("VIEW_PUBLIC", "ACCESS_LOGIN", "VIEW_PROTECTED", "EDIT_PROFILE",
                    "MODERATE_CONTENT", "VIEW_USER_LIST")),

    ADMIN(3, "Administrateur",
            Arrays.asList("VIEW_PUBLIC", "ACCESS_LOGIN", "VIEW_PROTECTED", "EDIT_PROFILE",
                    "MODERATE_CONTENT", "VIEW_USER_LIST", "MANAGE_USERS",
                    "SYSTEM_CONFIG", "VIEW_ADMIN_PANEL"));

    private final int level;
    private final String displayName;
    private final List<String> permissions;

    Role(int level, String displayName, List<String> permissions) {
        this.level = level;
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean hasLevelOrHigher(Role other) {
        return this.level >= other.level;
    }

    public boolean canAccess(Role targetRole) {
        return this.level >= targetRole.level;
    }

    /** Rôle à partir d'un nom (ANONYME par défaut si inconnu). */
    public static Role fromString(String roleName) {
        try {
            return Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ANONYME;
        }
    }

    public static boolean isValidRole(String roleName) {
        try {
            Role.valueOf(roleName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public List<String> getPermissions() { return permissions; }

    /**
     * SPRINT 11-bis : existence de variables spécifiques par rôle.
     */
    public Object getRoleSpecificVariable(String variableName) {
        switch (this) {
            case ANONYME:
                if ("maxFileSize".equals(variableName)) return 1 * 1024 * 1024L;
                if ("canComment".equals(variableName)) return false;
                return null;
            case USER:
                if ("maxFileSize".equals(variableName)) return 5 * 1024 * 1024L;
                if ("canComment".equals(variableName)) return true;
                return null;
            case MODERATOR:
                if ("maxFileSize".equals(variableName)) return 20 * 1024 * 1024L;
                if ("canComment".equals(variableName)) return true;
                if ("canBanUsers".equals(variableName)) return true;
                return null;
            case ADMIN:
                if ("maxFileSize".equals(variableName)) return 100 * 1024 * 1024L;
                if ("canManageSystem".equals(variableName)) return true;
                if ("accessLevel".equals(variableName)) return "FULL";
                return null;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return displayName + " (Level " + level + ")";
    }
}
