INSERT INTO tipo_chamado (codigo, nome, descricao, ativo, ordem) VALUES
('VAZAMENTO_ESGOTO', 'Vazamento de esgoto', 'Vazamento visível de esgoto em imóvel ou via', TRUE, 1),
('ESGOTO_RETORNANDO', 'Esgoto retornando para imóvel', 'Retorno de esgoto para o imóvel', TRUE, 2),
('EXTRAVASAMENTO_VIA', 'Extravasamento em via pública', 'Extravasamento de esgoto em via pública', TRUE, 3),
('BUEIRO_POCO', 'Bueiro ou poço de visita com problema', 'Problemas em bueiro ou poço de visita', TRUE, 4),
('MAU_CHEIRO', 'Mau cheiro relacionado à rede de esgoto', 'Odor associado à rede de esgoto', TRUE, 5),
('OUTROS', 'Outros', 'Outras ocorrências relacionadas à rede de esgoto', TRUE, 6);
