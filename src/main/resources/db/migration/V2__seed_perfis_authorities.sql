INSERT INTO perfil (codigo, nome, descricao, sistema) VALUES
('CIDADAO', 'Cidadão', 'Cadastro público para abertura e acompanhamento de chamados', TRUE),
('SABESP', 'Sabesp', 'Atendimento operacional dos chamados de esgoto', TRUE),
('PREFEITURA', 'Prefeitura', 'Monitoramento e consulta de indicadores', TRUE),
('ADMIN', 'Administrador', 'Gestão de usuários, permissões e catálogos', TRUE);

INSERT INTO authority (codigo, descricao) VALUES
('CHAMADO_CRIAR', 'Criar novos chamados'),
('CHAMADO_VISUALIZAR_PROPRIO', 'Visualizar próprios chamados'),
('CHAMADO_VISUALIZAR_TODOS', 'Visualizar todos os chamados'),
('CHAMADO_ALTERAR_STATUS', 'Alterar status de chamados'),
('MENSAGEM_PUBLICA', 'Enviar e visualizar mensagens públicas'),
('MENSAGEM_INTERNA', 'Enviar e visualizar mensagens internas'),
('DASHBOARD_VISUALIZAR', 'Visualizar dashboard'),
('USUARIO_GERENCIAR', 'Gerenciar usuários'),
('PERMISSAO_GERENCIAR', 'Gerenciar perfis e authorities'),
('TIPO_CHAMADO_GERENCIAR', 'Gerenciar tipos de chamado'),
('ANEXO_BAIXAR', 'Baixar anexos de chamados'),
('AUDITORIA_VISUALIZAR', 'Visualizar auditoria'),
('CONTA_PROPRIA_GERENCIAR', 'Gerenciar dados da própria conta');

-- CIDADAO
INSERT INTO perfil_authority (perfil_id, authority_id)
SELECT p.id, a.id
FROM perfil p
CROSS JOIN authority a
WHERE p.codigo = 'CIDADAO'
  AND a.codigo IN (
    'CHAMADO_CRIAR',
    'CHAMADO_VISUALIZAR_PROPRIO',
    'MENSAGEM_PUBLICA',
    'CONTA_PROPRIA_GERENCIAR'
  );

-- SABESP
INSERT INTO perfil_authority (perfil_id, authority_id)
SELECT p.id, a.id
FROM perfil p
CROSS JOIN authority a
WHERE p.codigo = 'SABESP'
  AND a.codigo IN (
    'CHAMADO_VISUALIZAR_TODOS',
    'CHAMADO_ALTERAR_STATUS',
    'MENSAGEM_PUBLICA',
    'MENSAGEM_INTERNA',
    'ANEXO_BAIXAR',
    'CONTA_PROPRIA_GERENCIAR'
  );

-- PREFEITURA
INSERT INTO perfil_authority (perfil_id, authority_id)
SELECT p.id, a.id
FROM perfil p
CROSS JOIN authority a
WHERE p.codigo = 'PREFEITURA'
  AND a.codigo IN (
    'CHAMADO_VISUALIZAR_TODOS',
    'MENSAGEM_PUBLICA',
    'MENSAGEM_INTERNA',
    'DASHBOARD_VISUALIZAR',
    'ANEXO_BAIXAR',
    'CONTA_PROPRIA_GERENCIAR'
  );

-- ADMIN
INSERT INTO perfil_authority (perfil_id, authority_id)
SELECT p.id, a.id
FROM perfil p
CROSS JOIN authority a
WHERE p.codigo = 'ADMIN'
  AND a.codigo IN (
    'CHAMADO_VISUALIZAR_TODOS',
    'CHAMADO_ALTERAR_STATUS',
    'MENSAGEM_PUBLICA',
    'MENSAGEM_INTERNA',
    'DASHBOARD_VISUALIZAR',
    'USUARIO_GERENCIAR',
    'PERMISSAO_GERENCIAR',
    'TIPO_CHAMADO_GERENCIAR',
    'ANEXO_BAIXAR',
    'AUDITORIA_VISUALIZAR',
    'CONTA_PROPRIA_GERENCIAR'
  );
