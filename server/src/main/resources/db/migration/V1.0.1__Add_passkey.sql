create table passkey
(
    id              uuid         not null primary key,
    created_at      timestamp,
    created_by      uuid,
    last_updated_at timestamp,
    last_updated_by uuid,
    version         bigint       not null,
    metadata        jsonb default '{}',

    user_id         uuid         not null,
    credential_id   text         not null unique,
    public_key_cose bytea        not null,
    sign_count      bigint       not null default 0,
    aaguid          varchar(36),
    transports      varchar(255) not null default ''
);

create index idx_passkey_user_id on passkey (user_id);

create table challenge
(
    id              uuid        not null primary key,
    created_at      timestamp,
    created_by      uuid,
    last_updated_at timestamp,
    last_updated_by uuid,
    version         bigint      not null,
    metadata        jsonb default '{}',

    type            varchar(20) not null,
    user_id         uuid,
    payload         text        not null
);
