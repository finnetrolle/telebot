BEGIN TRANSACTION;
ALTER TABLE pilots ADD COLUMN speaker BOOLEAN DEFAULT FALSE;
END TRANSACTION;