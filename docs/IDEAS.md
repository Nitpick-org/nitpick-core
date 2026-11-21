# Idées hors périmètre

> Bonnes idées qui ne sont pas dans le PRD. On les relit à H12 ; sinon elles vont dans le
> pitch « et après ». **Les agents peuvent ajouter une idée ici, mais ne l'implémentent jamais
> sans validation humaine.**

## Format

```md
## <titre>

- **Pour** : <personne / problème>
- **Ce que ça ferait** : <une phrase>
- **Pourquoi c'est intéressant** : <nouveauté / impact>
- **Coût estimé** : <heures>
- **Verdict** : à garder pour le pitch / à oublier
```

## Idées

- …

## « Sign in with GitHub » sur le téléphone (OAuth device flow)

- **Pour** : l'utilisateur mobile, qui tape aujourd'hui un token d'accès dans l'écran Connect.
- **Ce que ça ferait** : un bouton « Se connecter avec GitHub / GitLab » ouvre le navigateur, l'app récupère le token par le device flow et le range dans les réglages ; plus rien à copier-coller.
- **Pourquoi c'est intéressant** : même modèle qu'Air Teams (l'accès au dépôt passe par ton compte forge), sans saisie manuelle ; démo plus fluide.
- **Coût estimé** : 2 h (OAuth App GitHub + device flow Ktor client + écran), GitLab 1 h de plus.
- **Verdict** : après la démo ; le token saisi suffit pour LauzHack.
