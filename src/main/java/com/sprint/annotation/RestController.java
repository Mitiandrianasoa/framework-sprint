package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 9 - Marque un contrôleur comme REST : ses méthodes renvoient du JSON
 * au lieu d'une vue JSP.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {

    /** Préfixe appliqué à toutes les routes du contrôleur (ex: "/api"). */
    String value() default "";
}
