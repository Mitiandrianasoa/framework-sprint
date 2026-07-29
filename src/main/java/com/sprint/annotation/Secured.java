package com.sprint.annotation;

import com.sprint.security.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 11-bis - Sécurise une méthode (authentification, rôles, permissions).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Secured {

    /** Rôles autorisés (un seul suffit). */
    Role[] roles() default {};

    /** Permissions requises (une seule suffit). */
    String[] permissions() default {};

    /** Niveau minimum requis. */
    Role minimumLevel() default Role.ANONYME;

    /** Message d'erreur en cas d'accès refusé. */
    String errorMessage() default "Accès refusé: permissions insuffisantes";

    /** URL de redirection en cas de refus. */
    String redirectOnError() default "/access-denied";

    /** Si "true", exige aussi l'authentification. */
    String requireAuth() default "true";
}
