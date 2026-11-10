ALTER TABLE usuario
    ADD COLUMN tema_modo VARCHAR(10) NOT NULL DEFAULT 'LIGHT',
    ADD COLUMN cor_sidebar VARCHAR(7),
    ADD COLUMN cor_destaque VARCHAR(7);

ALTER TABLE usuario
    ADD CONSTRAINT ck_usuario_tema_modo CHECK (tema_modo IN ('LIGHT', 'DARK'));
