package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRINT 6-bis - Lie un paramètre de méthode à un paramètre de la requête.
 *
 * Exemple :
 *   get(@RequestParam("id") int id, @RequestParam("nom") String nom)
 *
 * "required" permet de vérifier l'existence de l'argument : si le paramètre est
 * requis mais absent, le framework lève une erreur explicite.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {

    /** Nom du paramètre dans la requête (par défaut : le nom du paramètre Java). */
    String value() default "";

    /** Si true, le paramètre est obligatoire. */
    boolean required() default true;
}
