# Ticket 80-tool-activity

- Statut : prêt pour PR
- Propriétaire humain : @dovatgabriel (H1) ; implémentation H2
- Piste : A + B
- Agent : lead
- Fichiers : `domain/{Review,ReviewToolUse}.kt`, `review/{LlmReviewAgent,ReviewToolbox}.kt`,
  `ui/repository/ReviewPanel.kt`, `commonTest/.../LlmReviewAgentTest.kt`, strings

## Journal

- 23:25 — `ReviewToolUse(tool, path)` passe dans `ReviewResult` (défaut vide, compatible
  avec les reviews partagées existantes). `LlmReviewAgent` collecte les appels de sa toolbox.
- 23:28 — carte résumé Review : ligne « Tools used: Pricing.kt » (chemins distincts, sans
  contenu ni clé), visible seulement si des outils ont servi.
- 23:30 — test : un client tool-capable qui lit `Main.kt` produit
  `ReviewToolUse("read_file","Main.kt")` dans le résultat.

## Décisions

- `toolUses` est un champ du résultat, pas un état UI séparé : une review partagée garde
  la trace de ses outils.
- Pas de compteur grammatical pour l’instant : on liste les chemins, ce qui évite le bug
  de pluriel #90.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **137 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- Android non rejoué (SDK absent).

## Incertain

- La ligne peut être longue sur un petit écran ; limitée à 2 lignes et aux chemins
  distincts.
- `ReviewResult` gagne un champ sérialisé : les anciens JSON restent lisibles grâce au
  défaut, à vérifier côté serveur de partage (#46/#48).
