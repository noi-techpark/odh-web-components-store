create table if not exists origin
(
    uuid            varchar(255)              not null
        constraint origin_pk
            primary key,
    url             varchar(512)              not null,
    api             varchar(32)               not null,
    import_metadata jsonb default '{}'::jsonb not null
);

create unique index if not exists origin_url_uindex
    on origin (url);