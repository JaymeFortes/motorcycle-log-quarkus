-- Seed de MaintenanceType. So roda porque quarkus.hibernate-orm.schema-management.strategy=drop-and-create
-- esta em application.properties: o Hibernate reconhece import.sql automaticamente
-- e executa logo apos recriar o schema, toda vez que a app sobe.
-- Intervalos sao estimativas razoaveis (nao vieram especificados) - ajuste se quiser
-- valores diferentes. Fluido de freio fica sem intervalo (troca e por tempo, nao por
-- km/hora, e a tabela nao tem esse tipo de campo).
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Troca de oleo', 3000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Filtro de ar', 10000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Filtro de oleo', 3000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Corrente e coroa', 15000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Pastilhas de freio', 20000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Pneu', 25000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Revisao de valvulas', 12000, NULL);
INSERT INTO maintenance_types (name, interval_km, interval_engine_hours) VALUES ('Fluido de freio', NULL, NULL);

-- Usuario padrao pra testar o frontend sem precisar registrar de novo a cada
-- restart (drop-and-create apaga tudo). password_hash e o bcrypt de "demo12345"
-- (>= 8 caracteres, mesmo minimo exigido no cadastro normal). Prefixo "$2a$"
-- de proposito, nao "$2b$": o parser MCF do WildFly Elytron (usado pelo
-- quarkus-security-jpa) so reconhece "$2a$" - "$2b$" da ELY08003 "Unknown
-- crypt string algorithm". Sao o mesmo algoritmo bcrypt (a diferenca de
-- versao so importa pra senhas > 72 bytes), entao o hash em si e valido,
-- so o prefixo precisa ser esse.
-- Login: demo@motolog.test / demo12345
INSERT INTO users (email, name, password_hash, role, created_at, telegram_chat_id)
VALUES ('demo@motolog.test', 'Usuario Demo', '$2a$10$V.1rw7Fd593ah5LB.1XF5uBfT0vnXny4rO5LKldtpWEqv8ABX4dR2', 'user', now(),909307766);

-- Moto padrao do usuario demo, pra ja ter algo pra testar (manutencoes, bot etc.)
-- sem precisar cadastrar na mao a cada restart (drop-and-create apaga tudo).
INSERT INTO motorcycles (brand, model, model_year, plate, current_km, current_engine_hours, created_at, owner_id)
VALUES ('Honda', 'CG 160', 2022, 'DEMO1A23', 15000, 320.5, now(), (SELECT id FROM users WHERE email = 'demo@motolog.test'));
