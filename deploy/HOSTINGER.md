# Déploiement Hostinger (front + back + reverse proxy)

Ce dossier propose une stack Docker unique (backend, frontend, PostgreSQL et reverse proxy) ainsi
qu'un `nginx.conf` prêt à servir deux sous-domaines :

- `api.example.com` → backend Spring Boot
- `app.example.com` → frontend Angular SSR

> ⚠️ Remplacez `api.example.com` et `app.example.com` par vos vrais domaines dans
> `deploy/nginx/conf.d/hostinger.conf`.

## 1) Fichier `.env`

Créez un fichier `.env` à la racine du repo avec les variables suivantes :

```bash
DB_NAME=jlh_autopam
DB_USERNAME=postgres
DB_PASSWORD=motdepassefort
```

## 2) Démarrage de la stack

```bash
docker compose -f docker-compose.hostinger.yml up -d
```

## 3) Ports exposés

- Nginx reverse proxy : **80** (public)
- Backend : **8080** (interne au réseau Docker)
- Frontend SSR : **4000** (interne au réseau Docker)
- PostgreSQL : **5432** (interne au réseau Docker)

## 4) Déploiement sur VPS Hostinger

1. Installez Docker + Compose sur le VPS.
2. Copiez ce repo (ou uniquement le dossier `deploy/` + `docker-compose.hostinger.yml`).
3. Ouvrez les ports 80/443 dans le firewall VPS.
4. Configurez vos DNS :
   - `api.example.com` → IP du VPS
   - `app.example.com` → IP du VPS

## 5) HTTPS (recommandé)

Ajoutez un service de certificats (ex: Caddy, Traefik ou Certbot) pour TLS. Ce fichier Nginx
est compatible avec un proxy TLS en amont.
