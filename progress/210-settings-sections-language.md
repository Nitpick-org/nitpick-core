# Ticket 210 — Settings: working sections, language, About

- Statut : fait, à valider à l'écran
- Propriétaire humain : @pepito2t (H3)
- Piste : B desktop + C android (langue) 
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/settings/{SettingsDialog,SettingsScreen}.kt`, `ui/App.kt`, `ui/AppLocale*.kt`
  (commonMain + jvmMain + androidMain), `settings/Settings.kt` (`language`), strings EN/FR, `SettingsTest`

## Fait

- Dialogue desktop : la navigation de gauche fonctionne (sélection, surlignage, filtre de recherche) ;
  sections General (thème, langue), Review (fournisseur, modèle, clé), Git (compte), Sharing (URL
  serveur), About (icône, tagline, lien du dépôt, les trois GitHub de l'équipe avec la marque GitHub,
  cliquables). Keymap supprimé : aucune raccourci configurable.
- Langue : `Settings.language` (System / English / Français), contrôle segmenté desktop et téléphone.
  Mécanisme : `LocalAppLocale` (expect/actual, workaround documenté par JetBrains dans
  « Manage local resource environment ») + `key(language)` autour du thème pour relire toutes les
  chaînes. Desktop : `Locale.setDefault` ; Android : locale + configuration des ressources.
- Tests : `SettingsTest` (défaut System, tag fr, aller-retour fichier avec la langue).

## Journal

- 02:55 — worktree, modèle, locale par plateforme, dialogue, écran téléphone, check vert.
