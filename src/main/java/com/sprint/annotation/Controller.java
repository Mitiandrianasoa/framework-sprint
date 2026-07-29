package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 2 - Annotation à créer qui permet de porter/afficher un commentaire.
 *
 * On place cette annotation sur une classe (une "balise" au sens du sujet) pour
 * la marquer comme contrôleur. Elle transporte un commentaire dont la valeur par
 * défaut est exactement "This is the comments" (demandé par le sprint 2).
 *
 * @Retention(RUNTIME) : l'annotation reste lisible par réflexion à l'exécution,
 *                       ce qui permettra de la scanner au sprint 2-bis.
 * @Target(TYPE)       : elle s'applique à une classe.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    /**
     * Préfixe d'URL optionnel pour le contrôleur (ex: "/etudiant").
     */
    String value() default "";

    /**
     * Commentaire porté par l'annotation. Valeur par défaut = "This is the comments".
     */
    String comment() default "This is the comments";
}
