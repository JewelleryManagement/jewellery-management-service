DO $$
BEGIN
  IF EXISTS (SELECT column_name FROM information_schema.columns WHERE table_name = 'product' AND column_name = 'is_sold') THEN
    ALTER TABLE product DROP COLUMN is_sold;
  END IF;
END $$;