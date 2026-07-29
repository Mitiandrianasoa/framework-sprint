package com.sprint.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SPRINT 6     - Gestion des URL paramétrées, ex: "/etudiant/{id}".
 * SPRINT 6-ter - Détermination des {} d'une URL : on identifie précisément les
 *                paramètres présents, on gère le cas multi-paramètres
 *                ("/etudiant/{id}/note/{noteId}") et on valide que le motif est
 *                bien formé (accolades équilibrées, pas de {} vide, pas de doublon).
 *
 * Transforme un motif d'URL contenant des {param} en expression régulière avec
 * groupes de capture nommés, afin de :
 *   - tester si une URL concrète correspond au motif (matches),
 *   - extraire les valeurs des paramètres (extractParameters).
 */
public class PathPattern {

    // Repère un segment {param} dans un motif d'URL.
    private static final Pattern PARAM_TOKEN = Pattern.compile("\\{([^}]*)\\}");

    private final String pattern;
    private final Pattern regexPattern;
    private final List<String> parameterNames;

    public PathPattern(String pattern) {
        this.pattern = pattern;
        this.parameterNames = new ArrayList<>();
        // SPRINT 6-ter : on DÉTERMINE d'abord les {} et on les valide,
        // avant de compiler le motif en expression régulière.
        extractParameterNames(pattern);
        validate(pattern, this.parameterNames);
        this.regexPattern = compilePattern(pattern);
    }

    /**
     * "/etudiant/{id}/note/{noteId}"
     *   -> "^/etudiant/(?<id>[^/]+)/note/(?<noteId>[^/]+)$"
     * (le remplacement est global : le multi-paramètres est géré nativement.)
     */
    private Pattern compilePattern(String pattern) {
        String regex = pattern.replaceAll("\\{([^}]+)\\}", "(?<$1>[^/]+)");
        return Pattern.compile("^" + regex + "$");
    }

    public boolean matches(String path) {
        return regexPattern.matcher(path).matches();
    }

    /**
     * Extrait les valeurs des paramètres pour une URL concrète.
     * Ex: "/etudiant/25" -> { id = "25" }
     */
    public Map<String, String> extractParameters(String path) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = regexPattern.matcher(path);
        if (matcher.matches()) {
            for (String paramName : parameterNames) {
                params.put(paramName, matcher.group(paramName));
            }
        }
        return params;
    }

    /**
     * SPRINT 6-ter : détermine les noms des paramètres présents dans le motif.
     */
    private void extractParameterNames(String pattern) {
        Matcher m = PARAM_TOKEN.matcher(pattern);
        while (m.find()) {
            parameterNames.add(m.group(1).trim());
        }
    }

    /**
     * SPRINT 6-ter : valide que la détermination des {} est cohérente.
     * Lève une IllegalArgumentException explicite si le motif est mal formé.
     */
    private void validate(String pattern, List<String> names) {
        long open = pattern.chars().filter(c -> c == '{').count();
        long close = pattern.chars().filter(c -> c == '}').count();
        if (open != close) {
            throw new IllegalArgumentException(
                    "Motif d'URL mal formé (accolades non équilibrées): " + pattern);
        }
        List<String> seen = new ArrayList<>();
        for (String name : names) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException(
                        "Paramètre {} vide dans le motif d'URL: " + pattern);
            }
            if (seen.contains(name)) {
                throw new IllegalArgumentException(
                        "Paramètre d'URL en double '" + name + "' dans: " + pattern);
            }
            seen.add(name);
        }
    }

    // --- SPRINT 6-ter : accès à la détermination des {} ---

    /** Noms des paramètres déterminés dans ce motif (copie). */
    public List<String> getParameterNames() {
        return new ArrayList<>(parameterNames);
    }

    /** Nombre de paramètres {} déterminés. */
    public int getParameterCount() {
        return parameterNames.size();
    }

    /** Ce motif contient-il au moins un paramètre {} ? */
    public boolean hasParameters() {
        return !parameterNames.isEmpty();
    }

    /** Détermine, pour une URL brute, si elle contient un paramètre {}. */
    public static boolean hasPathVariables(String url) {
        return url != null && PARAM_TOKEN.matcher(url).find();
    }

    public String getPattern() {
        return pattern;
    }
}
