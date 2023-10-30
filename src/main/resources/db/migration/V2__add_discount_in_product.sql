DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'product'
    AND column_name = 'discount'
  ) THEN
    ALTER TABLE product
    ADD COLUMN discount FLOAT DEFAULT 0.0;
  END IF;
END $$;