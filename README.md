# MotoLog

API REST para controle de manutencao de motocicletas. O piloto cadastra suas motos,
registra servicos, acompanha quilometragem/horas de motor e custos, consulta o
historico de manutencao e ve quais servicos estao proximos ou vencidos.

Este projeto e um exercicio pratico para comparar padroes do Quarkus com os do Spring
Boot, incluindo JWT, BCrypt, JPA com Panache, validacao, autorizacao por dono do
recurso e Dev Services.

## Estado atual

MVP do backend completo e testado:

- Quarkus `3.38.3` e Java `21`.
- Cadastro, login, "esqueci minha senha" e reset de senha.
- CRUD de motocicletas com autorizacao por dono.
- Registro e historico de manutencoes por motocicleta.
- Calculo de proximas manutencoes previstas (por km e/ou horas de motor).
- Catalogo fixo de tipos de manutencao (`GET /maintenance-types`, seed via `import.sql`).
- Integracao para bot (ex.: Telegram) registrar manutencao via API key, sem JWT.
- Testes de integracao (`@QuarkusTest` + RestAssured) cobrindo os fluxos principais,
  incluindo isolamento entre usuarios diferentes.

Frontend em desenvolvimento na pasta [`frontend/`](frontend/) (React + TypeScript +
Vite + Tailwind), projeto separado que consome esta API via HTTP.

## Autenticacao

O login usa `quarkus-security-jpa`: a entidade `User` e anotada com
`@UserDefinition`/`@Username`/`@Password`/`@Roles`, e o Quarkus gera um
`JpaIdentityProvider` que sabe buscar o usuario pelo e-mail e comparar a
senha com o hash BCrypt. `POST /auth/login` invoca esse provider de forma
programatica (via `IdentityProviderManager`, sem usar Basic Auth) e, se as
credenciais forem validas, emite um JWT com `io.smallrye.jwt.build.Jwt`. Rotas
protegidas usam `@Authenticated`/`@RolesAllowed` e exigem esse JWT no header
`Authorization: Bearer <token>`.

`POST /auth/forgot-password` sempre retorna `200`, exista ou nao o e-mail (sem
enumeracao). Se existir, gera um `PasswordResetToken` (UUID, valido por 30
minutos, uso unico) e envia o token por e-mail via `quarkus-mailer`. Em
`%dev`/`%test`, nenhum e-mail sai de verdade — `quarkus.mailer.mock=true` e o
padrao fora de `%prod`, entao a mensagem so aparece no log (e, em teste, via
`io.quarkus.mailer.MockMailbox`). `POST /auth/reset-password` valida esse
token (existe, nao expirou, nao foi usado) e troca a senha; qualquer falha
nessa validacao retorna `400` com mensagem generica.

## Integracao com bot (Telegram)

`POST /bot/maintenance` permite que um bot externo (ex.: um workflow do
Telegram/n8n) registre uma manutencao sem JWT, autenticando por API key fixa
no header `X-API-Key` (configurada em `motolog.bot.api-key`,
`application.properties`). O usuario e identificado pelo `chatId` do
Telegram, vinculado previamente na coluna `users.telegram_chat_id`.

Fluxo dentro de `MaintenanceService.registerFromBot`:

1. Busca o `User` pelo `telegram_chat_id` — se nao achar, `403`.
2. Busca a primeira moto desse usuario (MVP assume uma moto por usuario) — se
   nao tiver, `404`.
3. Busca o `MaintenanceType` pelo id informado — se nao existir, `404`.
4. Persiste o registro de manutencao.

Exemplo de chamada:

```bash
curl -X POST http://localhost:8080/bot/maintenance \
  -H "X-API-Key: um-segredo-qualquer" \
  -H "Content-Type: application/json" \
  -d '{
        "chatId": 909307766,
        "maintenanceTypeId": 1,
        "serviceDate": "2026-08-26",
        "odometerKm": 12500,
        "engineHours": 320.5,
        "cost": 450.00,
        "notes": "Troca de oleo e filtro"
      }'
```

## Endpoints

| Metodo | Rota | Auth | Objetivo |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Nao | Cadastrar usuario |
| `POST` | `/auth/login` | Nao | Emitir JWT |
| `POST` | `/auth/forgot-password` | Nao | Gerar token de reset |
| `POST` | `/auth/reset-password` | Nao | Redefinir senha |
| `GET` | `/motorcycles` | JWT | Listar motos do usuario |
| `POST` | `/motorcycles` | JWT | Cadastrar moto |
| `PUT` | `/motorcycles/{id}` | JWT | Substituir moto (todos os campos) |
| `PATCH` | `/motorcycles/{id}` | JWT | Atualizar moto parcialmente |
| `DELETE` | `/motorcycles/{id}` | JWT | Excluir moto |
| `GET` | `/motorcycles/{id}/maintenances` | JWT | Consultar historico |
| `POST` | `/motorcycles/{id}/maintenances` | JWT | Registrar manutencao |
| `GET` | `/motorcycles/{id}/maintenances/upcoming` | JWT | Consultar proximas manutencoes |
| `GET` | `/maintenance-types` | JWT | Listar tipos de servico (catalogo fixo) |
| `POST` | `/bot/maintenance` | `X-API-Key` | Registrar manutencao via bot (Telegram) |

