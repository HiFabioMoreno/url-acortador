UPDATE usuario
SET email = external_id || '@test.com'
WHERE email IS NULL;

ALTER TABLE usuario DROP CONSTRAINT uk_usuario_external_id;
ALTER TABLE usuario DROP COLUMN external_id;

ALTER TABLE usuario ALTER COLUMN email SET NOT NULL;
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_email UNIQUE (email);

ALTER TABLE usuario ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE usuario ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER';
ALTER TABLE usuario ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
