package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 11-bis - Exige une permission précise pour accéder à une méthode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequirePermission {

    /** Permission requise. */
    String value();

    /** Permissions alternatives (une seule suffit). */
    String[] alternatives() default {};

    /** Message d'erreur (%s = permission). */
    String message() default "Permission requise: %s";

    /** URL de redirection en cas de refus. */
    String redirect() default "/access-denied";
}
