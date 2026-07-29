package com.sprint.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * SPRINT 1 - Recherche de ressource.
 *
 * Utilitaire qui parcourt le classpath pour retrouver ("rechercher") toutes les
 * classes contenues dans un package donné. C'est la brique de base du framework :
 * tout le reste (scan des annotations, des contrôleurs...) s'appuie dessus.
 */
public class PackageScanner {

    /**
     * Retourne la liste des classes présentes dans un package.
     *
     * @param packageName ex: "com.sprint.controller"
     * @return la liste des classes trouvées sur le classpath
     */
    public static List<Class<?>> getClasses(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // Un package "com.sprint.x" correspond au dossier "com/sprint/x" sur le disque
        String path = packageName.replace('.', '/');

        // Recherche de la ressource (le dossier) sur le classpath
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> directories = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            directories.add(new File(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : directories) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Parcours récursif d'un dossier pour charger toutes les classes .class.
     */
    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Sous-package -> on descend récursivement
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    // "Etudiant.class" -> nom pleinement qualifié "com.sprint.x.Etudiant"
                    String className = packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }
}
