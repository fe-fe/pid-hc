CREATE TABLE anexo_avaliacao (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    caminho character varying(255),
    nome character varying(255),
    atualizado_por uuid,
    criado_por uuid,
    avaliacao_id uuid NOT NULL
);

CREATE TABLE anvisa_produto_saude (
    id uuid NOT NULL,
    classe_risco character varying(255),
    cnpj_detentor_registro_cadastro character varying(255),
    detentor_registro_cadastro text,
    dt_atualizacao_dado character varying(255),
    dt_pub_registro_cadastro character varying(255),
    nome_comercial text,
    nome_fabricante text,
    nome_pais_fabric character varying(255),
    nome_tecnico text,
    numero_processo character varying(255),
    numero_registro_cadastro character varying(255) NOT NULL,
    validade_registro_cadastro character varying(255)
);

CREATE TABLE avaliacao (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    data date NOT NULL,
    descricao text,
    local character varying(255),
    parecer text,
    registroanvisa character varying(255),
    resultado character varying(255) NOT NULL,
    atualizado_por uuid,
    criado_por uuid,
    fabricante_id uuid NOT NULL,
    marca_id uuid,
    material_id uuid NOT NULL,
    CONSTRAINT avaliacao_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['APROVADO'::character varying, 'REPROVADO'::character varying])::text[])))
);

CREATE TABLE desvio (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    amostra character varying(255),
    data date,
    desfecho character varying(255),
    formaentrada character varying(255),
    graudano character varying(255),
    lote character varying(255),
    motivo text,
    notificacaoanvisa character varying(255),
    processosei character varying(255),
    providencias text,
    registroanvisa character varying(255),
    servico character varying(255),
    tecnicousep character varying(255),
    unidade character varying(255),
    validade date,
    vigihosp character varying(255),
    atualizado_por uuid,
    criado_por uuid,
    fabricante_id uuid NOT NULL,
    importacao_id uuid,
    marca_id uuid,
    material_id uuid NOT NULL,
    CONSTRAINT desvio_amostra_check CHECK (((amostra)::text = ANY ((ARRAY['SIM'::character varying, 'NAO'::character varying, 'FOTO'::character varying])::text[]))),
    CONSTRAINT desvio_desfecho_check CHECK (((desfecho)::text = ANY ((ARRAY['QUEIXA_PONTUAL'::character varying, 'ESCLARECIMENTO'::character varying, 'INCONCLUSIVO'::character varying, 'DESVIO_FINALIDADE'::character varying, 'SUGESTAO_TROCA_LOTE'::character varying, 'SUGESTAO_REPROVACAO_MARCA'::character varying, 'SUGESTAO_REVISAO_DESCRITIVO'::character varying, 'SUGESTAO_REPOSICAO'::character varying, 'SUGESTAO_RECOLHIMENTO'::character varying, 'SUGESTAO_CAPACITACAO'::character varying])::text[]))),
    CONSTRAINT desvio_graudano_check CHECK (((graudano)::text = ANY ((ARRAY['AUSENTE'::character varying, 'POTENCIAL'::character varying, 'LEVE'::character varying, 'MODERADO'::character varying, 'GRAVE'::character varying, 'OBITO'::character varying])::text[])))
);

CREATE TABLE fabricante (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    nome character varying(255) NOT NULL,
    atualizado_por uuid,
    criado_por uuid
);

CREATE TABLE importacao_tecnovigilancia (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    nome_arquivo character varying(255) NOT NULL,
    sha256 character varying(64) NOT NULL,
    total_registros integer NOT NULL,
    atualizado_por uuid,
    criado_por uuid
);

CREATE TABLE importacaoanvisa (
    id uuid NOT NULL,
    dt_atualizacao_dado timestamp(6) without time zone NOT NULL,
    importado_em timestamp(6) without time zone NOT NULL,
    sha256 character varying(64) NOT NULL
);

CREATE TABLE marca (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    nome character varying(255) NOT NULL,
    atualizado_por uuid,
    criado_por uuid
);

CREATE TABLE material (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    codigo character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    atualizado_por uuid,
    criado_por uuid
);

CREATE TABLE usuario (
    id uuid NOT NULL,
    ativo boolean,
    atualizado_em timestamp(6) without time zone,
    criado_em timestamp(6) without time zone,
    cpf character varying(11),
    email character varying(255) NOT NULL,
    nome character varying(255),
    perfil character varying(255) NOT NULL,
    senha character varying(255) NOT NULL,
    atualizado_por uuid,
    criado_por uuid,
    CONSTRAINT usuario_perfil_check CHECK (((perfil)::text = ANY ((ARRAY['ADMINISTRADOR'::character varying, 'ANALISTA'::character varying])::text[])))
);

ALTER TABLE anexo_avaliacao ADD CONSTRAINT anexo_avaliacao_pkey PRIMARY KEY (id);

ALTER TABLE anvisa_produto_saude ADD CONSTRAINT anvisa_produto_saude_pkey PRIMARY KEY (id);

ALTER TABLE avaliacao ADD CONSTRAINT avaliacao_pkey PRIMARY KEY (id);

