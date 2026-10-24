package com.minegocio.mi_negocio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MiNegocioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiNegocioApplication.class, args);
	}

}


/*
 * 
-- =====================================================================
-- MI NEGOCIO - MVP v2 (PostgreSQL 13+)
-- SUPER_ADMIN  -> users.platform_role (dueño del SaaS)
-- Dueño        -> stores.owner_user_id (puede tener 1, 2 o más sucursales)
-- STORE_ADMIN / EMPLOYEE -> store_users.role (rol dentro de cada sucursal)
-- Cada sucursal (store) tiene sus propios productos, ventas y pedidos.
-- =====================================================================

-- 1. USUARIOS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    platform_role VARCHAR(20) NOT NULL DEFAULT 'USER'
        CHECK (platform_role IN ('SUPER_ADMIN', 'USER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. SUCURSALES / NEGOCIOS (un dueño puede tener muchos)
CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    address TEXT,
    phone VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),  -- CLOSED = "eliminado"
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (owner_user_id, name)   -- el mismo dueño no repite nombre de sucursal
);
CREATE INDEX idx_stores_owner ON stores(owner_user_id);

-- 3. MIEMBROS DE CADA SUCURSAL (admin o empleado)
-- Al crear una sucursal, el backend inserta aquí al dueño como STORE_ADMIN.
-- Así, para saber a qué sucursales accede un usuario basta consultar esta tabla.
CREATE TABLE store_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(20) NOT NULL CHECK (role IN ('STORE_ADMIN', 'EMPLOYEE')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (store_id, user_id)
);
CREATE INDEX idx_store_users_user ON store_users(user_id);

-- 4. CATEGORÍAS (por sucursal)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id),
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id, store_id)
);
CREATE UNIQUE INDEX ux_categories_store_name ON categories(store_id, lower(name));

-- 5. PRODUCTOS (por sucursal; SIN control de stock, RF-21)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id),
    category_id UUID,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id, store_id),
    FOREIGN KEY (category_id, store_id) REFERENCES categories(id, store_id)
);
CREATE INDEX idx_products_store ON products(store_id);
CREATE INDEX idx_products_store_category ON products(store_id, category_id);
CREATE UNIQUE INDEX ux_products_store_name ON products(store_id, lower(name));

-- 6. PEDIDOS (por sucursal)
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id),
    created_by UUID NOT NULL REFERENCES users(id),
    customer_name VARCHAR(150),
    customer_phone VARCHAR(30),
    delivery_address TEXT,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED')),
    total NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id, store_id)
);
CREATE INDEX idx_orders_store_status ON orders(store_id, status, created_at DESC);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    store_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    FOREIGN KEY (order_id, store_id) REFERENCES orders(id, store_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id, store_id) REFERENCES products(id, store_id)
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- 7. VENTAS (por sucursal; opcionalmente nacen de un pedido)
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id),
    order_id UUID,                                  -- NULL = venta directa en mostrador
    sold_by UUID NOT NULL REFERENCES users(id),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id, store_id),
    FOREIGN KEY (order_id, store_id) REFERENCES orders(id, store_id)
);
CREATE INDEX idx_sales_store_date ON sales(store_id, created_at DESC);

CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL,
    store_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    FOREIGN KEY (sale_id, store_id) REFERENCES sales(id, store_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id, store_id) REFERENCES products(id, store_id)
);
CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);

-- =====================================================================
-- CONSULTAS ÚTILES
-- =====================================================================
-- Sucursales a las que accede un usuario (dueño, admin o empleado):
--   SELECT s.*, su.role
--   FROM store_users su
--   JOIN stores s ON s.id = su.store_id
--   WHERE su.user_id = :user AND su.is_active AND s.status = 'ACTIVE';
--
-- Ventas de hoy por sucursal de un dueño (reporte consolidado):
--   SELECT s.name, COUNT(*) AS ventas, SUM(sa.total) AS total
--   FROM sales sa
--   JOIN stores s ON s.id = sa.store_id
--   WHERE s.owner_user_id = :owner AND sa.created_at >= CURRENT_DATE
--   GROUP BY s.name;
--
-- =====================================================================
-- FLUJOS (en una transacción desde el backend)
-- =====================================================================
-- Crear sucursal (SUPER_ADMIN para cualquier dueño; el dueño para sí mismo):
--   INSERT INTO stores (owner_user_id, name) VALUES (:owner, ...);
--   INSERT INTO store_users (store_id, user_id, role) VALUES (:store, :owner, 'STORE_ADMIN');
--
-- Eliminar sucursal (SUPER_ADMIN):
--   UPDATE stores SET status = 'CLOSED' WHERE id = :store;
--
-- Crear empleado (el backend verifica que quien lo crea es STORE_ADMIN de ESA sucursal):
--   INSERT INTO users (full_name, email, password_hash) VALUES (...);
--   INSERT INTO store_users (store_id, user_id, role) VALUES (:store, :new_user, 'EMPLOYEE');
--   (si el empleado ya tiene cuenta, solo el segundo INSERT)
--
-- Pedido entregado -> venta:
--   UPDATE orders SET status = 'DELIVERED' WHERE id = :order AND store_id = :store;
--   INSERT INTO sales (store_id, order_id, sold_by, total) ...;
--   copiar order_items a sale_items (no hay stock que descontar: RF-21).













-- =====================================================================
-- Usuarios de prueba para Mi Negocio
-- Contrasena de los 3 usuarios: Admin123*   (hash BCrypt, compatible con Spring)
-- Ejecutar DESPUES de mi_negocio_mvp_v2.sql
-- =====================================================================

-- 1. Tres usuarios con distinto rol de plataforma
INSERT INTO users (full_name, email, password_hash, platform_role) VALUES
 ('Dueno del SaaS',  'superadmin@minegocio.com', '$2a$10$tX911HhBGgQ8bE0iBU9EOOg4bh0Z.RkLt3DqXYWU.2DDwa6suADFa', 'SUPER_ADMIN'),
 ('Carlos Admin',    'carlos@minegocio.com',     '$2a$10$9snIvuUQbTQJizpi4U43gOt4HlVANifUESyot7OftllD9D4zdx582', 'USER'),
 ('Maria Empleada',  'maria@minegocio.com',      '$2a$10$5KeaS43UPefyetGTZAUEZOr9qiI3FQL682oTZ7t22LPfBLSWNfgwK', 'USER');

-- 2. Una sucursal cuyo dueno es Carlos
INSERT INTO stores (owner_user_id, name, address, phone)
SELECT id, 'Tienda Central', 'Calle Principal 123', '70000000'
FROM users WHERE email = 'carlos@minegocio.com';

-- 3. Roles dentro de la sucursal
--    Carlos -> STORE_ADMIN  |  Maria -> EMPLOYEE
INSERT INTO store_users (store_id, user_id, role)
SELECT s.id, u.id, 'STORE_ADMIN'
FROM stores s, users u
WHERE s.name = 'Tienda Central' AND u.email = 'carlos@minegocio.com';

INSERT INTO store_users (store_id, user_id, role)
SELECT s.id, u.id, 'EMPLOYEE'
FROM stores s, users u
WHERE s.name = 'Tienda Central' AND u.email = 'maria@minegocio.com';

-- 4. Verificacion
SELECT u.email, u.platform_role, s.name AS sucursal, su.role AS rol_en_sucursal
FROM users u
LEFT JOIN store_users su ON su.user_id = u.id
LEFT JOIN stores s ON s.id = su.store_id
ORDER BY u.email;


ALTER TABLE stores   ADD COLUMN IF NOT EXISTS logo_url TEXT, ADD COLUMN IF NOT EXISTS logo_public_id VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url TEXT, ADD COLUMN IF NOT EXISTS image_public_id VARCHAR(255);

-- =====================================================================
-- MIGRACION v3 (RF-19 / RF-21): categorias con estado activo y sin stock
-- Ejecutar sobre bases de datos ya creadas con el script anterior.
-- =====================================================================
ALTER TABLE categories ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products   DROP COLUMN IF EXISTS stock_quantity;

-- =====================================================================
-- MIGRACION v4: precio > 0, unicidad case-insensitive de nombres,
-- indice de productos por categoria y created_at en categorias.
-- Ejecutar sobre bases de datos ya creadas con los scripts anteriores.
-- =====================================================================
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_price_check;
ALTER TABLE products ADD CONSTRAINT products_price_check CHECK (price > 0);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
CREATE INDEX IF NOT EXISTS idx_products_store_category ON products(store_id, category_id);
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_store_id_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_store_name ON categories(store_id, lower(name));
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_store_name   ON products(store_id, lower(name));
 * 
 * */
 