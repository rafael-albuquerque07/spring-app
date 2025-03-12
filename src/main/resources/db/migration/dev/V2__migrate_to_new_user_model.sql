-- Script de migração para a nova estrutura de usuários (Compatível com H2)

-- 1. Backup das tabelas originais (H2 não suporta CREATE TABLE AS SELECT diretamente)
CREATE TABLE users_backup AS (SELECT * FROM users);
CREATE TABLE clients_backup AS (SELECT * FROM clients);
CREATE TABLE affiliates_backup AS (SELECT * FROM affiliates);
CREATE TABLE user_roles_backup AS (SELECT * FROM user_roles);

-- 2. Adicionar colunas de autenticação em clients
ALTER TABLE clients ADD COLUMN username VARCHAR(255);
ALTER TABLE clients ADD COLUMN password VARCHAR(255);
ALTER TABLE clients ADD COLUMN admin_id BIGINT DEFAULT 1;

-- 3. Adicionar colunas de autenticação em affiliates
ALTER TABLE affiliates ADD COLUMN username VARCHAR(255);
ALTER TABLE affiliates ADD COLUMN password VARCHAR(255);
ALTER TABLE affiliates ADD COLUMN admin_id BIGINT DEFAULT 1;

-- 4. Criar tabelas para roles de clientes e afiliados
CREATE TABLE IF NOT EXISTS client_roles (
                                            client_id BIGINT NOT NULL,
                                            role VARCHAR(255) NOT NULL,
                                            PRIMARY KEY (client_id, role),
                                            FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS affiliate_roles (
                                               affiliate_id BIGINT NOT NULL,
                                               role VARCHAR(255) NOT NULL,
                                               PRIMARY KEY (affiliate_id, role),
                                               FOREIGN KEY (affiliate_id) REFERENCES affiliates(id)
);

-- 5. Migrar dados de credenciais de users para clients
UPDATE clients
SET username = (SELECT username FROM users WHERE users.id = clients.user_id),
    password = (SELECT password FROM users WHERE users.id = clients.user_id),
    admin_id = 1
WHERE user_id IS NOT NULL;

-- 6. Migrar dados de credenciais de users para affiliates
UPDATE affiliates
SET username = (SELECT username FROM users WHERE users.id = affiliates.user_id),
    password = (SELECT password FROM users WHERE users.id = affiliates.user_id),
    admin_id = 1
WHERE user_id IS NOT NULL;

-- 7. Migrar dados de roles para client_roles
MERGE INTO client_roles AS cr
USING (SELECT c.id AS client_id, ur.role FROM clients c
                                                  JOIN users u ON u.id = c.user_id
                                                  JOIN user_roles ur ON ur.user_id = u.id
       WHERE c.user_id IS NOT NULL) AS src
ON cr.client_id = src.client_id AND cr.role = src.role
WHEN NOT MATCHED THEN
    INSERT (client_id, role) VALUES (src.client_id, src.role);

-- 8. Migrar dados de roles para affiliate_roles
MERGE INTO affiliate_roles AS ar
USING (SELECT a.id AS affiliate_id, ur.role FROM affiliates a
                                                     JOIN users u ON u.id = a.user_id
                                                     JOIN user_roles ur ON ur.user_id = u.id
       WHERE a.user_id IS NOT NULL) AS src
ON ar.affiliate_id = src.affiliate_id AND ar.role = src.role
WHEN NOT MATCHED THEN
    INSERT (affiliate_id, role) VALUES (src.affiliate_id, src.role);

-- 9. Adicionar restrições únicas para username
ALTER TABLE clients ADD CONSTRAINT uk_clients_username UNIQUE (username);
ALTER TABLE affiliates ADD CONSTRAINT uk_affiliates_username UNIQUE (username);

-- 10. Configurar user_id como NULL para remover as referências
UPDATE clients SET user_id = NULL WHERE user_id IS NOT NULL;
UPDATE affiliates SET user_id = NULL WHERE user_id IS NOT NULL;

-- 11. Criar usuário admin se não existir
MERGE INTO users AS u
USING (SELECT 1 AS id, 'admin' AS username,
              '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' AS password,
              'admin@ecofraldas.com' AS email, TRUE AS enabled) AS src
ON u.id = src.id
WHEN NOT MATCHED THEN
    INSERT (id, username, password, email, enabled)
    VALUES (src.id, src.username, src.password, src.email, src.enabled);

-- 12. Garantir que o admin tem as roles corretas
MERGE INTO user_roles AS ur
USING (SELECT 1 AS user_id, 'ADMIN' AS role) AS src
ON ur.user_id = src.user_id AND ur.role = src.role
WHEN NOT MATCHED THEN
    INSERT (user_id, role) VALUES (src.user_id, src.role);

MERGE INTO user_roles AS ur
USING (SELECT 1 AS user_id, 'SUPERUSER' AS role) AS src
ON ur.user_id = src.user_id AND ur.role = src.role
WHEN NOT MATCHED THEN
    INSERT (user_id, role) VALUES (src.user_id, src.role);

-- 13. Remover todos os usuários exceto o admin
DELETE FROM user_roles WHERE user_id != 1;
DELETE FROM users WHERE id != 1;

-- 14. Adicionar chaves estrangeiras para admin_id
ALTER TABLE clients ADD CONSTRAINT fk_client_admin FOREIGN KEY (admin_id) REFERENCES users(id);
ALTER TABLE affiliates ADD CONSTRAINT fk_affiliate_admin FOREIGN KEY (admin_id) REFERENCES users(id);
