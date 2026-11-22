
    create table Parametro (
        chave varchar(255) not null,
        valor varchar(255) not null,
        primary key (chave)
    );

    create table Parametro_AUD (
        chave varchar(255) not null,
        REV bigint not null,
        REVTYPE smallint,
        valor varchar(255),
        primary key (REV, chave)
    );

    alter table if exists Parametro_AUD 
       add constraint FK3nra9sogylrn9g4mp7drr78dk 
       foreign key (REV) 
       references Revisao;
