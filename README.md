# Sprint Framework

Framework web Java léger (style « Spring simplifié »), construit **sprint par sprint**.
Chaque sprint est développé dans sa propre branche et intégré via une *pull request* dédiée,
afin que l'on puisse suivre l'évolution du framework étape par étape.

## Prérequis
- Java 17
- Maven 3.9+

## Compilation
```bash
mvn clean compile
```

## Progression des sprints

| Sprint | Objectif |
|--------|----------|
| 1 | Recherche de ressource (scan du classpath) |
| 2 | Annotation `@Controller` portant un commentaire |
| 2-bis | Scan des classes annotées + affichage terminal |
| 3 | `FrontServlet` : contrôleurs, URL, routes conservées dans `init()` |
| 4 | Exécution de l'URL + retour `String` |
| 4-bis | `ModelView` (vue) |
| 5 | `ModelView.data` : `Map<String, Object>` |
| 6 | Paramètres d'URL `/etudiant/{id}`, `@RequestParam` |
| 7 | Méthodes HTTP `@Get` / `@Post` |
| 8 | Binding d'objets (`EntityBinder`) |
| 9 | API REST + JSON (`JsonResponse`) |
| 10 | Upload de fichiers (`MultipartFile`) |
| 11 | Session basée sur `Map` (sans `HttpSession`) |
| 11-bis | Gestion des rôles et sécurité |
