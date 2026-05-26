create table permission
(
    id          varchar(255) not null
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    code        varchar(255) null,
    description varchar(255) null,
    name        varchar(255) null
);

create table role
(
    id          varchar(255) not null
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    code        varchar(100) not null,
    description varchar(500) null,
    name        varchar(255) not null
);

create table role_permission
(
    role_id       varchar(255) not null,
    permission_id varchar(255) not null,
    primary key (role_id, permission_id),
    constraint FKa6jx8n8xkesmjmv6jqug6bg68
        foreign key (role_id) references role (id),
    constraint FKf8yllw1ecvwqy3ehyxawqa1qp
        foreign key (permission_id) references permission (id)
);

create table user_account
(
    id                varchar(255)                          not null
        primary key,
    created_at        datetime(6)                           not null,
    updated_at        datetime(6)                           not null,
    email             varchar(255)                          not null,
    google_id         varchar(255)                          null,
    last_login_at     datetime(6)                           null,
    name              varchar(255)                          null,
    password          varchar(255)                          null,
    phone             varchar(20)                           null,
    is_phone_verified bit                                   not null,
    status            enum ('ACTIVE', 'BANNED', 'INACTIVE') not null,
    constraint UKofnxqt1fdupw3ut1mcjeo6ey
        unique (google_id),
    constraint uk_user_email
        unique (email),
    constraint uk_user_phone
        unique (phone)
);

create table email_verify_token
(
    id         varchar(255) not null
        primary key,
    created_at datetime(6)  not null,
    updated_at datetime(6)  not null,
    expires_at datetime(6)  not null,
    token      varchar(100) not null,
    used       bit          not null,
    used_at    datetime(6)  null,
    user_id    varchar(255) not null,
    constraint idx_evt_token
        unique (token),
    constraint FK9bn25rv60vlo1xkfeyn2kx2ac
        foreign key (user_id) references user_account (id)
);

create index idx_evt_user
    on email_verify_token (user_id);

create table user_provider
(
    user_id  varchar(255) not null,
    provider varchar(255) null,
    constraint FKnhvmqlq62porhrdapc11j11bn
        foreign key (user_id) references user_account (id)
);

create table user_role
(
    user_id varchar(255) not null,
    role_id varchar(255) not null,
    primary key (user_id, role_id),
    constraint FK7ojmv1m1vrxfl3kvt5bi5ur73
        foreign key (user_id) references user_account (id),
    constraint FKa68196081fvovjhkek5m97n3y
        foreign key (role_id) references role (id)
);


