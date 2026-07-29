package com.sprint.util;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * SPRINT 8 - Binding automatique d'un objet (entité) depuis la requête.
 *
 * Traitement des données envoyées :
 *   - si le paramètre attendu est un type simple (String, int...), on convertit
 *     directement la valeur ;
 *   - si c'est un objet (une "entité" du package model), on crée une instance et
 *     on remplit ses champs à partir des paramètres de la requête portant le même nom.
 */
public class EntityBinder {

    /**
     * Crée une instance de entityClass et remplit ses champs depuis la requête.
     */
    public static Object bindEntity(HttpServletRequest request, Class<?> entityClass)
            throws Exception {

        Object entity = entityClass.getDeclaredConstructor().newInstance();

        // Récupérer les paramètres (nom -> valeur) de la requête
        Map<String, String[]> paramMap = request.getParameterMap();

        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true); // autoriser l'accès aux champs privés

            String[] values = paramMap.get(field.getName());
            if (values != null && values.length > 0 && !values[0].isEmpty()) {
                try {
                    Object converted = convertValue(values[0], field.getType());
                    field.set(entity, converted);
                } catch (Exception e) {
                    System.err.println("Erreur binding champ " + field.getName()
                            + ": " + e.getMessage());
                }
            }
        }
        return entity;
    }

    /**
     * Convertit une valeur textuelle vers le type cible.
     */
    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value.trim());
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value.trim());
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value.trim());
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value.trim());
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value.trim());
        return value;
    }

    /**
     * Une "entité" = une classe métier (package model/entity), hors classes
     * techniques du framework (ModelView...).
     */
    public static boolean isEntity(Class<?> clazz) {
        if (clazz.getPackage() == null) {
            return false;
        }
        String packageName = clazz.getPackage().getName().toLowerCase();
        if (packageName.contains("entity") || packageName.contains("model")) {
            String simpleName = clazz.getSimpleName();
            // Exclure les classes utilitaires du framework
            return !simpleName.equals("ModelView");
        }
        return false;
    }

    /**
     * Vérifie si une classe est un type simple (String, Integer, etc.).
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return clazz == String.class ||
               clazz == Integer.class || clazz == int.class ||
               clazz == Long.class || clazz == long.class ||
               clazz == Double.class || clazz == double.class ||
               clazz == Float.class || clazz == float.class ||
               clazz == Boolean.class || clazz == boolean.class ||
               clazz == Byte.class || clazz == byte.class ||
               clazz == Short.class || clazz == short.class ||
               clazz == Character.class || clazz == char.class;
    }
}
