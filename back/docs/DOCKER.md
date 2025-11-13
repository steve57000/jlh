# Commandes Docker et configuration

Ce document liste les commandes Docker indispensables pour gérer les environnements de développement et de production de l'application **JLH AUTO PAM**, ainsi que l'installation d'un conteneur Nginx pour la gestion des images.

---

## Prérequis

* Docker
* Docker Compose

---

## 1. Nettoyage de l'environnement Docker

Avant de repartir sur une base propre, exécutez :

```bash

# Arrêter et supprimer les conteneurs de production
docker-compose -f ../docker-compose.prod.yml down

# Arrêter et supprimer les conteneurs de développement
docker-compose -f ../docker-compose.dev.yml down

# Supprimer les conteneurs orphelins
docker container prune -f

# Supprimer les volumes inutilisés (ATTENTION : supprime les données)
docker volume prune -f

# Supprimer les images inutilisées
docker image prune -a -f

# Supprimer les réseaux inutilisés
docker network prune -f
```

---

## 2. Environnement de développement

Pour lancer la stack de développement (API, base MySQL, interface Adminer) :

```bash
  docker-compose -f ../docker-compose.dev.yml up --build
```

* **API** : [http://localhost:8080](http://localhost:8080)
* **MySQL** : 127.0.0.1:3306 (utilisateur `root`, mot de passe `password`)
* **Adminer** : [http://localhost:8081](http://localhost:8081)
* **MailHog** :
* SMTP :
* mailhog:1025 Interface Web : http://localhost:8025
* **Nginx** (images statiques) : http://localhost

Tests emails (MailHog)

En développement, tous les emails envoyés par l’application Spring Boot sont interceptés par MailHog :
ils ne sortent pas réellement, mais sont consultables sur http://localhost:8025
.

Cela permet de vérifier :

* l’objet et le contenu HTML,

* les variables dynamiques (nom, lien de vérification…),

* les pièces jointes.

Exemple de configuration dans application-dev.properties :

```code
spring.mail.host=${MAIL_HOST:mailhog}
spring.mail.port=${MAIL_PORT:1025}
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

### Journaux (logs)

```bash
  docker-compose -f ../docker-compose.dev.yml logs -f backend
```

### Arrêter les conteneurs de développement

```bash
  docker-compose -f ../docker-compose.dev.yml down
```

---

## 3. Environnement de production

Démarrer la stack de production en arrière-plan :

```bash
  docker-compose -f ../docker-compose.prod.yml up -d
```

Arrêter et supprimer les conteneurs de production :

```bash
  docker-compose -f ../docker-compose.prod.yml down
```

Mettre à jour l’image backend et redémarrer le service :

```bash
  docker-compose -f ../docker-compose.prod.yml pull backend
  docker-compose -f ../docker-compose.prod.yml up -d backend
```

---

## 4. Conteneur Nginx pour le stockage et la diffusion des images

Nous utilisons Nginx en frontal pour :

* Servir rapidement les fichiers statiques (images et icônes)
* Gérer le cache et les en-têtes HTTP

### Ajouter un service `nginx` à `docker-compose` (dev et prod)

```yaml
  nginx:
    image: nginx:alpine
    restart: unless-stopped
    ports:
      - "80:80"
    volumes:
      - promo-images:/var/www/promo      # mêmes images que l’API
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
    depends_on:
      - backend
```

#### Exemple de configuration (`nginx/conf.d/promotions.conf`)

```nginx
server {
  listen 80;
  server_name _;

  location /promotions/images/ {
    alias /var/www/promo/;
    expires 1h;
    add_header Cache-Control "public";
  }

  location /icons/ {
    alias /var/www/promo/icons/;
    expires 30d;
    add_header Cache-Control "public";
  }

  location / {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

* **Volume `promo-images`** partagé entre l’API et Nginx pour stocker les fichiers éphémères.
* **Cache court** (1h) pour les images de promotion, **cache long** (30j) pour les icônes.

---

## 5. Build manuel de l’image backend (optionnel)

```bash
# Construire uniquement l’étape runtime pour accélérer
docker build --target runtime -t jlh-autopam-backend:local .
```

---