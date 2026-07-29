package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 7 - Mappe une méthode de contrôleur sur une requête HTTP GET.
 * Version spécialisée de @Test pour le verbe GET.
 *
 * Exemple : @Get("/etudiant/{id}")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Get {
    String value() default "";
}
