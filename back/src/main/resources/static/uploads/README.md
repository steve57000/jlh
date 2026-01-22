# Dossier d'uploads statiques (démo)

Les fichiers présents ici servent de **fallback** en mode démo. Quand le profil
`demo` est actif (ou si `app.uploads.static-fallback=true`), l'API sert d'abord
les fichiers du répertoire d'upload configuré, puis bascule sur ce dossier si le
fichier est introuvable.
