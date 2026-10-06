# PID-HC

App Jakarta EE (JSF + Hibernate) rodando em WildFly 39 / JDK 21, com Postgres 16.

## Pré-requisitos

- Docker + Docker Compose
- Arquivo `.env` na raiz (não versionado)

## .env

```env
DB_NAME=pidhc
DB_USER=pidhc_user
DB_PASSWORD=<senha>
DOMAIN=pid.maria.qa   # só usado com o Caddy
```

O datasource `java:/pidhcDS` é criado no `configure.cli` durante o build da imagem e lê
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` do ambiente do container quando o
WildFly sobe. `DB_HOST` e `DB_PORT` já são definidos no `docker-compose.yml` (`postgres:5432`).
Mudanças no `.env` só valem após recriar os containers (`up -d`); `restart` não relê o arquivo.

## Subir (normal)

```bash
docker compose up --build
```

Build multi-stage: Maven empacota o WAR e o Dockerfile copia para os deployments do WildFly.
Qualquer alteração (inclusive em XHTML) exige rebuild.

O WildFly fica exposto só em `127.0.0.1:8080` e o Postgres não expõe porta. Em servidor que já
tem Nginx/Apache, aponte o proxy para `127.0.0.1:8080` — o WildFly está com
`proxy-address-forwarding` ligado, então respeita os cabeçalhos `X-Forwarded-*`.

## Subir (dev)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

`docker-compose.dev.yml` é um override: troca para o `Dockerfile.dev`, que explode o WAR nos
deployments, e monta por bind `faces-views`, `templates`, `components` e `resources` do host.
Com isso XHTML e estáticos são refletidos sem rebuild (basta recarregar a página —
`FACELETS_REFRESH_PERIOD` está em 0). Mudança em código Java continua exigindo rebuild.
Também expõe o Postgres em `localhost:5432`.

## Subir com Caddy (HTTPS)

Para servidor sem proxy reverso próprio (ex.: VM na OCI). Requer `DOMAIN` no `.env` com o DNS
apontando para o IP da máquina e as portas 80/443 liberadas.

```bash
docker compose -f docker-compose.yml -f docker-compose.caddy.yml up -d --build
```

O Caddy emite e renova o certificado (Let's Encrypt) sozinho; os certificados ficam no volume
`caddy_data`.

## Deploy automático

Push na branch `release` dispara `.github/workflows/deploy.yml`, que entra na VM por SSH,
atualiza o código para o commit do push, sobe com o Caddy e verifica se a aplicação responde.

Configuração no GitHub (*Settings → Environments → prototipo-oci*):

| Tipo | Nome | Valor |
|---|---|---|
| Secret | `VM_HOST` | IP público da VM |
| Secret | `VM_USER` | `ubuntu` |
| Secret | `VM_SSH_KEY` | chave privada com acesso à VM |
| Secret | `VM_FINGERPRINT` | `ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub` na VM (só o `SHA256:...`) |
| Variable | `DEPLOY_PATH` | opcional, padrão `/home/ubuntu/pid-hc` |

Na VM, o repositório precisa estar clonado em `DEPLOY_PATH` e o `.env` criado.

## Acesso

- Local: http://localhost:8080/pid-hc/
- Com Caddy: https://$DOMAIN/pid-hc/

O schema é criado/atualizado pelo Hibernate (`hbm2ddl.auto=update`).

## Derrubar

```bash
docker compose down          # normal
docker compose down -v       # apaga também o volume do banco
```
