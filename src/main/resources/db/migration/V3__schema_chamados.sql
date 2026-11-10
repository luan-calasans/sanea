CREATE TABLE tipo_chamado (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(60)  NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    descricao       VARCHAR(500),
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    ordem           INTEGER      NOT NULL DEFAULT 0,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_tipo_chamado_codigo ON tipo_chamado (codigo);
CREATE INDEX idx_tipo_chamado_ativo ON tipo_chamado (ativo);

CREATE TABLE chamado (
    id                      BIGSERIAL PRIMARY KEY,
    protocolo               VARCHAR(20)  NOT NULL,
    usuario_id              BIGINT       NOT NULL REFERENCES usuario (id),
    tipo_chamado_id         BIGINT       NOT NULL REFERENCES tipo_chamado (id),
    status                  VARCHAR(30)  NOT NULL,
    descricao               VARCHAR(4000) NOT NULL,
    data_ocorrencia         DATE,
    aberto_em               TIMESTAMPTZ  NOT NULL,
    atualizado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    cep                     VARCHAR(8)   NOT NULL,
    logradouro              VARCHAR(200) NOT NULL,
    numero                  VARCHAR(30),
    complemento             VARCHAR(120),
    bairro                  VARCHAR(120) NOT NULL,
    cidade                  VARCHAR(120) NOT NULL,
    uf                      CHAR(2)      NOT NULL,
    endereco_verificado     BOOLEAN      NOT NULL DEFAULT FALSE,
    versao                  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_chamado_status CHECK (status IN (
        'ABERTO', 'EM_ANALISE', 'EM_ATENDIMENTO', 'RESOLVIDO', 'DUPLICADO', 'CANCELADO'
    )),
    CONSTRAINT ck_chamado_uf CHECK (uf ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_chamado_cep CHECK (cep ~ '^[0-9]{8}$')
);

CREATE UNIQUE INDEX uk_chamado_protocolo ON chamado (protocolo);
CREATE INDEX idx_chamado_status ON chamado (status);
CREATE INDEX idx_chamado_aberto_em ON chamado (aberto_em DESC);
CREATE INDEX idx_chamado_tipo ON chamado (tipo_chamado_id);
CREATE INDEX idx_chamado_bairro ON chamado (bairro);
CREATE INDEX idx_chamado_cidade ON chamado (cidade);
CREATE INDEX idx_chamado_usuario ON chamado (usuario_id);
CREATE INDEX idx_chamado_uf ON chamado (uf);

CREATE TABLE status_historico (
    id                  BIGSERIAL PRIMARY KEY,
    chamado_id          BIGINT       NOT NULL REFERENCES chamado (id),
    status_anterior     VARCHAR(30),
    status_novo         VARCHAR(30)  NOT NULL,
    autor_id            BIGINT       REFERENCES usuario (id),
    observacao          VARCHAR(1000),
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_status_historico_novo CHECK (status_novo IN (
        'ABERTO', 'EM_ANALISE', 'EM_ATENDIMENTO', 'RESOLVIDO', 'DUPLICADO', 'CANCELADO'
    ))
);

CREATE INDEX idx_status_historico_chamado ON status_historico (chamado_id, criado_em);
