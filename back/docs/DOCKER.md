# Commandes Docker et configuration

Ce guide décrit les piles Docker de **JLH AutoPam** telles qu'elles sont codées dans `docker-compose.dev.yml` et `docker-compose.prod.yml`. Chaque section précise les variables attendues, les services démarrés et les commandes utiles pour diagnostiquer l'environnement.

---

## 1. Prérequis

- Docker Engine ≥ 24 + plugin Compose (`docker compose`)
- 4 Gio de RAM libres (MySQL + JVM + Nginx)
- Un fichier `.env` ou des variables exportées contenant **au minimum** :
  ```bash
  DB_NAME=jlh_autopam
  DB_USERNAME=root        # utilisé par le backend en dev
  DB_PASSWORD=password    # root password MySQL + backend
  ```
  Vous pouvez ajouter `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `APP_UPLOAD_DIR` si vous souhaitez surcharger la configuration Spring.

---

## 2. Nettoyage rapide de l'environnement Docker

```bash
# Arrêter/supprimer les piles existantes
docker compose -f ../docker-compose.prod.yml down
docker compose -f ../docker-compose.dev.yml  down

# Nettoyer les ressources orphelines (utiliser avec précaution)
docker container prune -f
docker volume prune -f
docker image prune -a -f
docker network prune -f
```

---

## 3. Stack de développement (`docker-compose.dev.yml`)

### Services et volumes
- **db** : MySQL 8.0 avec volume `db-data` et healthcheck `mysqladmin ping`. Les identifiants proviennent de `DB_NAME`/`DB_PASSWORD`.【F:docker-compose.dev.yml†L1-L16】
- **mailhog** : capture tous les e-mails sortants (`SMTP 1025`, UI `http://localhost:8025`).【F:docker-compose.dev.yml†L17-L28】
- **backend** : construit l'image via le `Dockerfile` (cible `runtime`), monte `./src` et `./pom.xml` pour permettre le rechargement à chaud, partage `promo-images` + `./uploads`, et conserve un cache Maven via `m2-repo`. Les variables `SPRING_PROFILES_ACTIVE=dev`, `DB_*` et `MAIL_*` sont injectées telles quelles dans Spring.【F:docker-compose.dev.yml†L29-L66】
- **nginx** : sert les fichiers `./uploads` (montés en lecture seule) et reverse-proxy vers le backend (`http://localhost`).【F:docker-compose.dev.yml†L67-L75】
- **Volumes** : `db-data`, `promo-images`, `m2-repo` sont déclarés en bas du fichier.【F:docker-compose.dev.yml†L78-L81】

### Démarrer / arrêter
```bash
docker compose -f docker-compose.dev.yml up --build
# ou pour détacher :
docker compose -f docker-compose.dev.yml up --build -d
# stop
docker compose -f docker-compose.dev.yml down
```

### Services exposés
| Service   | URL / Port                | Notes |
|-----------|--------------------------|-------|
| Backend   | http://localhost:8080    | Profil `dev`, dépend du schéma `schema_jlh_autopam.sql` + `data.sql`. |
| MySQL     | localhost:3306           | root / `$DB_PASSWORD`. |
| MailHog   | SMTP 1025 / UI 8025      | Vérifier tous les e-mails sortants dans l'onglet "Messages". |
| Nginx     | http://localhost         | Sert `/promotions/images/**` depuis `./uploads`. |

### Commandes utiles
```bash
# Journaux temps réel
docker compose -f docker-compose.dev.yml logs -f backend

# Se connecter au MySQL embarqué
docker compose -f docker-compose.dev.yml exec db \
  mysql -uroot -p"$DB_PASSWORD" "$DB_NAME"

# Lancer un shell dans le conteneur backend (profil dev)
docker compose -f docker-compose.dev.yml exec backend /bin/bash
```

### Stockage des fichiers
- Les uploads sont écrits dans `/var/www/promo` **dans** le conteneur (`app.upload-dir`). Ce chemin est relié à la fois au volume `promo-images` et au dossier local `./uploads`, ce qui permet à Nginx de servir immédiatement les images enregistrées par l'API.【F:docker-compose.dev.yml†L35-L39】【F:src/main/resources/application.yml†L7-L21】

---

## 4. Stack de production (`docker-compose.prod.yml`)

### Services
- **db** : MySQL 8.0 persistant (`db-data`) avec healthcheck plus large (intervalle 30 s / timeout 10 s).【F:docker-compose.prod.yml†L1-L18】
- **backend** : image `steve57/jlh-autopam-backend:latest`, profil `prod`, variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, port 8080 publié. Monte le volume `promo-images` pour partager les médias avec Nginx.【F:docker-compose.prod.yml†L20-L36】
- **nginx** : frontal `nginx:alpine` qui expose le port 80 et partage `promo-images` + les fichiers de configuration `nginx/*.conf`.【F:docker-compose.prod.yml†L38-L50】
- **Volumes & réseau** : `db-data`, `promo-images` et le réseau bridge `backend` sont déclarés en bas du fichier.【F:docker-compose.prod.yml†L52-L57】

### Démarrer, mettre à jour, arrêter
```bash
# Démarrage détaché
docker compose -f docker-compose.prod.yml up -d

# Arrêt complet
docker compose -f docker-compose.prod.yml down

# Récupérer la dernière image backend et redémarrer uniquement ce service
docker compose -f docker-compose.prod.yml pull backend
docker compose -f docker-compose.prod.yml up -d backend
```

### Configuration Spring en production
- Le backend lit `SPRING_PROFILES_ACTIVE=prod` et les variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` pour se connecter à MySQL, puis `app.upload-dir`/`app.images.base-url` pour publier les images via Nginx.【F:docker-compose.prod.yml†L20-L36】【F:src/main/resources/application-prod.properties†L1-L21】
- Les secrets SMTP peuvent être fournis via `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `SMTP_PASSWORD` (transmis au conteneur par Compose).【F:src/main/resources/application-prod.properties†L15-L21】

---

## 5. Build manuel de l'image backend (optionnel)

Pour tester localement la même image qu'en CI/CD :
```bash
docker build --target runtime -t jlh-autopam-backend:local .
```
Ce build reprend exactement le `Dockerfile` multi-étapes utilisé par Compose (téléchargement des dépendances Maven, compilation, copie du `app.jar`, healthcheck HTTP).【F:Dockerfile†L1-L30】

---

## 6. Dépannage rapide
- Vérifiez que les variables `DB_*` sont cohérentes entre le service `db` et `backend`, sinon MySQL montera mais Spring Boot échouera à se connecter.
- Si les uploads ne sont pas visibles via Nginx, assurez-vous que le dossier hôte `./uploads` existe (Compose le crée automatiquement) et qu'il contient les fichiers générés dans `/var/www/promo` côté backend.
- Les mails sortants en dev n'apparaissent que dans MailHog (`http://localhost:8025`). Si vous devez tester un SMTP réel, surcharger `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` dans `.env`.
