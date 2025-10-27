create table if not exists users (
                                     id bigserial primary key,
                                     username text not null unique,
                                     password_hash text not null
);

create table if not exists files (
                                     id uuid primary key,
                                     original_name text not null,
                                     content_type text,
                                     size_bytes bigint not null,
                                     storage_path text not null,
                                     download_token text not null unique,
                                     created_at timestamptz not null default now(),
    last_download_at timestamptz null,
    download_count bigint not null default 0
    );

create index if not exists files_last_download_idx on files (last_download_at);
