alter table passkey
  add constraint fk_passkey_user_id_user
  foreign key (user_id) references "user" (id) on delete cascade;
