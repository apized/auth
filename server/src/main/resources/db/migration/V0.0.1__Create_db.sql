create table audit_trail
(
    id             uuid         not null primary key,
    transaction_id uuid         not null,
    action         varchar(255) not null,
    type           varchar(255) not null,
    target         uuid,
    reason         varchar(255),
    timestamp      timestamp    not null,
    epoch          bigint       not null,
    payload        jsonb,
    author         uuid         not null
);

create table "user"
(
    id                      uuid         not null primary key,
    created_at              timestamp,
    created_by              uuid,
    last_updated_at         timestamp,
    last_updated_by         uuid,
    version                 bigint       not null,
    metadata                jsonb default '{}',

    name                    varchar(255),
    username                varchar(255) not null unique,
    password                varchar(255) not null,
    verified                boolean      not null,
    permissions             jsonb default '[]',
    password_reset_code     varchar(255),
    email_verification_code varchar(255)
);

create table role
(
    id              uuid   not null primary key,
    created_at      timestamp,
    created_by      uuid,
    last_updated_at timestamp,
    last_updated_by uuid,
    version         bigint not null,
    metadata        jsonb default '{}',

    name            varchar(255),
    description     varchar(1024),
    permissions     jsonb default '[]'
);

create table users_roles
(
    users_id uuid not null,
    roles_id uuid not null,

    constraint fk_user_roles_user_id_user foreign key (users_id) references "user" (id),
    constraint fk_user_roles_roles_id_role foreign key (roles_id) references role (id)
);