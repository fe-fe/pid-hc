INSERT INTO material (id, ativo, codigo, nome) VALUES
  (gen_random_uuid(), true, '104210', 'LUVA DE PROCEDIMENTO NÃO ESTÉRIL TAMANHO M'),
  (gen_random_uuid(), true, '104388', 'SERINGA DESCARTÁVEL 10 ML SEM AGULHA'),
  (gen_random_uuid(), true, '105017', 'CATETER INTRAVENOSO PERIFÉRICO 20G'),
  (gen_random_uuid(), true, '105244', 'COMPRESSA DE GAZE ESTÉRIL 7,5 X 7,5 CM'),
  (gen_random_uuid(), true, '106031', 'EQUIPO MACROGOTAS PARA SOLUÇÃO PARENTERAL'),
  (gen_random_uuid(), true, '106712', 'SONDA VESICAL DE DEMORA 2 VIAS Nº 16'),
  (gen_random_uuid(), true, '107455', 'AGULHA HIPODÉRMICA 25 X 7 MM'),
  (gen_random_uuid(), false, '108120', 'ATADURA DE CREPOM 10 CM');

INSERT INTO fabricante (id, ativo, nome) VALUES
  (gen_random_uuid(), true, 'NOVAMED PRODUTOS HOSPITALARES'),
  (gen_random_uuid(), true, 'SANIVIDA INDÚSTRIA MÉDICA'),
  (gen_random_uuid(), true, 'CIRUMAX DO BRASIL'),
  (gen_random_uuid(), true, 'PROTEXA DESCARTÁVEIS'),
  (gen_random_uuid(), false, 'HOSPILAR EQUIPAMENTOS');

INSERT INTO marca (id, ativo, nome) VALUES
  (gen_random_uuid(), true, 'NOVATOUCH'),
  (gen_random_uuid(), true, 'SANIFLEX'),
  (gen_random_uuid(), true, 'CIRUFLOW'),
  (gen_random_uuid(), true, 'PROTEXA PLUS'),
  (gen_random_uuid(), true, 'VITALINE');

CREATE TEMP TABLE carga_avaliacao (codigo text, fabricante text, marca text, registro text, data date,
                                   resultado text, local text, descricao text, parecer text);
INSERT INTO carga_avaliacao VALUES
  ('104210', 'NOVAMED PRODUTOS HOSPITALARES', 'NOVATOUCH', '80123450011', '2026-03-12', 'APROVADO', 'Centro Cirúrgico',
   'Teste de calçamento e resistência em 50 pares.', 'Material atende ao descritivo.'),
  ('104210', 'PROTEXA DESCARTÁVEIS', 'PROTEXA PLUS', '80456780023', '2026-04-02', 'REPROVADO', 'UTI Adulto',
   'Rasgos frequentes durante o calçamento.', 'Reprovar a marca para o próximo pregão.'),
  ('104388', 'SANIVIDA INDÚSTRIA MÉDICA', 'SANIFLEX', '80234560018', '2026-02-20', 'APROVADO', 'Pronto Atendimento',
   'Graduação legível e êmbolo sem travamento.', 'Aprovada.'),
  ('105017', 'CIRUMAX DO BRASIL', 'CIRUFLOW', '80345670031', '2026-05-08', 'APROVADO', 'Pediatria',
   'Punções realizadas por três enfermeiros.', 'Bom desempenho na inserção.'),
  ('105017', 'NOVAMED PRODUTOS HOSPITALARES', 'NOVATOUCH', '80123450046', '2026-05-15', 'REPROVADO', 'Pediatria',
   'Dobra do cateter após a punção.', 'Não recomendado para uso pediátrico.'),
  ('105244', 'PROTEXA DESCARTÁVEIS', NULL, '80456780058', '2026-06-03', 'APROVADO', 'Ambulatório',
   'Embalagem íntegra e sem fiapos.', 'Aprovada.'),
  ('106031', 'SANIVIDA INDÚSTRIA MÉDICA', 'VITALINE', '80234560067', '2026-07-21', 'APROVADO', 'Clínica Médica',
   'Gotejamento estável por 24 horas.', 'Aprovado.'),
  ('106712', 'CIRUMAX DO BRASIL', 'CIRUFLOW', '80345670072', '2026-08-11', 'REPROVADO', 'Urologia',
   'Balão não manteve a insuflação.', 'Reprovado; solicitar troca de lote.'),
  ('107455', 'NOVAMED PRODUTOS HOSPITALARES', 'NOVATOUCH', '80123450085', '2026-09-04', 'APROVADO', 'Centro Cirúrgico',
   'Bisel afiado e canhão firme.', 'Aprovada.'),
  ('104388', 'PROTEXA DESCARTÁVEIS', 'PROTEXA PLUS', '80456780094', '2026-10-17', 'APROVADO', 'UTI Adulto',
   'Sem vazamento no teste de pressão.', 'Aprovada.');

