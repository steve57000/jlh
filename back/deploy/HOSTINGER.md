# Déploiement Hostinger (backend + DB + reverse proxy)

Ce dossier propose une stack Docker **backend** unique (Spring Boot + PostgreSQL + reverse proxy)
et un `nginx.conf` prêt à servir le sous-domaine API :

- `api.example.com` → backend Spring Boot

Exemple avec votre domaine `jlh-autopam.fr` :

- `api.jlh-autopam.fr` → backend Spring Boot
- `app.jlh-autopam.fr` (ou `www.jlh-autopam.fr`) → frontend (déployé séparément)

> ⚠️ Remplacez `api.example.com` par votre vrai domaine dans
> `deploy/nginx/conf.d/hostinger.conf`.

## 1) Fichier `.env`

Créez un fichier `.env` à la racine du repo avec les variables suivantes (PostgreSQL) :

```bash
DB_HOST=db
DB_PORT=5432
DB_NAME=jlh_autopam
DB_USERNAME=postgres
DB_PASSWORD=motdepassefort
DB_URL=jdbc:postgresql://db:5432/jlh_autopam?sslmode=disable
APP_UPLOAD_DIR=/var/www/promo
SPRING_PROFILES_ACTIVE=prod
```

Des exemples prêts à copier sont disponibles dans :
- `deploy/.env.example`
- `deploy/.env.prod.example`

## 2) Démarrage de la stack backend

```bash
docker compose -f docker-compose.hostinger.yml up -d
```

## 3) Ports exposés

- Nginx reverse proxy : **80** (public)
- Backend : **8080** (interne au réseau Docker)
- PostgreSQL : **5432** (interne au réseau Docker)

## 4) Déploiement sur VPS Hostinger

1. Installez Docker + Compose sur le VPS.
2. Copiez ce repo (ou uniquement le dossier `deploy/` + `docker-compose.hostinger.yml`).
3. Ouvrez les ports 80/443 dans le firewall VPS.
4. Configurez vos DNS :
   - `api.example.com` → IP du VPS (backend)
   - `app.example.com` → IP de votre hébergement frontend (même VPS ou autre)

## 5) Créer les sous-domaines chez Hostinger

Dans Hostinger (DNS Zone) :

1. **Ajouter un enregistrement A** pour `api` qui pointe vers l'IP du VPS.
2. **Ajouter un enregistrement A** pour `app` (ou `www`) qui pointe vers l'IP du frontend.
3. Attendre la propagation DNS (quelques minutes à quelques heures).

Pour `jlh-autopam.fr`, cela donne :

- `api.jlh-autopam.fr` → A → IP du VPS backend
- `app.jlh-autopam.fr` → A → IP/serveur du frontend

## 6) HTTPS (recommandé)

Ajoutez un service de certificats (ex: Caddy, Traefik ou Certbot) pour TLS. Ce fichier Nginx
est compatible avec un proxy TLS en amont.

## 7) Déploiement recommandé : CI/CD + registre d’images

Le flux le plus fiable avec un VPS est :
1. **CI/CD GitHub Actions** → build de l’image backend → push sur un registre
   (Docker Hub, GHCR, etc.).
2. **VPS Hostinger** → `docker compose pull` puis `up -d`.

Vous pouvez définir l’image backend à tirer via une variable d’environnement :
```bash
BACKEND_IMAGE=ghcr.io/votre-org/jlh-autopam-backend:prod
```

Sur le VPS :
```bash
docker login ghcr.io
docker compose -f docker-compose.hostinger.yml pull
docker compose -f docker-compose.hostinger.yml up -d
```

Ce workflow évite de copier le code source sur le VPS, et vous garantit des
déploiements reproductibles.

## 8) Déploiement frontend (indépendant)

Vous avez deux options :
1. **Frontend sur un autre VPS/serveur** (recommandé si le front est dans un repo dédié).
2. **Frontend sur le même VPS**, dans un dossier séparé, avec son propre
   `docker-compose.yml` + reverse proxy.

Dans tous les cas, gardez un sous-domaine dédié :
- `api.jlh-autopam.fr` → backend (ce repo)
- `app.jlh-autopam.fr` → frontend (repo front)

## 9) CI/CD avec 2 repos séparés (front + back)

Si vous déployez depuis **deux repos distincts** :

1. **Repo backend** : build l'image → push sur un registre (tag `:prod` ou SHA, évitez `:latest` si votre registre rend ce tag immuable).
2. **Repo frontend** : build l'image SSR → push sur un registre (tag `:prod` ou SHA).
3. **Sur le VPS** : utilisez un `docker-compose.hostinger.yml` qui référence
   l’image backend (même sans code source présent).
4. **Mise à jour** :
   ```bash
   docker compose -f docker-compose.hostinger.yml pull
   docker compose -f docker-compose.hostinger.yml up -d
   ```

Astuce : vous pouvez déclencher ces commandes via un pipeline SSH (GitHub Actions,
GitLab CI, etc.).
