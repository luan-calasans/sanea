-- Amplia o catálogo de tipos. Não recria itens já cobertos por:
-- VAZAMENTO_ESGOTO, ESGOTO_RETORNANDO, EXTRAVASAMENTO_VIA, BUEIRO_POCO, MAU_CHEIRO.

INSERT INTO tipo_chamado (codigo, nome, descricao, ativo, ordem)
SELECT v.codigo, v.nome, v.descricao, TRUE, v.ordem
FROM (VALUES
    ('FALTA_AGUA', 'Falta de água', 'Ausência de fornecimento de água', 10),
    ('BAIXA_PRESSAO_AGUA', 'Baixa pressão de água', 'Pressão insuficiente no imóvel', 20),
    ('FALTA_AGUA_RECORRENTE', 'Falta de água recorrente', 'Interrupções frequentes no fornecimento', 30),
    ('INTERRUPCAO_SEM_AVISO', 'Interrupção sem aviso', 'Corte ou interrupção sem comunicação prévia', 40),
    ('DEMORA_RESTABELECIMENTO', 'Demora no restabelecimento', 'Demora para retorno do fornecimento', 50),
    ('AR_TUBULACAO', 'Ar na tubulação', 'Presença de ar na rede/tubulação', 60),
    ('VAZAMENTO_RUA', 'Vazamento na rua', 'Vazamento de água em via pública', 70),
    ('VAZAMENTO_CALCADA', 'Vazamento na calçada', 'Vazamento de água na calçada', 80),
    ('VAZAMENTO_CAVALETE', 'Vazamento no cavalete', 'Vazamento no cavalete do imóvel', 90),
    ('HIDROMETRO_DANIFICADO', 'Hidrômetro danificado', 'Hidrômetro quebrado ou danificado', 100),
    ('TAMPA_PROTECAO_DANIFICADA', 'Tampa de proteção danificada', 'Tampa de proteção do hidrômetro/cavalete danificada', 110),
    ('VAZAMENTO_APOS_MANUTENCAO', 'Vazamento após manutenção', 'Vazamento após serviço de manutenção', 120),
    ('BURACO_APOS_OBRA', 'Buraco após obra', 'Buraco ou irregularidade após obra na via', 130),
    ('ESGOTO_ENTUPIDO', 'Esgoto entupido', 'Obstrução na rede ou ramal de esgoto', 140),
    ('SOLICITACAO_LIGACAO_ESGOTO', 'Solicitação de ligação de esgoto', 'Pedido de nova ligação de esgoto', 150),
    ('CONTA_AGUA_ALTA', 'Conta de água muito alta', 'Consumo ou valor da conta considerado elevado', 160),
    ('COBRANCA_INDEVIDA', 'Cobrança indevida', 'Contestação de cobrança indevida', 170),
    ('SEGUNDA_VIA_CONTA', 'Segunda via de conta', 'Solicitação de segunda via da conta', 180),
    ('PAGAMENTO_NAO_IDENTIFICADO', 'Pagamento não identificado', 'Pagamento realizado e não localizado', 190),
    ('NEGOCIACAO_DIVIDA', 'Negociação de dívida', 'Pedido de negociação de débitos', 200),
    ('ALTERACAO_VENCIMENTO', 'Alteração do vencimento', 'Pedido de mudança da data de vencimento', 210),
    ('CONTESTACAO_CONSUMO', 'Contestação de consumo', 'Contestação da leitura ou do consumo faturado', 220),
    ('TARIFA_SOCIAL', 'Solicitação de Tarifa Social', 'Pedido de enquadramento em Tarifa Social', 230),
    ('NOVA_LIGACAO_AGUA', 'Nova ligação de água', 'Solicitação de nova ligação de água', 240),
    ('RELIGACAO_AGUA', 'Religação de água', 'Pedido de religação do fornecimento', 250),
    ('REATIVACAO_FORNECIMENTO', 'Reativação de fornecimento', 'Reativação do fornecimento de água', 260),
    ('DESLIGAMENTO_TEMPORARIO', 'Desligamento temporário', 'Pedido de desligamento temporário', 270),
    ('ALTERACAO_TITULARIDADE', 'Alteração de titularidade', 'Troca de titular da ligação', 280),
    ('ATUALIZACAO_CADASTRAL', 'Atualização cadastral', 'Atualização de dados cadastrais', 290),
    ('ALTERACAO_LOCAL_CAVALETE', 'Alteração de localização do cavalete', 'Mudança de local do cavalete', 300),
    ('SEPARACAO_LIGACAO', 'Separação de ligação', 'Pedido de separação de ligação', 310),
    ('AGUA_COLORACAO_ALTERADA', 'Água com coloração alterada', 'Água com cor diferente do habitual', 320),
    ('AGUA_CHEIRO_ESTRANHO', 'Água com cheiro estranho', 'Água com odor anormal', 330),
    ('AGUA_GOSTO_DIFERENTE', 'Água com gosto diferente', 'Água com sabor anormal', 340),
    ('PARTICULAS_AGUA', 'Presença de partículas na água', 'Partículas ou resíduos na água', 350),
    ('ANALISE_AGUA', 'Solicitação de análise da água', 'Pedido de análise da qualidade da água', 360),
    ('ATRASO_ATENDIMENTO', 'Atraso no atendimento', 'Demora no atendimento do chamado', 370),
    ('SERVICO_NAO_REALIZADO', 'Serviço não realizado', 'Serviço agendado ou solicitado e não executado', 380),
    ('REABERTURA_CHAMADO', 'Reabertura de chamado', 'Reabertura de ocorrência anterior', 390),
    ('CONSULTA_PROTOCOLO', 'Consulta de protocolo', 'Consulta sobre andamento de protocolo', 400),
    ('RECLAMACAO_ATENDIMENTO', 'Reclamação sobre atendimento', 'Reclamação relacionada ao atendimento', 410),
    ('SOLICITACAO_VISTORIA', 'Solicitação de vistoria', 'Pedido de vistoria técnica', 420),
    ('RECLAMACAO_OUVIDORIA', 'Reclamação na Ouvidoria', 'Registro de reclamação via Ouvidoria', 430)
) AS v(codigo, nome, descricao, ordem)
WHERE NOT EXISTS (
    SELECT 1 FROM tipo_chamado t WHERE t.codigo = v.codigo
);

-- Mantém "Outros" sempre por último
UPDATE tipo_chamado
SET ordem = 999,
    atualizado_em = NOW()
WHERE codigo = 'OUTROS';