INSERT INTO avaliacao (id, ativo, material_id, fabricante_id, marca_id, registroanvisa, data, resultado, local,
                       descricao, parecer)
SELECT gen_random_uuid(), true, m.id, f.id, ma.id, c.registro, c.data, c.resultado, c.local, c.descricao, c.parecer
FROM carga_avaliacao c
JOIN material m ON m.codigo = c.codigo
JOIN fabricante f ON f.nome = c.fabricante
LEFT JOIN marca ma ON ma.nome = c.marca;

INSERT INTO importacao_tecnovigilancia (id, ativo, nome_arquivo, sha256, total_registros)
VALUES (gen_random_uuid(), true, 'tecnovigilancia-2026.xlsx', repeat('a', 64), 6);

CREATE TEMP TABLE carga_desvio (codigo text, fabricante text, marca text, data date, vigihosp text, registro text,
                                lote text, validade date, amostra text, servico text, unidade text, motivo text,
                                providencias text, tecnico text, desfecho text, grau text, processo text);
INSERT INTO carga_desvio VALUES
  ('104210', 'PROTEXA DESCARTÁVEIS', 'PROTEXA PLUS', '2026-03-28', '2026-0412', '80456780023', 'L2601A', '2028-01-31',
   'SIM', 'Enfermagem', 'UTI Adulto', 'Luvas rasgando ao calçar.', 'Recolhido o lote da unidade.', 'Técnico A',
   'SUGESTAO_REPROVACAO_MARCA', 'LEVE', '23075.004512/2026-11'),
  ('105017', 'NOVAMED PRODUTOS HOSPITALARES', 'NOVATOUCH', '2026-05-02', '2026-0587', '80123450046', 'C2604B',
   '2029-03-31', 'FOTO', 'Enfermagem', 'Pediatria', 'Cateter dobrando após a punção.',
   'Encaminhado para avaliação técnica.', 'Técnico B', 'SUGESTAO_TROCA_LOTE', 'MODERADO', NULL),
  ('106712', 'CIRUMAX DO BRASIL', 'CIRUFLOW', '2026-07-30', '2026-0833', '80345670072', 'S2607C', '2028-07-31',
   'SIM', 'Enfermagem', 'Urologia', 'Balão da sonda esvaziando.', 'Lote segregado no almoxarifado.', 'Técnico A',
   'SUGESTAO_RECOLHIMENTO', 'MODERADO', '23075.009871/2026-02'),
  ('104388', 'SANIVIDA INDÚSTRIA MÉDICA', 'SANIFLEX', '2026-08-19', '2026-0901', '80234560018', 'R2608D',
   '2029-08-31', 'NAO', 'Farmácia', 'Pronto Atendimento', 'Êmbolo com resistência.', 'Orientada a equipe.',
   'Técnico C', 'QUEIXA_PONTUAL', 'AUSENTE', NULL),
  ('106031', 'SANIVIDA INDÚSTRIA MÉDICA', 'VITALINE', '2026-09-12', '2026-0958', '80234560067', 'E2609E',
   '2028-09-30', 'SIM', 'Enfermagem', 'Clínica Médica', 'Gotejamento irregular.', 'Substituído o equipo.',
   'Técnico B', 'INCONCLUSIVO', 'POTENCIAL', NULL),
  ('105244', 'PROTEXA DESCARTÁVEIS', NULL, '2026-10-05', '2026-1022', '80456780058', 'G2610F', '2030-10-31',
   'FOTO', 'Centro de Material', 'Ambulatório', 'Embalagem violada.', 'Lote devolvido ao fornecedor.', 'Técnico C',
   'SUGESTAO_TROCA_LOTE', 'POTENCIAL', NULL);

