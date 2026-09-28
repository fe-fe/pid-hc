# PID-HC

App Jakarta EE (JSF + Hibernate) rodando em WildFly 39 / JDK 21, com Postgres 16.

## Pré-requisitos

- Docker + Docker Compose
- Arquivo `.env` na raiz (não versionado)

## .env

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=pidhc
DB_USER=pidhc_user
DB_PASSWORD=pidhc_pass
```

> Os valores de `DB_NAME`, `DB_USER` e `DB_PASSWORD` **não podem ser trocados à vontade**:
> o datasource em `src/main/webapp/WEB-INF/pidhc-ds.xml` tem essas credenciais fixas no XML
> (`jdbc:postgresql://postgres:5432/pidhc`, user `pidhc_user`, senha `pidhc_pass`).
> Se mudar no `.env` sem mudar no `pidhc-ds.xml`, o WildFly sobe mas não conecta no banco.
> `DB_HOST` e `DB_PORT` só são repassados ao container do WildFly — hoje nada os lê.

## Subir (normal)

```bash
docker compose up --build
```

Build multi-stage: Maven empacota o WAR e o Dockerfile copia para os deployments do WildFly.
Qualquer alteração (inclusive em XHTML) exige rebuild.

## Subir (dev)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

`docker-compose.dev.yml` é um override: troca para o `Dockerfile.dev`, que explode o WAR nos
deployments, e monta por bind `faces-views`, `templates`, `components` e `resources` do host.
Com isso XHTML e estáticos são refletidos sem rebuild (basta recarregar a página —
`FACELETS_REFRESH_PERIOD` está em 0). Mudança em código Java continua exigindo rebuild.

## Acesso

- App: http://localhost:8080/pid-hc/
- Postgres: `localhost:5432`

O schema é criado/atualizado pelo Hibernate (`hbm2ddl.auto=update`).

## Derrubar

```bash
docker compose down          # normal
docker compose down -v       # apaga também o volume do banco
```
