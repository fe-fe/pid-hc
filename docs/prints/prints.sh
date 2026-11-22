#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."
compose=(docker compose -p pid-hc-prints -f docker-compose.yml -f docs/prints/compose.yml)
base=http://127.0.0.1:8081

trap '"${compose[@]}" down -v' EXIT

"${compose[@]}" up -d --build

echo "Aguardando o WildFly subir..."
until curl -sf -o /dev/null "$base/pages/auth/login.xhtml"; do sleep 3; done

"${compose[@]}" exec -T postgres sh -c 'psql -v ON_ERROR_STOP=1 -q -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
  < docs/prints/dados.sql

codigo=$("${compose[@]}" logs --no-log-prefix wildfly | grep -oE '^\s*[A-Za-z0-9]+(-[A-Za-z0-9]+)+\s*$' | tail -1 | tr -d '[:space:]')

mkdir -p docs/telas
docker run --rm --network host -u "$(id -u):$(id -g)" -e HOME=/tmp \
  -e BASE="$base" -e CODIGO="$codigo" \
  -v "$PWD/docs/prints:/prints:ro" -v "$PWD/docs/telas:/telas" \
  mcr.microsoft.com/playwright:v1.49.1-noble \
  sh -c 'cd /tmp && npm i --silent --no-save playwright@1.49.1 >/dev/null && cp /prints/prints.mjs . && node prints.mjs'
