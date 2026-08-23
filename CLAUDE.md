# Contexto do MotoLog

Este arquivo preserva o contexto do projeto para futuras sessoes de desenvolvimento.
Leia-o antes de alterar codigo ou tomar decisoes de arquitetura.

## Objetivo

Construir um MVP de API REST para um piloto registrar motocicletas e manutencoes,
acompanhando data, quilometragem, horas de motor, custo e proximos servicos previstos.
O projeto tambem serve para comparar Quarkus com Spring Boot em um dominio pequeno e
completo.

## Estado confirmado do repositorio

- Projeto Maven Quarkus em `c:\Users\jayme\Program\quartus\moto-log`.
- Quarkus `3.38.3`, Java `21`, Maven Wrapper presente.
- Dependencias atuais: `quarkus-rest`, `quarkus-arc`, JUnit do Quarkus e RestAssured.
- Existe somente o endpoint temporario `GET /hello` em `GreetingResource`.
- Existe um teste para `/hello` em `GreetingResourceTest`.
- `application.properties` esta vazio.
- As pastas `controllers`, `dtos` e `services` ja existem, mas o dominio do MVP ainda nao foi implementado.
- O `README.md` documenta o escopo funcional e os comandos de execucao.

## Contrato funcional

### Rotas publicas

- `POST /auth/register`: cadastro, `201`; e-mail duplicado, `409`; validacao, `422`.
- `POST /auth/login`: credenciais validas retornam JWT; credenciais invalidas, `401`.
- `POST /auth/forgot-password`: sempre retorna `200`, sem enumerar e-mails existentes.
- `POST /auth/reset-password`: token valido redefine a senha; token invalido, expirado ou usado retorna `400`.

### Rotas protegidas

- `GET|POST /motorcycles`
- `PUT|DELETE /motorcycles/{id}`
- `GET|POST /motorcycles/{id}/maintenances`
- `GET /motorcycles/{id}/maintenances/upcoming`
- `GET /maintenance-types`

Todas as rotas protegidas exigem JWT. O `sub` do JWT identifica o usuario dono; nunca
aceitar um `userId` arbitrario vindo do corpo para definir o dono do recurso.

## Modelo de dominio

Entidades: `User`, `PasswordResetToken`, `Motorcycle`, `MaintenanceType` e
`MaintenanceRecord`. Os relacionamentos sao User 1:N Motorcycle, User 1:N
PasswordResetToken, Motorcycle 1:N MaintenanceRecord e MaintenanceType 1:N
MaintenanceRecord.

Regras importantes:

- E-mail unico.
- Senha minima de 8 caracteres, persistida somente como BCrypt.
- Reset com UUID, validade de 30 minutos e uso unico.
- `service_date` nao pode ser futura.
- Historico ordenado da manutencao mais recente para a mais antiga.
- Consultas e alteracoes devem filtrar pelo usuario autenticado.

## Diretrizes de implementacao

- Manter Java 21 e Quarkus 3.x, usando Jakarta REST, CDI e Panache.
- Preferir DTOs para entrada e saida; nao expor entidades diretamente quando isso vazar hash, relacionamentos ou detalhes internos.
- Usar `BcryptUtil` para senhas e SmallRye JWT para validar/emitir tokens.
- Usar Dev Services em `%dev` e deixar a conexao real de PostgreSQL no `%prod`.
- Popular `MaintenanceType` com `import.sql`.
- Cobrir cada regra critica com `@QuarkusTest` e RestAssured.
- Manter alteracoes pequenas, coesas e alinhadas ao escopo do MVP; ideias extras devem ser registradas como v2.
- Nao criar frontend, refresh token, blacklist de logout, upload ou filtros avancados nesta fase.

## Ordem recomendada de trabalho

1. Adicionar extensoes e configurar JWT/Dev Services.
2. Criar entidades, relacionamentos, constraints e seed.
3. Implementar register/login.
4. Implementar reset de senha.
5. Implementar motos e autorizacao por dono.
6. Implementar manutencoes e historico.
7. Implementar proximas manutencoes, se o MVP basico estiver estavel.
8. Adicionar testes de integracao e atualizar o README conforme o estado real.

## Validacao minima

No Windows, executar:

```powershell
.\mvnw.cmd test
```

Durante desenvolvimento:

```powershell
.\mvnw.cmd quarkus:dev
```

Antes de considerar uma etapa concluida, confirmar build, testes e comportamento de
autorizacao entre dois usuarios diferentes.