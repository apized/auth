create table api_key
(
    id              uuid         not null primary key,
    created_at      timestamp,
    created_by      uuid,
    last_updated_at timestamp,
    last_updated_by uuid,
    version         bigint       not null,
    metadata        jsonb default '{}',

    user_id         uuid         not null references "user" (id),
    name            varchar(255) not null,
    secret_hash     varchar(64)  not null
);

create index idx_api_key_user_id on api_key (user_id);
