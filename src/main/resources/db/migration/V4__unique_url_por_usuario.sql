-- Quita la unicidad global de original_url
ALTER TABLE url
DROP CONSTRAINT uk_url_original_url;

-- Cada usuario puede acortar cualquier URL, pero sin repetirla él mismo
ALTER TABLE url
    ADD CONSTRAINT uk_url_usuario_original_url UNIQUE (usuario_id, original_url);