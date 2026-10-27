-- =====================================================================
-- MI NEGOCIO - MIGRACION PARA TABLA SALES Y SALE_ITEMS
-- =====================================================================

-- 1. Agregar columna status a tabla sales si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sales' AND column_name = 'status'
    ) THEN
        ALTER TABLE sales ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';
    END IF;
END $$;

-- 2. Asegurar el CHECK constraint para status en sales
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_sale_status'
    ) THEN
        ALTER TABLE sales ADD CONSTRAINT check_sale_status 
            CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'));
    END IF;
END $$;

-- 3. Índices recomendados para optimización de consultas
CREATE INDEX IF NOT EXISTS idx_sales_store_status ON sales(store_id, status);
CREATE INDEX IF NOT EXISTS idx_sales_order ON sales(order_id);
CREATE INDEX IF NOT EXISTS idx_sales_sold_by ON sales(sold_by);
CREATE INDEX IF NOT EXISTS idx_sale_items_store ON sale_items(store_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items(product_id);
