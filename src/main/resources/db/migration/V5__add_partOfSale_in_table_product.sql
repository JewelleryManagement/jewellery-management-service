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