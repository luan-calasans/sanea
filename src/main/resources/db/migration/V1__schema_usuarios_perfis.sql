CREATE TABLE perfil (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(40)  NOT NULL,
    nome            VARCHAR(120) NOT NULL,
    descricao       VARCHAR(255),
    sistema         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_perfil_codigo ON perfil (codigo);

CREATE TABLE authority (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(80)  NOT NULL,
    descricao       VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX uk_authority_codigo ON authority (codigo);

CREATE TABLE perfil_authority (
    perfil_id       BIGINT NOT NULL REFERENCES perfil (id),
    authority_id    BIGINT NOT NULL REFERENCES authority (id),
    PRIMARY KEY (perfil_id, authority_id)
);

CREATE TABLE usuario (
    id                      BIGSERIAL PRIMARY KEY,
    nome                    VARCHAR(150) NOT NULL,
    email                   VARCHAR(180) NOT NULL,
    senha_hash              VARCHAR(255) NOT NULL,
    ativo                   BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verificado        BOOLEAN      NOT NULL DEFAULT FALSE,
    deve_trocar_senha       BOOLEAN      NOT NULL DEFAULT FALSE,
    perfil_id               BIGINT       NOT NULL REFERENCES perfil (id),
    criado_em               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    atualizado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ultimo_acesso_em        TIMESTAMPTZ,
    desativado_em           TIMESTAMPTZ,
    versao                  BIGINT       NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_usuario_email ON usuario (LOWER(email));
CREATE INDEX idx_usuario_perfil ON usuario (perfil_id);
CREATE INDEX idx_usuario_ativo ON usuario (ativo);

CREATE TABLE email_verificacao_token (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT       NOT NULL REFERENCES usuario (id),
    token_hash          VARCHAR(128) NOT NULL,
    proposito           VARCHAR(40)  NOT NULL,
    novo_email          VARCHAR(180),
    expira_em           TIMESTAMPTZ  NOT NULL,
    usado_em            TIMESTAMPTZ,
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_email_token_hash ON email_verificacao_token (token_hash);
CREATE INDEX idx_email_token_usuario ON email_verificacao_token (usuario_id);

CREATE TABLE login_tentativa (
    id                  BIGSERIAL PRIMARY KEY,
    email_normalizado   VARCHAR(180) NOT NULL,
    ip_address          VARCHAR(64)  NOT NULL,
    sucesso             BOOLEAN      NOT NULL,
    bloqueado_ate       TIMESTAMPTZ,
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_tentativa_email_ip ON login_tentativa (email_normalizado, ip_address, criado_em DESC);
