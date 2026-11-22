
    create table Acesso (
        id uuid not null,
        data timestamp(6) not null,
        tipo varchar(255) not null check (tipo in ('LOGIN','LOGIN_RECUSADO','LOGOUT')),
        usuario varchar(255) not null,
        primary key (id)
    );

    alter table if exists Avaliacao_AUD 
       add column ativo boolean;

    alter table if exists Desvio_AUD 
       add column ativo boolean;

    alter table if exists Fabricante_AUD 
       add column ativo boolean;

    alter table if exists importacao_tecnovigilancia_AUD 
       add column ativo boolean;

    alter table if exists Marca_AUD 
       add column ativo boolean;

    alter table if exists Material_AUD 
       add column ativo boolean;

    alter table if exists Usuario_AUD 
       add column ativo boolean;