ALTER TABLE desvio ADD CONSTRAINT desvio_pkey PRIMARY KEY (id);

ALTER TABLE fabricante ADD CONSTRAINT fabricante_pkey PRIMARY KEY (id);

ALTER TABLE importacao_tecnovigilancia ADD CONSTRAINT importacao_tecnovigilancia_pkey PRIMARY KEY (id);

ALTER TABLE importacaoanvisa ADD CONSTRAINT importacaoanvisa_pkey PRIMARY KEY (id);

ALTER TABLE marca ADD CONSTRAINT marca_pkey PRIMARY KEY (id);

ALTER TABLE material ADD CONSTRAINT material_pkey PRIMARY KEY (id);

ALTER TABLE marca ADD CONSTRAINT uk31y3b9y9nthoykx6ukxpgvxw UNIQUE (nome);

ALTER TABLE usuario ADD CONSTRAINT uk4tdehxj7dh8ghfc68kbwbsbll UNIQUE (email);

ALTER TABLE material ADD CONSTRAINT uk7k0nl26bdwex3s76dgmb59w73 UNIQUE (codigo);

ALTER TABLE usuario ADD CONSTRAINT uk898atepo5gx8dqj60c07k766b UNIQUE (cpf);

ALTER TABLE importacao_tecnovigilancia ADD CONSTRAINT uk89v2awwlvg1a5s1p436f3llk3 UNIQUE (sha256);

ALTER TABLE fabricante ADD CONSTRAINT ukq6vy96nn48jyc9ii7x1qoym6s UNIQUE (nome);

ALTER TABLE usuario ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);

CREATE INDEX idxhpgoilvajegue4bpoufxc93vy ON anvisa_produto_saude USING btree (numero_registro_cadastro);

ALTER TABLE importacao_tecnovigilancia ADD CONSTRAINT fk1ao8kbt4fqjcfr4www2ugwxmv FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE avaliacao ADD CONSTRAINT fk1bcgqdj1gkev8xq83imrw5yu6 FOREIGN KEY (material_id) REFERENCES material(id);

ALTER TABLE desvio ADD CONSTRAINT fk2d5q4ccqmi08na910kv218sgs FOREIGN KEY (fabricante_id) REFERENCES fabricante(id);

ALTER TABLE marca ADD CONSTRAINT fk47l2ptgcvohe5l747mwxew0f1 FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE desvio ADD CONSTRAINT fk4e73cim60otj788a7p67j1227 FOREIGN KEY (material_id) REFERENCES material(id);

ALTER TABLE fabricante ADD CONSTRAINT fk5c8e2pindp1vql2rc8wguy25m FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE usuario ADD CONSTRAINT fk5kghdjsi5imuw7yehg1qsew8 FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE avaliacao ADD CONSTRAINT fk61l7933eyjyf1hqw99sa3du0t FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE avaliacao ADD CONSTRAINT fk6kqqwb6hu8cwh3l9ekpjdaoru FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE desvio ADD CONSTRAINT fk8iex5xkkbq0x0c3r76io3etns FOREIGN KEY (marca_id) REFERENCES marca(id);

ALTER TABLE desvio ADD CONSTRAINT fk8ix9651vvbbej8vtl8tc8kx62 FOREIGN KEY (importacao_id) REFERENCES importacao_tecnovigilancia(id);

ALTER TABLE anexo_avaliacao ADD CONSTRAINT fka25atq9v58siml81a2lgm5ec7 FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE marca ADD CONSTRAINT fkh25wdiordnwyb3kkqxhldf4xu FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE avaliacao ADD CONSTRAINT fkiarlpedvlo3o5c4b8u692gkuo FOREIGN KEY (fabricante_id) REFERENCES fabricante(id);

ALTER TABLE material ADD CONSTRAINT fkn4jjwq794yrlk88bdf5gxg4ng FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE desvio ADD CONSTRAINT fko278i0sso6c4viuvtsfetmyxf FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE material ADD CONSTRAINT fkoykbi1u4dpd2cy4o9imeo62cu FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE desvio ADD CONSTRAINT fkq9hrhuloqky5p54x22axs0f5d FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE anexo_avaliacao ADD CONSTRAINT fkqo3jwvklwpq7yytol22doy5eo FOREIGN KEY (criado_por) REFERENCES usuario(id);

ALTER TABLE anexo_avaliacao ADD CONSTRAINT fkqrcbo0tsf4pp7mbsfr030a9k1 FOREIGN KEY (avaliacao_id) REFERENCES avaliacao(id);

ALTER TABLE importacao_tecnovigilancia ADD CONSTRAINT fkr7bbfkmlt2l1kysg7xx2iw2we FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE usuario ADD CONSTRAINT fkrmdb1b3if3foh76xpdldac0f2 FOREIGN KEY (atualizado_por) REFERENCES usuario(id);

ALTER TABLE avaliacao ADD CONSTRAINT fksfix6fuvtebw7qootry5brdun FOREIGN KEY (marca_id) REFERENCES marca(id);

ALTER TABLE fabricante ADD CONSTRAINT fksohdb4ix3lxu6o5x4705esaum FOREIGN KEY (atualizado_por) REFERENCES usuario(id);
