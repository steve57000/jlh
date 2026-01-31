#!/bin/sh
set -e

if [ -d /app/uploads-seed ]; then
  mkdir -p /var/www/promo
  cp -a -n /app/uploads-seed/. /var/www/promo/
fi

exec java -jar /app/app.jar
