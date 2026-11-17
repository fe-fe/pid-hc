
    create table anexo_avaliacao_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        caminho varchar(255),
        nome varchar(255),
        avaliacao_id uuid,
        primary key (REV, id)
    );

    create table Avaliacao_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        data date,
        descricao text,
        local varchar(255),
        parecer text,
        registroAnvisa varchar(255),
        resultado varchar(255) check (resultado in ('APROVADO','REPROVADO')),
        fabricante_id uuid,
        marca_id uuid,
        material_id uuid,
        primary key (REV, id)
    );

    create table Desvio_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        amostra varchar(255) check (amostra in ('SIM','NAO','FOTO')),
        data date,
        desfecho varchar(255) check (desfecho in ('QUEIXA_PONTUAL','ESCLARECIMENTO','INCONCLUSIVO','DESVIO_FINALIDADE','SUGESTAO_TROCA_LOTE','SUGESTAO_REPROVACAO_MARCA','SUGESTAO_REVISAO_DESCRITIVO','SUGESTAO_REPOSICAO','SUGESTAO_RECOLHIMENTO','SUGESTAO_CAPACITACAO')),
        formaEntrada varchar(255),
        grauDano varchar(255) check (grauDano in ('AUSENTE','POTENCIAL','LEVE','MODERADO','GRAVE','OBITO')),
        lote varchar(255),
        motivo text,
        notificacaoAnvisa varchar(255),
        processoSei varchar(255),
        providencias text,
        registroAnvisa varchar(255),
        servico varchar(255),
        tecnicoUsep varchar(255),
        unidade varchar(255),
        validade date,
        vigihosp varchar(255),
        fabricante_id uuid,
        importacao_id uuid,
        marca_id uuid,
        material_id uuid,
        primary key (REV, id)
    );

    create table Fabricante_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        nome varchar(255),
        primary key (REV, id)
    );

    create table importacao_tecnovigilancia_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        nome_arquivo varchar(255),
        sha256 varchar(64),
        total_registros integer,
        primary key (REV, id)
    );

    create table ImportacaoAnvisa_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        dt_atualizacao_dado timestamp(6),
        importado_em timestamp(6),
        sha256 varchar(64),
        primary key (REV, id)
    );

    create table Marca_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        nome varchar(255),
        primary key (REV, id)
    );

    create table Material_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        codigo varchar(255),
        nome varchar(255),
        primary key (REV, id)
    );

    create table Revisao (
        id bigint not null,
        data timestamp(6),
        usuario varchar(255),
        primary key (id)
    );

    create table Usuario_AUD (
        id uuid not null,
        REV bigint not null,
        REVTYPE smallint,
        cpf varchar(11),
        email varchar(255),
        nome varchar(255),
        perfil varchar(255) check (perfil in ('ADMINISTRADOR','ANALISTA')),
        primary key (REV, id)
    );

    create sequence Revisao_SEQ start with 1 increment by 50;

    alter table if exists anexo_avaliacao_AUD 
       add constraint FKjeauwagewlphnakdb50kig965 
       foreign key (REV) 
       references Revisao;

    alter table if exists Avaliacao_AUD 
       add constraint FK7jm82mg1g33exh7hfl0q5jm7s 
       foreign key (REV) 
       references Revisao;

    alter table if exists Desvio_AUD 
       add constraint FKb50962xfmlf0c5e8xhl2bolta 
       foreign key (REV) 
       references Revisao;

    alter table if exists Fabricante_AUD 
       add constraint FKkols60byl0tgwjtdwbydq2vwf 
       foreign key (REV) 
       references Revisao;

    alter table if exists importacao_tecnovigilancia_AUD 
       add constraint FKmk2pic6fhbyq10pdsftwh9ith 
       foreign key (REV) 
       references Revisao;

    alter table if exists ImportacaoAnvisa_AUD 
       add constraint FKo37ujsxh5x76wbnplaiwe2b6p 
       foreign key (REV) 
       references Revisao;

    alter table if exists Marca_AUD 
       add constraint FK9b67wgpafdi8uiemwqxotjcnb 
       foreign key (REV) 
       references Revisao;

    alter table if exists Material_AUD 
       add constraint FKl15vwhkl8g9i6cx5imrsq3w8u 
       foreign key (REV) 
       references Revisao;

    alter table if exists Usuario_AUD 
       add constraint FK26ri8f6obp8ue0vpmgn3rusth 
       foreign key (REV) 
       references Revisao;


CREATE TEMPORARY TABLE historico_inicial AS
    SELECT 'anexo_avaliacao' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM anexo_avaliacao x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'anexo_avaliacao', x.id, 1, x.atualizado_em, u.email
    FROM anexo_avaliacao x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'avaliacao' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM avaliacao x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'avaliacao', x.id, 1, x.atualizado_em, u.email
    FROM avaliacao x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'desvio' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM desvio x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'desvio', x.id, 1, x.atualizado_em, u.email
    FROM desvio x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'fabricante' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM fabricante x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'fabricante', x.id, 1, x.atualizado_em, u.email
    FROM fabricante x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'importacao_tecnovigilancia' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM importacao_tecnovigilancia x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'importacao_tecnovigilancia', x.id, 1, x.atualizado_em, u.email
    FROM importacao_tecnovigilancia x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'marca' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM marca x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'marca', x.id, 1, x.atualizado_em, u.email
    FROM marca x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'material' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM material x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'material', x.id, 1, x.atualizado_em, u.email
    FROM material x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'usuario' AS tabela, x.id, 0 AS tipo, x.criado_em AS data, u.email AS usuario
    FROM usuario x LEFT JOIN usuario u ON u.id = x.criado_por
    UNION ALL
    SELECT 'usuario', x.id, 1, x.atualizado_em, u.email
    FROM usuario x LEFT JOIN usuario u ON u.id = x.atualizado_por
    WHERE x.atualizado_em > x.criado_em
    UNION ALL
    SELECT 'importacaoanvisa', x.id, 0, x.importado_em, NULL
    FROM importacaoanvisa x;

