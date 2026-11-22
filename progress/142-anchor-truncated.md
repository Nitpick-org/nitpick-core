# Ticket 142-anchor-truncated

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C finitions
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/.../ui/components/NitpickReview.kt

## Journal

- 02:05 — trouvé par la lane UX de l'audit (#130) : `"$filePath:$line"` en une ligne avec ellipse de fin, la capture `dogfooding-104-review.png` montre « JGitRepository.kt:2… ».
- 02:10 — `TextOverflow.StartEllipsis` : l'ellipse mange le début du chemin (les dossiers), jamais le nom du fichier ni le numéro de ligne. Une ligne, desktop et téléphone (même carte).

## Incertain

- Rien.
