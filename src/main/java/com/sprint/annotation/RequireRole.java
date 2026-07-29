package com.sprint.annotation;

import com.sprint.security.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 11-bis - Exige un rôle minimum pour accéder à une méthode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequireRole {

    /** Rôle requis. */
    Role value();

    /** Si true, accepte aussi les rôles de niveau supérieur. */
    boolean allowHigher() default true;

    /** Message d'erreur (%s = nom du rôle). */
    String message() default "Rôle requis: %s";

    /** URL de redirection en cas de refus. */
    String redirect() default "/access-denied";
}