ALTER TABLE historico_inicial ADD COLUMN rev bigint;
UPDATE historico_inicial h SET rev = n.rev
FROM (SELECT tabela, id, tipo, row_number() OVER (ORDER BY data NULLS FIRST, tabela, id, tipo) AS rev FROM historico_inicial) n
WHERE n.tabela = h.tabela AND n.id = h.id AND n.tipo = h.tipo;

INSERT INTO Revisao (id, data, usuario)
SELECT rev, COALESCE(data, now()), usuario FROM historico_inicial;

INSERT INTO anexo_avaliacao_AUD (id, REV, REVTYPE, caminho, nome, avaliacao_id)
SELECT x.id, h.rev, h.tipo, x.caminho, x.nome, x.avaliacao_id
FROM anexo_avaliacao x JOIN historico_inicial h ON h.tabela = 'anexo_avaliacao' AND h.id = x.id;

INSERT INTO Avaliacao_AUD (id, REV, REVTYPE, data, descricao, local, parecer, registroAnvisa, resultado, fabricante_id, marca_id, material_id)
SELECT x.id, h.rev, h.tipo, x.data, x.descricao, x.local, x.parecer, x.registroAnvisa, x.resultado, x.fabricante_id, x.marca_id, x.material_id
FROM avaliacao x JOIN historico_inicial h ON h.tabela = 'avaliacao' AND h.id = x.id;

INSERT INTO Desvio_AUD (id, REV, REVTYPE, amostra, data, desfecho, formaEntrada, grauDano, lote, motivo, notificacaoAnvisa, processoSei, providencias, registroAnvisa, servico, tecnicoUsep, unidade, validade, vigihosp, fabricante_id, importacao_id, marca_id, material_id)
SELECT x.id, h.rev, h.tipo, x.amostra, x.data, x.desfecho, x.formaEntrada, x.grauDano, x.lote, x.motivo, x.notificacaoAnvisa, x.processoSei, x.providencias, x.registroAnvisa, x.servico, x.tecnicoUsep, x.unidade, x.validade, x.vigihosp, x.fabricante_id, x.importacao_id, x.marca_id, x.material_id
FROM desvio x JOIN historico_inicial h ON h.tabela = 'desvio' AND h.id = x.id;

INSERT INTO Fabricante_AUD (id, REV, REVTYPE, nome)
SELECT x.id, h.rev, h.tipo, x.nome
FROM fabricante x JOIN historico_inicial h ON h.tabela = 'fabricante' AND h.id = x.id;

INSERT INTO importacao_tecnovigilancia_AUD (id, REV, REVTYPE, nome_arquivo, sha256, total_registros)
SELECT x.id, h.rev, h.tipo, x.nome_arquivo, x.sha256, x.total_registros
FROM importacao_tecnovigilancia x JOIN historico_inicial h ON h.tabela = 'importacao_tecnovigilancia' AND h.id = x.id;

INSERT INTO ImportacaoAnvisa_AUD (id, REV, REVTYPE, dt_atualizacao_dado, importado_em, sha256)
SELECT x.id, h.rev, h.tipo, x.dt_atualizacao_dado, x.importado_em, x.sha256
FROM importacaoanvisa x JOIN historico_inicial h ON h.tabela = 'importacaoanvisa' AND h.id = x.id;

INSERT INTO Marca_AUD (id, REV, REVTYPE, nome)
SELECT x.id, h.rev, h.tipo, x.nome
FROM marca x JOIN historico_inicial h ON h.tabela = 'marca' AND h.id = x.id;

INSERT INTO Material_AUD (id, REV, REVTYPE, codigo, nome)
SELECT x.id, h.rev, h.tipo, x.codigo, x.nome
FROM material x JOIN historico_inicial h ON h.tabela = 'material' AND h.id = x.id;

INSERT INTO Usuario_AUD (id, REV, REVTYPE, cpf, email, nome, perfil)
SELECT x.id, h.rev, h.tipo, x.cpf, x.email, x.nome, x.perfil
FROM usuario x JOIN historico_inicial h ON h.tabela = 'usuario' AND h.id = x.id;

SELECT setval('Revisao_SEQ', GREATEST((SELECT COALESCE(max(id), 0) FROM Revisao), 1));

DROP TABLE historico_inicial;

ALTER TABLE anexo_avaliacao
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE avaliacao
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE desvio
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE fabricante
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE importacao_tecnovigilancia
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE marca
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE material
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;

ALTER TABLE usuario
    DROP COLUMN criado_em,
    DROP COLUMN atualizado_em,
    DROP COLUMN criado_por,
    DROP COLUMN atualizado_por;
