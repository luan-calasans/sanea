CREATE TABLE mensagem_chamado (
    id                  BIGSERIAL PRIMARY KEY,
    chamado_id          BIGINT        NOT NULL REFERENCES chamado (id),
    autor_id            BIGINT        NOT NULL REFERENCES usuario (id),
    corpo               VARCHAR(4000) NOT NULL,
    visibilidade        VARCHAR(20)   NOT NULL,
    criado_em           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mensagem_visibilidade CHECK (visibilidade IN ('PUBLICA', 'INTERNA'))
);

CREATE INDEX idx_mensagem_chamado ON mensagem_chamado (chamado_id, criado_em);
CREATE INDEX idx_mensagem_visibilidade ON mensagem_chamado (chamado_id, visibilidade);

CREATE TABLE anexo (
    id                      BIGSERIAL PRIMARY KEY,
    chamado_id              BIGINT       NOT NULL REFERENCES chamado (id),
    nome_original           VARCHAR(255) NOT NULL,
    nome_armazenado         VARCHAR(120) NOT NULL,
    caminho_relativo        VARCHAR(255) NOT NULL,
    content_type            VARCHAR(80)  NOT NULL,
    tamanho_bytes           BIGINT       NOT NULL,
    largura                 INTEGER,
    altura                  INTEGER,
    enviado_por_id          BIGINT       NOT NULL REFERENCES usuario (id),
    criado_em               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_anexo_content_type CHECK (content_type IN ('image/jpeg', 'image/png'))
);

CREATE UNIQUE INDEX uk_anexo_caminho ON anexo (caminho_relativo);
CREATE INDEX idx_anexo_chamado ON anexo (chamado_id);

CREATE TABLE auditoria_evento (
    id                  BIGSERIAL PRIMARY KEY,
    tipo_evento         VARCHAR(80)  NOT NULL,
    usuario_id          BIGINT       REFERENCES usuario (id),
    recurso_tipo        VARCHAR(80),
    recurso_id          VARCHAR(80),
    resultado           VARCHAR(40)  NOT NULL,
    ip_address          VARCHAR(64),
    detalhes            VARCHAR(1000),
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_tipo ON auditoria_evento (tipo_evento, criado_em DESC);
CREATE INDEX idx_auditoria_usuario ON auditoria_evento (usuario_id, criado_em DESC);
CREATE INDEX idx_auditoria_recurso ON auditoria_evento (recurso_tipo, recurso_id);
