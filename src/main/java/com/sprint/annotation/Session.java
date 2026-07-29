package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 11 - Injecte la session (une Map) dans un paramètre de contrôleur.
 *
 * Exemple :
 *   public String page(@Session Map&lt;String,Object&gt; session) { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Session {

    /** Nom de la session (par défaut "default"). */
    String value() default "default";

    /** Crée la session si elle n'existe pas encore. */
    boolean create() default true;
}
