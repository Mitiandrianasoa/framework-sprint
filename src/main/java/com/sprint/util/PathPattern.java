package com.sprint.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SPRINT 6 - Gestion des URL paramétrées, ex: "/etudiant/{id}".
 *
 * Transforme un motif d'URL contenant des {param} en expression régulière avec
 * groupes de capture nommés, afin de :
 *   - tester si une URL concrète correspond au motif (matches),
 *   - extraire les valeurs des paramètres (extractParameters).
 */
public class PathPattern {

    private final String pattern;
    private final Pattern regexPattern;
    private final List<String> parameterNames;

    public PathPattern(String pattern) {
        this.pattern = pattern;
        this.parameterNames = new ArrayList<>();
        this.regexPattern = compilePattern(pattern);
        extractParameterNames(pattern);
    }

    /**
     * "/etudiant/{id}" -> "^/etudiant/(?<id>[^/]+)$"
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

    private void extractParameterNames(String pattern) {
        Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(pattern);
        while (m.find()) {
            parameterNames.add(m.group(1));
        }
    }

    public String getPattern() {
        return pattern;
    }
}
