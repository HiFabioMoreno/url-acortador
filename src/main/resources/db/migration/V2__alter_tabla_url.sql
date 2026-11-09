-- Fechas: de VARCHAR a DATE (compatible con LocalDate)
ALTER TABLE url
ALTER COLUMN fecha_registro     TYPE DATE USING fecha_registro::DATE,
ALTER COLUMN fecha_modificacion TYPE DATE USING fecha_modificacion::DATE;

-- Clicks: de INTEGER a BIGINT
ALTER TABLE url
ALTER COLUMN clicks TYPE BIGINT;