package com.sprint.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPRINT 4-bis - Vue à afficher.
 * SPRINT 5      - Ajout des données à transmettre à la vue : Map<String, Object> data.
 * SPRINT 8      - Détection du type de données (string / list / map / object).
 *
 * Un contrôleur retourne un ModelView contenant :
 *   - le nom de la vue (JSP) à afficher,
 *   - un dictionnaire de données (clé -> valeur) exposé à la vue.
 *
 * Exemple :
 *   ModelView mv = new ModelView("test");
 *   mv.addObject("message", "Bonjour");
 *   return mv;
 */
public class ModelView {

    private String view;
    private Map<String, Object> data;
    private String dataType = "string"; // type de données par défaut

    public ModelView(String view) {
        this.view = view;
        this.data = new HashMap<>();
    }

    public ModelView(String view, Map<String, Object> data) {
        this.view = view;
        this.data = (data != null) ? data : new HashMap<>();
        detectDataType();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    /**
     * Ajoute une donnée à exposer dans la vue. Retourne this pour le chaînage.
     */
    public ModelView addObject(String key, Object value) {
        this.data.put(key, value);
        detectDataType(); // SPRINT 8 : re-détecter le type après ajout
        return this;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public Object getObject(String key) {
        return this.data.get(key);
    }

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    public String getDataType() {
        return dataType;
    }

    /**
     * SPRINT 8 : détecte le type dominant des données.
     * String -> "string", et si une valeur est une Map/List/objet -> type associé.
     */
    private void detectDataType() {
        if (data == null || data.isEmpty()) {
            this.dataType = "string";
            return;
        }
        for (Object value : data.values()) {
            if (value instanceof Map) {
                this.dataType = "map";
                return;
            } else if (value instanceof List) {
                this.dataType = "list";
                return;
            } else if (value instanceof String) {
                this.dataType = "string";
            } else if (value != null) {
                this.dataType = "object";
                return;
            }
        }
    }
}
