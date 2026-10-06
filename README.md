# PID-HC

App Jakarta EE (JSF + Hibernate) rodando em WildFly 39 / JDK 21, com Postgres 16.

## .env

```env
DB_NAME=pidhc
DB_USER=pidhc_user
DB_PASSWORD=<senha>
DOMAIN=pid.maria.qa   # só usado com o Caddy
```

## Arquivos

| Arquivo | Uso |
|---|---|
| `Dockerfile` | Build multi-stage: Maven empacota o WAR e copia para os deployments do WildFly. |
| `Dockerfile.dev` | Igual, mas explode o WAR para permitir bind de XHTML e estáticos. |
| `configure.cli` | Configuração do WildFly aplicada no build da imagem. |
| `docker-compose.yml` | Base: Postgres + WildFly. |
| `docker-compose.dev.yml` | Override de dev. |
| `docker-compose.caddy.yml` | Override que adiciona o Caddy (HTTPS). |
| `Caddyfile` | Configuração do Caddy. |

## WildFly

O `configure.cli` roda durante o build da imagem e:

- cria o datasource `java:/pidhcDS`, que lê `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e
  `DB_PASSWORD` do ambiente do container quando o WildFly sobe (`DB_HOST` e `DB_PORT` vêm do
  `docker-compose.yml`, o resto do `.env`);
- liga `proxy-address-forwarding`, para o WildFly respeitar os cabeçalhos `X-Forwarded-*` de um
  proxy reverso (Caddy, Nginx, Apache).

O schema é criado/atualizado pelo Hibernate (`hbm2ddl.auto=update`).

## Subir (normal)

```bash
docker compose up --build
```

Qualquer alteração (inclusive em XHTML) exige rebuild. O WildFly fica exposto só em
`127.0.0.1:8080` e o Postgres não expõe porta. Em servidor que já tem Nginx/Apache, aponte o
proxy para `127.0.0.1:8080`.

## Subir (dev)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Usa o `Dockerfile.dev` e monta por bind `faces-views`, `templates`, `components` e `resources`
do host. XHTML e estáticos são refletidos sem rebuild (basta recarregar a página —
`FACELETS_REFRESH_PERIOD` está em 0). Mudança em código Java continua exigindo rebuild.
Também expõe o Postgres em `localhost:5432`.

## Subir com Caddy (HTTPS)

Para servidor sem proxy reverso próprio. Requer `DOMAIN` no `.env`, com o DNS apontando para o
IP da máquina e as portas 80/443 liberadas.

```bash
docker compose -f docker-compose.yml -f docker-compose.caddy.yml up -d --build
```

O Caddy recebe as requisições em 80/443, redireciona HTTP para HTTPS e repassa para
`wildfly:8080`. Ele emite e renova o certificado (Let's Encrypt) sozinho; os certificados ficam
no volume `caddy_data`.

## Acesso

- Local: http://localhost:8080/pid-hc/
- Com Caddy: https://$DOMAIN/pid-hc/

## Derrubar

```bash
docker compose down          # normal
docker compose down -v       # apaga também o volume do banco
```
