# MotoLog

API REST para controle de manutencao de motocicletas. O piloto cadastra suas motos,
registra servicos, acompanha quilometragem/horas de motor e custos e consulta o
historico de manutencao.

Este projeto e um exercicio pratico para comparar padroes do Quarkus com os do Spring
Boot, incluindo JWT, BCrypt, JPA com Panache, validacao, autorizacao por dono do
recurso e Dev Services.

## Estado atual

- Quarkus `3.38.3` e Java `21`.
- Endpoint temporario `GET /hello`.
- `POST /auth/register` e `POST /auth/login` implementados (ver "Autenticacao"
  abaixo).
- Mailer, reset de senha, motos, manutencoes e OpenAPI ainda precisam ser
  adicionados.

O escopo abaixo e o contrato do MVP a ser implementado. Nao considere um endpoint
disponivel ate que ele tenha codigo e teste correspondentes.

### Autenticacao (implementado)

O login usa `quarkus-security-jpa`: a entidade `User` e anotada com
`@UserDefinition`/`@Username`/`@Password`/`@Roles`, e o Quarkus gera um
`JpaIdentityProvider` que sabe buscar o usuario pelo e-mail e comparar a
senha com o hash BCrypt. `POST /auth/login` invoca esse provider de forma
programatica (via `IdentityProviderManager`, sem usar Basic Auth) e, se as
credenciais forem validas, emite um JWT com `io.smallrye.jwt.build.Jwt`. As
rotas protegidas (a implementar) vao validar esse JWT via `quarkus-smallrye-jwt`,
sem exigir Basic Auth em cada requisicao.

## Escopo do MVP

### Autenticacao

- Cadastro de usuario com nome, e-mail unico e senha com no minimo 8 caracteres.
- Senhas armazenadas somente como hash BCrypt.
- Login com emissao de JWT contendo `sub`, `groups` e expiracao.
- Solicitar redefinicao de senha sem revelar se o e-mail existe.
- Redefinir senha com token UUID de uso unico, valido por 30 minutos.

### Dominio

- CRUD de motocicletas pertencentes ao usuario autenticado.
- Registro e historico de manutencoes por motocicleta.
- Tipos de manutencao previamente cadastrados via `import.sql`.
- Consulta opcional de proximas manutencoes calculadas a partir dos intervalos.

## Endpoints planejados

| Metodo | Rota | Auth | Objetivo |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Nao | Cadastrar usuario |
| `POST` | `/auth/login` | Nao | Emitir JWT |
| `POST` | `/auth/forgot-password` | Nao | Gerar token de reset |
| `POST` | `/auth/reset-password` | Nao | Redefinir senha |
| `GET` | `/motorcycles` | JWT | Listar motos do usuario |
| `POST` | `/motorcycles` | JWT | Cadastrar moto |
| `PUT` | `/motorcycles/{id}` | JWT | Atualizar moto |
| `DELETE` | `/motorcycles/{id}` | JWT | Excluir moto |
| `GET` | `/motorcycles/{id}/maintenances` | JWT | Consultar historico |
| `POST` | `/motorcycles/{id}/maintenances` | JWT | Registrar manutencao |
| `GET` | `/motorcycles/{id}/maintenances/upcoming` | JWT | Consultar proximas manutencoes |
| `GET` | `/maintenance-types` | JWT | Listar tipos de servico |

## Modelo de dados

- `User` 1:N `Motorcycle`.
- `User` 1:N `PasswordResetToken`.
- `Motorcycle` 1:N `MaintenanceRecord`.
- `MaintenanceType` 1:N `MaintenanceRecord`.

Entidades e campos principais:

- `User`: nome, e-mail, hash da senha, role e data de criacao.
- `PasswordResetToken`: token, validade, uso e usuario.
- `Motorcycle`: marca, modelo, ano, placa, km atual, horas de motor e usuario dono.
- `MaintenanceType`: nome, intervalo em km e intervalo em horas de motor.
- `MaintenanceRecord`: moto, tipo, data, km, horas, custo, observacoes e data de criacao.

## Regras de negocio

1. O e-mail e unico.
2. A senha deve ter pelo menos 8 caracteres e nunca pode ser persistida em texto puro.
3. O token de reset expira em 30 minutos e so pode ser usado uma vez.
4. `/auth/forgot-password` sempre retorna `200`, inclusive para e-mail inexistente.
5. O usuario so pode listar, alterar e excluir suas proprias motos e manutencoes.
6. `service_date` nao pode ser uma data futura; dados invalidos devem retornar `422`.
7. Rotas fora de `/auth` exigem JWT valido.

## Stack planejada

- Quarkus REST + Jackson
- Hibernate ORM with Panache
- PostgreSQL via Dev Services em desenvolvimento
- Hibernate Validator
- SmallRye JWT e JWT Build
- Elytron Security Common para BCrypt
- Quarkus Mailer para o fluxo de reset
- SmallRye OpenAPI/Swagger UI como bonus

Em desenvolvimento, nao configurar uma URL manual de banco: com o driver PostgreSQL
presente, o Quarkus Dev Services deve iniciar o PostgreSQL automaticamente. A URL
real fica reservada para o profile `%prod`.

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

Enquanto o projeto ainda estiver no scaffold, o endpoint de verificacao e:

```shell
curl http://localhost:8080/hello
```

O Dev UI fica disponivel em <http://localhost:8080/q/dev/>.

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

## Plano de implementacao

1. Adicionar as extensoes do MVP e configurar os profiles Quarkus.
2. Criar entidades Panache, relacionamentos, validacoes e seed de tipos.
3. Implementar cadastro, login e protecao JWT.
4. Implementar reset de senha e envio do link pelo mailer.
5. Implementar CRUD de motos com autorizacao por dono.
6. Implementar manutencoes, historico e validacao de data.
7. Implementar proximas manutencoes como bonus.
8. Cobrir os fluxos principais com `@QuarkusTest` e RestAssured.

## Definition of Done

- E possivel registrar, autenticar e receber um JWT valido.
- O fluxo de redefinicao de senha funciona para tokens validos e rejeita tokens invalidos.
- Endpoints protegidos recusam requisicoes sem token.
- CRUD de motos e manutencoes funciona.
- Um usuario nao acessa dados de outro usuario.
- Manutencao com data futura e rejeitada.
- Testes automatizados cobrem os fluxos principais.

## Referencias

- [Quarkus](https://quarkus.io/)
- [Quarkus REST](https://quarkus.io/guides/rest)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Security with JWT](https://quarkus.io/guides/security-jwt)
