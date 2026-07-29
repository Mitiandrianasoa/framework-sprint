package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 7 - Mappe une méthode de contrôleur sur une requête HTTP POST.
 * Version spécialisée de @Test pour le verbe POST.
 *
 * Exemple : @Post("/etudiant")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {
    String value() default "";
}
