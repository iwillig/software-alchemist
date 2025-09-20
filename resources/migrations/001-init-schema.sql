
PRAGMA foreign_keys = ON;

--
create table discipline (
       id integer primary key asc,
       name text unique,
       description text,
       platform text
);

--
create table person_team (
       id integer primary key asc,
       name text unique
);

--
create table person (
       id integer primary key asc,
       name text unique,
       person_team_id integer references person_team(id)
);

create table person_discipline (
       id integer primary key asc,
       person_id text references person(id),
       discipline_id text references discipline(id)
);

create table repo (
       id integer primary key asc,
       url  text,
       discipline text references discipline(id)
);

create table system_level (
       id integer primary key asc,
       name text unique,
       description text
);

create table lifecycle_state_type (
       name text primary key,
       description text
);

create table lifecycle_state (
       id             integer primary key asc,
       lifecycle_type text references lifecycle_state_type(name),
       name           text
);

create table system (
       id               integer primary key asc,
       name             text unique,
       display_name     text unique,
       system_usage     text,

       system_level     integer references system_level(id),
       lifecycle_state  integer references lifecycle_state(Cid),
       discipline       integer references discipline(name)
);

create table document_type (
       name text primary key
);

create table document_format (
       name text primary key
);

create table system_document (
       id              integer primary key asc,
       name            text unique,
       document_type   text references system_document_type(slug),
       document_format text references system_document_format(slug)
);

create table document_relevant_system (
       id              integer primary key asc,
       document        integer references system_document(id),
       software_system text references software_system(slug)
);

create table software_system_location (
       id              integer primary key asc,
       software_repo   text references software_repo(slug),
       software_system text references software_system(slug)
);

create table knowledge_level (
       id          integer primary key asc,
       slug        text unique,
       description text
);

create table knowledge_of_software_system (
       id              integer primary key asc,
       knowledge_level text references knowledge_level(slug),
       person          text references person(slug),
       software_system text references software_system(slug),
       software_discipline text references discipline(slug)
);
