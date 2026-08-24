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
