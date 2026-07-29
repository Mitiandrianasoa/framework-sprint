package com.sprint.util;

import com.sprint.annotation.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * SPRINT 2-bis - Scanner les classes qui utilisent l'annotation @Controller
 * puis les afficher dans le terminal.
 *
 * On réutilise le PackageScanner du sprint 1 pour "rechercher" les classes, puis
 * on filtre celles qui portent l'annotation @Controller et on imprime leur nom
 * ainsi que le commentaire porté par l'annotation ("This is the comments").
 */
public class AnnotationScanner {

    /**
     * Retourne les classes annotées @Controller dans le package donné.
     */
    public static List<Class<?>> findControllers(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();
        for (Class<?> clazz : PackageScanner.getClasses(packageName)) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                controllers.add(clazz);
            }
        }
        return controllers;
    }

    /**
     * Scanne le package et affiche dans le terminal chaque classe @Controller
     * trouvée avec son commentaire.
     */
    public static void scanAndPrint(String packageName) throws Exception {
        System.out.println("=== SCAN DES CLASSES @Controller dans '" + packageName + "' ===");
        List<Class<?>> controllers = findControllers(packageName);

        if (controllers.isEmpty()) {
            System.out.println("Aucune classe annotée @Controller trouvée.");
        } else {
            int i = 1;
            for (Class<?> clazz : controllers) {
                Controller annotation = clazz.getAnnotation(Controller.class);
                System.out.println(i++ + ". " + clazz.getName()
                        + " -> comment = \"" + annotation.comment() + "\"");
            }
        }
        System.out.println("===============================================");
    }

    /**
     * Point d'entrée pour tester le scan en ligne de commande.
     */
    public static void main(String[] args) throws Exception {
        String packageName = (args.length > 0) ? args[0] : "com.sprint.controller";
        scanAndPrint(packageName);
    }
}
