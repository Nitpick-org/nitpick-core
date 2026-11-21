# Audit 106-cleanup

- Statut : PR partielle (items sûrs)
- Propriétaire humain : @zooldyyk
- Piste : UI/security cleanup
- Agent : lead
- Fichiers : `ui/components/NitpickReview.kt`, `ui/repository/RepositoryScreen.kt`,
  `review/ReviewContext.kt`, `androidApp/src/main/AndroidManifest.xml`, strings

## Fait

- Suppression des hints de raccourcis clavier (`⌘⇧R/F/L/P`) alors qu’aucun key handler
  n’existe.
- Carte review : `SUGGESTED CHANGE`, `Apply suggestion` et les libellés de sévérité
  passent par `composeResources` (EN/FR).
- Filtrage LLM : `*.pem`, `*.key`, `*.p12`, `*.jks`, `local.properties`.
- Android `allowBackup="false"`.

## Restant de #106 (non fait)

- Cycle `ui/phone` ↔ `ui/repository` pour les helpers de clone.
- Duplications `severityTone`, date-label, `selectedDiff`.
- Tests wizard `1+2==3`, `.editorconfig` 2 espaces.
→ ticket laissé ouvert.

## Vérifications

- `:shared:jvmTest` + `:desktopApp:compileKotlin` verts.
- Test des fichiers secrets ajouté.
