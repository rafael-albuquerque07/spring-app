-- Criação da tabela de usuários
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE
    );

-- Tabela de papéis de usuário
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Tabela de clientes
CREATE TABLE IF NOT EXISTS clients (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Tabela de afiliados
CREATE TABLE IF NOT EXISTS affiliates (
                                          id BIGSERIAL PRIMARY KEY,
                                          name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Tabela de produtos
CREATE TABLE IF NOT EXISTS products (
                                        product_code BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    product_choice VARCHAR(50) NOT NULL,
    affiliate_id BIGINT,
    image_url VARCHAR(255),
    FOREIGN KEY (affiliate_id) REFERENCES affiliates(id)
    );

-- Tabela de estoque
CREATE TABLE IF NOT EXISTS stocks (
                                      id BIGSERIAL PRIMARY KEY,
                                      product_id BIGINT NOT NULL,
                                      quantity INTEGER NOT NULL,
                                      FOREIGN KEY (product_id) REFERENCES products(product_code)
    );

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS orders (
                                      order_id BIGSERIAL PRIMARY KEY,
                                      client_id BIGINT NOT NULL,
                                      order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
    );

-- Tabela de carrinho
CREATE TABLE IF NOT EXISTS carts (
                                     id BIGSERIAL PRIMARY KEY,
                                     order_id BIGINT NOT NULL,
                                     product_id BIGINT NOT NULL,
                                     quantity INTEGER NOT NULL,
                                     payment_type VARCHAR(50) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_code)
    );

-- Tabela de checkout
CREATE TABLE IF NOT EXISTS checkouts (
                                         id BIGSERIAL PRIMARY KEY,
                                         order_id BIGINT NOT NULL,
                                         product_id BIGINT,
                                         quantity INTEGER NOT NULL,
                                         total_price DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    checkout_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipping_address VARCHAR(255) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_code)
    );