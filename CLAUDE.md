# Contexto do MotoLog

Este arquivo preserva o contexto do projeto para futuras sessoes de desenvolvimento.
Leia-o antes de alterar codigo ou tomar decisoes de arquitetura.

## Objetivo

Construir um MVP de API REST para um piloto registrar motocicletas e manutencoes,
acompanhando data, quilometragem, horas de motor, custo e proximos servicos previstos.
O projeto tambem serve para comparar Quarkus com Spring Boot em um dominio pequeno e
completo.

## Estado confirmado do repositorio

- Backend: projeto Maven Quarkus na raiz do repositorio (`c:\Users\Jayme\Program1\motorcycle-log-quarkus`).
- Quarkus `3.38.3`, Java `21`, Maven Wrapper presente.
- **API do MVP completa e testada**: register/login/forgot-password/reset-password,
  CRUD de motos com autorizacao por dono, registro/historico de manutencoes,
  proximas manutencoes previstas (UC10) e `GET /maintenance-types` (catalogo
  fixo, seedado via `import.sql`, sem endpoint de escrita).
- Autenticacao via `quarkus-security-jpa` (`@UserDefinition` em `User`) +
  JWT emitido manualmente no login (`io.smallrye.jwt.build.Jwt`) - nao usa
  Basic Auth. Rotas protegidas usam `@Authenticated`/`@RolesAllowed`.
- Testes com `@QuarkusTest` + RestAssured cobrindo os fluxos principais e
  isolamento entre usuarios diferentes.
- `README.md` documenta o escopo funcional e os comandos de execucao.
- **Frontend**: iniciado em `2026-08-24`. Ver secao "Frontend" abaixo.

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
- Manter alteracoes pequenas, coesas e alinhadas ao escopo de cada fase; ideias extras devem ser registradas como v2.
- Nao criar refresh token, blacklist de logout, upload ou filtros avancados nesta fase.

## Ordem recomendada de trabalho (backend - concluido)

1. ~~Adicionar extensoes e configurar JWT/Dev Services.~~
2. ~~Criar entidades, relacionamentos, constraints e seed.~~
3. ~~Implementar register/login.~~
4. ~~Implementar reset de senha.~~
5. ~~Implementar motos e autorizacao por dono.~~
6. ~~Implementar manutencoes e historico.~~
7. ~~Implementar proximas manutencoes.~~
8. ~~Adicionar testes de integracao e atualizar o README conforme o estado real.~~

## Frontend (fase atual)

Iniciada em `2026-08-24`. Objetivo: consumir a API existente com uma
interface simples - nao e o foco de comparacao Quarkus/Spring do projeto,
entao manter o escopo enxuto.

- Stack: React + TypeScript + Vite + Tailwind CSS.
- Projeto separado do backend (pasta propria, ex.: `frontend/`), consumindo
  a API via HTTP - nao serve os assets pelo Quarkus nesta fase.
- Guarda o JWT recebido no login e manda `Authorization: Bearer` nas
  chamadas as rotas protegidas.
- Escopo funcional e ordem de implementacao: a definir na sessao em que o
  frontend for planejado (ver historico de decisao no chat/plano salvo).

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