# Déploiement démo (Netlify + Render + PostgreSQL managé)

Ce document ajoute une **configuration démo** sans casser les paramètres existants (dev/prod).

## 1) PostgreSQL (Neon/Supabase)

Créez une base puis récupérez :

- `DB_HOST`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `DB_PORT`
- (optionnel) `DATABASE_URL` **ou** `DB_URL`

> Le backend accepte désormais `DB_USER` **ou** `DB_USERNAME`, et `DATABASE_URL` **ou** `DB_URL`.

## 2) Backend Spring Boot (Render)

### Render → variables d’environnement

Minimum :

```
SPRING_PROFILES_ACTIVE=demo
DB_HOST=...
DB_PORT=5432
DB_NAME=...
DB_USER=...
DB_PASSWORD=...
APP_BASE_URL=https://<ton-backend>.onrender.com
APP_FRONTEND_URL=https://<ton-frontend>.netlify.app
```

Optionnel :

```
DATABASE_URL=postgresql://...
APP_IMAGES_BASE_URL=https://<ton-backend>.onrender.com/promotions/images/
APP_FILES_BASE_URL=https://<ton-backend>.onrender.com
APP_UPLOAD_DIR=/tmp/jlh-upload
APP_EMAIL_ENABLED=false
```

### Notes

- Le profil `demo` utilise `ddl-auto=update` et **charge `data.sql`** (sécurisé par `ON CONFLICT DO NOTHING`).
- L’envoi d’e-mails est **désactivé par défaut** en démo (`APP_EMAIL_ENABLED=false`).

## 3) Frontend Angular (Netlify)

### Configuration démo

Le frontend a un environnement `demo` :

- `front/src/environments/environment.demo.ts`
- Build Netlify conseillé :

```
pnpm install
pnpm run build -- --configuration=demo
```

Ensuite, mettez les vraies URLs dans `environment.demo.ts` (API Render + media base).

## 4) Nginx / MailHog

- **Nginx** : optionnel en démo cloud (le backend peut servir les URLs d’images).
- **MailHog** : remplacé en démo (e-mails désactivés ou sandbox SMTP).
