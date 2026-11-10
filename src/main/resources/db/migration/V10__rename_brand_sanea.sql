-- Atualiza textos de perfil que ainda citavam o nome antigo do sistema.
UPDATE perfil
SET descricao = 'Atendimento operacional no Sanea'
WHERE codigo = 'SABESP'
  AND descricao = 'Atendimento operacional dos chamados de esgoto';
