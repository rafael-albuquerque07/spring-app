-- Script de migração para a nova estrutura de usuários

-- 1. Backup das tabelas originais (opcional, mas recomendado)
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users;
CREATE TABLE IF NOT EXISTS clients_backup AS SELECT * FROM clients;
CREATE TABLE IF NOT EXISTS affiliates_backup AS SELECT * FROM affiliates;
CREATE TABLE IF NOT EXISTS user_roles_backup AS SELECT * FROM user_roles;

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
UPDATE clients c
SET username = (SELECT u.username FROM users u WHERE u.id = c.user_id),
    password = (SELECT u.password FROM users u WHERE u.id = c.user_id),
    admin_id = 1
WHERE c.user_id IS NOT NULL;

-- 6. Migrar dados de credenciais de users para affiliates
UPDATE affiliates a
SET username = (SELECT u.username FROM users u WHERE u.id = a.user_id),
    password = (SELECT u.password FROM users u WHERE u.id = a.user_id),
    admin_id = 1
WHERE a.user_id IS NOT NULL;

-- 7. Migrar dados de roles para client_roles
INSERT INTO client_roles (client_id, role)
SELECT c.id, ur.role
FROM clients c
         JOIN users u ON u.id = c.user_id
         JOIN user_roles ur ON ur.user_id = u.id
WHERE c.user_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 8. Migrar dados de roles para affiliate_roles
INSERT INTO affiliate_roles (affiliate_id, role)
SELECT a.id, ur.role
FROM affiliates a
         JOIN users u ON u.id = a.user_id
         JOIN user_roles ur ON ur.user_id = u.id
WHERE a.user_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 9. Adicionar restrições únicas para username
ALTER TABLE clients ADD CONSTRAINT uk_clients_username UNIQUE (username);
ALTER TABLE affiliates ADD CONSTRAINT uk_affiliates_username UNIQUE (username);

-- 10. Configurar user_id como NULL para remover as referências
UPDATE clients SET user_id = NULL WHERE user_id IS NOT NULL;
UPDATE affiliates SET user_id = NULL WHERE user_id IS NOT NULL;

-- 11. Verificar se o usuário admin existe e criar se não existir
INSERT INTO users (id, username, password, email, enabled)
SELECT 1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@ecofraldas.com', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1)
ON CONFLICT DO NOTHING;

-- 12. Garantir que o admin tem as roles corretas
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ADMIN'), (1, 'SUPERUSER')
ON CONFLICT DO NOTHING;

-- 13. Remover todos os users exceto o admin (ID 1)
DELETE FROM user_roles WHERE user_id != 1;
DELETE FROM users WHERE id != 1;

-- 14. Adicionar chaves estrangeiras para admin_id
ALTER TABLE clients ADD CONSTRAINT fk_client_admin FOREIGN KEY (admin_id) REFERENCES users(id);
ALTER TABLE affiliates ADD CONSTRAINT fk_affiliate_admin FOREIGN KEY (admin_id) REFERENCES users(id);