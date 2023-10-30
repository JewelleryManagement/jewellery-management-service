DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT *
        FROM information_schema.columns
        WHERE table_name = 'product'
        AND column_name = 'discount'
    ) THEN
        ALTER TABLE product ADD COLUMN discount float DEFAULT 0.0;
    END IF;
END $$;
