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

UPDATE product
SET discount = 0.0
WHERE discount IS NULL;

DO $$
BEGIN
  IF EXISTS (SELECT column_name FROM information_schema.columns WHERE table_name = 'product' AND column_name = 'is_sold') THEN
    ALTER TABLE product DROP COLUMN is_sold;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT column_name
    FROM information_schema.columns
    WHERE table_name = 'product'
    AND column_name = 'part_of_sale_id'
  ) THEN
    ALTER TABLE product
    ADD COLUMN part_of_sale_id UUID;

    ALTER TABLE product
    ADD CONSTRAINT fk_product_sale
    FOREIGN KEY (part_of_sale_id)
    REFERENCES sale(id);
  END IF;
END $$;