# PID-HC

App Jakarta EE (JSF + Hibernate) rodando em WildFly 39 / JDK 21, com Postgres 16.

## .env

```env
DB_NAME=pidhc
DB_USER=pidhc_user
DB_PASSWORD=<senha>
DOMAIN=pid.maria.qa   # só usado com o Caddy
```

Opcionais, com o padrão de produção quando ausentes (o `docker-compose.dev.yml` define os de dev):

| Variável | Padrão | Dev |
|---|---|---|
| `PROJECT_STAGE` | `Production` | `Development` |
| `SHOW_SQL` | `false` | `true` |
| `FACELETS_REFRESH_PERIOD` | `-1` | `0` |

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
| `Makefile` | Atalhos: `make dev`, `make test`, `make migration nome=...`. |

## WildFly

O `configure.cli` roda durante o build da imagem e:

- cria o datasource `java:/pidhcDS`, que lê `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e
  `DB_PASSWORD` do ambiente do container quando o WildFly sobe (`DB_HOST` e `DB_PORT` vêm do
  `docker-compose.yml`, o resto do `.env`);
- liga `proxy-address-forwarding`, para o WildFly respeitar os cabeçalhos `X-Forwarded-*` de um
  proxy reverso (Caddy, Nginx, Apache);
- liga a substituição de `${env.VAR:padrão}` no `web.xml` e no `persistence.xml`, que leem as
  variáveis opcionais do `.env`.

O schema é criado e atualizado pelo Flyway quando o WildFly sobe, com as migrations de
`src/main/resources/db/migration` (`V2__descricao.sql`, `V3__...`). O Hibernate não altera o
banco (`hbm2ddl.auto=none`). Banco que já existia sem histórico do Flyway entra na versão 1 sem
rodar o `V1__esquema_inicial.sql`. Mudança em entidade precisa de uma migration nova; o
`MigracoesTest` falha se as migrations não baterem com as entidades.

Para gerar a migration de uma mudança em entidade (precisa de JDK 21 e Docker):

```bash
make migration nome=marca_site
```

Ele sobe um Postgres temporário, aplica as migrations existentes, compara com as entidades e grava
o SQL que falta no próximo `V<n>__marca_site.sql`. Revise o arquivo: renomear coluna sai como
apagar e criar, e coluna obrigatória em tabela com dados precisa ser preenchida antes do `NOT NULL`.

## Subir (normal)

```bash
docker compose up --build
```

Qualquer alteração (inclusive em XHTML) exige rebuild. O WildFly fica exposto só em
`127.0.0.1:8080` e o Postgres não expõe porta. Em servidor que já tem Nginx/Apache, aponte o
proxy para `127.0.0.1:8080`.

## Subir (dev)

```bash
make dev   # ou: docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
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

- Local: http://localhost:8080/
- Com Caddy: https://$DOMAIN/

## Primeiro acesso

Com o banco sem nenhum administrador ativo, o WildFly escreve no log um código de primeiro
acesso ao subir:

```bash
docker compose logs wildfly | grep -A4 "PRIMEIRO ACESSO"
```

Abra `/pages/auth/init` (a tela de login tem o link), informe o código e cadastre o
administrador. O código muda a cada reinício e deixa de valer assim que o administrador existe.

## Derrubar

```bash
docker compose down          # normal
docker compose down -v       # apaga também o volume do banco
```
