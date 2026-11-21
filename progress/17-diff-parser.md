# Ticket 17-diff-parser

- Statut : fini, à pousser et merger
- Propriétaire humain : @thomas
- Piste : A core
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/diff/**, shared/src/commonTest/kotlin/dev/nitpick/app/diff/**

## Journal

- 14:40 — branche `ticket/17-diff-parser`, ticket #17 assigné. TDD : fixtures de diff git
  (modifié 2 hunks, ajouté, supprimé, renommé, binaire, sans newline final, malformé,
  multi-fichiers) puis parseur.
- 15:00 — `parseUnifiedDiff(text): List<FileDiff>` dans `commonMain/diff`, 8 tests verts.
- Décisions :
  - Fonction pure de premier niveau, pas de classe : rien à injecter.
  - Les chemins viennent des lignes `---`/`+++` (repli sur l'en-tête `diff --git`) ;
    `/dev/null` donne un chemin absent, donc un fichier supprimé garde son ancien chemin.
  - `oldPath` n'est renseigné que pour un renommage.
  - Fin de hunk par décompte des lignes annoncées (`@@ -a,b +c,d @@`), avec `b`/`d` à 1 quand
    omis ; une ligne sans préfixe arrête le hunk et le parseur renvoie ce qu'il a compris.
  - `\ No newline at end of file` ignoré, numéros de ligne inchangés.
  - Texte sans en-tête `diff --git` → liste vide (JGit émet toujours cet en-tête).
- Fichiers : shared/src/commonMain/kotlin/dev/nitpick/app/diff/UnifiedDiffParser.kt,
  shared/src/commonTest/kotlin/dev/nitpick/app/diff/UnifiedDiffParserTest.kt.
- Incertain : chemins contenant ` b/` ou des guillemets (git les cite) mal découpés dans
  l'en-tête ; les lignes `---`/`+++` couvrent le cas courant. Marqué `shortcut:` dans le code.
- Blocage : aucun.
- Suivant : #19 (JGit `diffAgainst` branché sur ce parseur) et #20 (agent Koog) sont débloqués.
- 15:30 — review ponytail : fixture ajoutée pour le libellé JGit `Binary files differ` sans lignes `---`/`+++` (9 tests).
