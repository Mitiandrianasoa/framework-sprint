package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 3 - Annotation d'URL posée sur une méthode de contrôleur.
 *
 * On associe une URL à une méthode : le FrontServlet scanne ces annotations au
 * démarrage et construit la table de routage (URL -> méthode).
 *
 * Exemple : @Test("/hello")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

    /**
     * Chemin de l'URL à mapper (ex: "/hello", plus tard "/etudiant/{id}").
     */
    String value() default "";
}
