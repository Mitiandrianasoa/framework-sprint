package com.sprint.model;

/**
 * SPRINT 4-bis - Vue à afficher.
 *
 * Un contrôleur peut retourner un ModelView au lieu d'une simple String.
 * Le champ "view" désigne le nom de la page JSP à afficher.
 *
 * Exemple :
 *   ModelView mv = new ModelView("test");   // -> /WEB-INF/views/test.jsp
 *   return mv;
 */
public class ModelView {

    private String view;

    public ModelView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