INSERT INTO desvio (id, ativo, material_id, fabricante_id, marca_id, importacao_id, data, vigihosp, registroanvisa,
                    lote, validade, formaentrada, amostra, servico, unidade, motivo, providencias, tecnicousep,
                    desfecho, graudano, processosei)
SELECT gen_random_uuid(), true, m.id, f.id, ma.id, i.id, c.data, c.vigihosp, c.registro, c.lote, c.validade,
       'Notificação VigiHosp', c.amostra, c.servico, c.unidade, c.motivo, c.providencias, c.tecnico, c.desfecho,
       c.grau, c.processo
FROM carga_desvio c
JOIN material m ON m.codigo = c.codigo
JOIN fabricante f ON f.nome = c.fabricante
LEFT JOIN marca ma ON ma.nome = c.marca
CROSS JOIN importacao_tecnovigilancia i;

INSERT INTO anvisa_produto_saude (id, numero_registro_cadastro, numero_processo, nome_tecnico, classe_risco,
                                  nome_comercial, cnpj_detentor_registro_cadastro, detentor_registro_cadastro,
                                  nome_fabricante, nome_pais_fabric, dt_pub_registro_cadastro,
                                  validade_registro_cadastro, dt_atualizacao_dado)
SELECT gen_random_uuid(), c.registro, '25351.' || substr(c.registro, 4, 6) || '/2025-10', m.nome, 'II',
       coalesce(c.marca, 'GENÉRICO'), '00.000.000/0001-' || lpad((row_number() OVER ())::text, 2, '0'),
       c.fabricante || ' LTDA', c.fabricante, 'BRASIL', '15/01/2025', '15/01/2035', '17/11/2026'
FROM carga_avaliacao c JOIN material m ON m.codigo = c.codigo;

INSERT INTO importacaoanvisa (id, sha256, dt_atualizacao_dado, importado_em)
VALUES (gen_random_uuid(), repeat('b', 64), '2026-11-17 08:00', '2026-11-17 03:05');

INSERT INTO revisao (id, data, usuario) VALUES
  (-3, now(), NULL),
  (-2, '2026-10-17 14:20', 'admin@pid.hc'),
  (-1, '2026-10-20 09:40', 'admin@pid.hc');

INSERT INTO importacaoanvisa_aud (id, rev, revtype, sha256, dt_atualizacao_dado, importado_em)
SELECT id, -3, 0, sha256, dt_atualizacao_dado, importado_em FROM importacaoanvisa;

INSERT INTO avaliacao_aud (id, rev, revtype, ativo, material_id, fabricante_id, marca_id, registroanvisa, data,
                           resultado, local, descricao, parecer)
SELECT id, -2, 0, ativo, material_id, fabricante_id, marca_id, registroanvisa, data, resultado, local, descricao,
       parecer
FROM avaliacao WHERE data = '2026-10-17';

INSERT INTO importacao_tecnovigilancia_aud (id, rev, revtype, ativo, nome_arquivo, sha256, total_registros)
SELECT id, -1, 0, ativo, nome_arquivo, sha256, total_registros FROM importacao_tecnovigilancia;