Todas as rotas protegidas exigem `Authorization: Bearer <jwt>`. O `sub` do JWT
identifica o usuario dono; nunca aceitar um `userId`/`owner` vindo do corpo
para definir o dono do recurso — o backend sempre resolve isso a partir do
usuario autenticado.

## Modelo de dados

- `User` 1:N `Motorcycle`.
- `User` 1:N `PasswordResetToken`.
- `Motorcycle` 1:N `MaintenanceRecord`.
- `MaintenanceType` 1:N `MaintenanceRecord`.

Entidades e campos principais:

- `User`: nome, e-mail (unico), hash da senha (BCrypt), role, `telegram_chat_id`
  (opcional, unico — vincula o usuario a um chat do bot) e data de criacao.
- `PasswordResetToken`: token (UUID), validade (30 min), flag de uso e usuario.
- `Motorcycle`: marca, modelo, ano, placa (unica), km atual, horas de motor
  atuais e usuario dono.
- `MaintenanceType`: nome, intervalo em km e intervalo em horas de motor
  (catalogo fixo, seedado via `import.sql`, sem endpoint de escrita).
- `MaintenanceRecord`: moto, tipo, data do servico (nao pode ser futura), km,
  horas, custo, observacoes e data de criacao.

## Regras de negocio

1. O e-mail e unico.
2. A senha deve ter pelo menos 8 caracteres e nunca pode ser persistida em texto puro.
3. O token de reset expira em 30 minutos e so pode ser usado uma vez.
4. `/auth/forgot-password` sempre retorna `200`, inclusive para e-mail inexistente.
5. O usuario so pode listar, alterar e excluir suas proprias motos e manutencoes.
6. `service_date`/`serviceDate` nao pode ser uma data futura; dados invalidos
   retornam `422` (via `ValidationExceptionMapper`).
7. Rotas fora de `/auth` e `/bot` exigem JWT valido; `/bot/maintenance` exige
   `X-API-Key` valida em vez de JWT.
8. Historico de manutencao ordenado da mais recente para a mais antiga.

## Stack

- Quarkus REST + Jackson (`quarkus-rest`, `quarkus-rest-jackson`)
- Hibernate ORM with Panache + driver PostgreSQL
- Hibernate Validator (Bean Validation)
- Quarkus Security JPA + Elytron Security Common (BCrypt)
- SmallRye JWT + JWT Build (emissao/validacao de token)
- Quarkus Mailer (fluxo de reset de senha)
- PostgreSQL via Dev Services em desenvolvimento/teste

Em desenvolvimento, nao configurar uma URL manual de banco: com o driver PostgreSQL
presente, o Quarkus Dev Services inicia o PostgreSQL automaticamente. A porta fica
fixada em `55432` (`%dev.quarkus.datasource.devservices.port`), entao uma conexao
salva num cliente de banco (extensao do VS Code, DBeaver etc.) continua valida entre
restarts: host `localhost`, porta `55432`, banco/usuario/senha `quarkus`.

## Como executar

### Desenvolvimento

No Windows:

```powershell
.\mvnw.cmd quarkus:dev
```

No Linux/macOS:

```shell
./mvnw quarkus:dev
```

O Dev UI fica disponivel em <http://localhost:8080/q/dev-ui/>.

Usuario de teste ja seedado (`import.sql`), com uma moto cadastrada:

```
E-mail: demo@motolog.test
Senha:  demo12345
```

### Testes

```powershell
.\mvnw.cmd test
```

### Build JVM

```powershell
.\mvnw.cmd package
java -jar target\quarkus-app\quarkus-run.jar
```

### Build nativo

Com GraalVM/Mandrel instalado:

```powershell
.\mvnw.cmd package -Dnative
```

Ou usando build em container:

```powershell
.\mvnw.cmd package -Dnative -Dquarkus.native.container-build=true
```

## Frontend

Projeto separado em [`frontend/`](frontend/) (React + TypeScript + Vite +
Tailwind CSS), consumindo esta API via HTTP e guardando o JWT recebido no
login para as chamadas autenticadas. Veja o README dentro da pasta para os
comandos especificos (`npm install`, `npm run dev`).

## Definition of Done

- E possivel registrar, autenticar e receber um JWT valido.
- O fluxo de redefinicao de senha funciona para tokens validos e rejeita tokens invalidos.
- Endpoints protegidos recusam requisicoes sem token.
- CRUD de motos e manutencoes funciona, com autorizacao por dono.
- Um usuario nao acessa dados de outro usuario.
- Manutencao com data futura e rejeitada.
- Bot externo registra manutencao via API key, sem precisar de JWT.
- Testes automatizados cobrem os fluxos principais.

## Referencias

- [Quarkus](https://quarkus.io/)
- [Quarkus REST](https://quarkus.io/guides/rest)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Security with JWT](https://quarkus.io/guides/security-jwt)
