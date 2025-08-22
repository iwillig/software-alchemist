
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
       discipline text references discipline(id)
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
       lifecycle_state  integer references lifecycle_state(id),
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
       slug        text primary key,
       description text
);

create table knowledge_of_software_system (
       id              integer primary key asc,
       knowledge_level text references knowledge_level(slug),
       person          text references person(slug),
       software_system text references software_system(slug),
       software_discipline text references software_discipline(slug)
);




.import csv/software_disciplines.csv software_discipline -csv --skip 1

.import csv/teams.csv person_team --csv --skip 1

.import csv/people.csv person --csv --skip 1
.import csv/knowledge_of_types.csv knowledge_level --csv --skip 1


.import csv/software_respo.csv software_repo --csv --skip 1

-- Lifecycle support
.import csv/software_system_lifecycle_state_types.csv software_system_lifecycle_state_type --csv --skip 1
.import csv/software_system_lifecycle_states.csv software_system_lifecycle_state --csv --skip 1
--
.import csv/software_system_levels.csv software_system_level --csv --skip 1

-- backend services
.import csv/backend_services_software_systems.csv software_system --csv --skip 1
-- locations
.import csv/software_system_locations.csv software_system_location --csv --skip 1

--- people's disciplines
.import csv/person_disciplines.csv person_discipline --csv --skip 1

-- people's different understanding of the systems backend
.import csv/backend_knowledge_of_systems.csv knowledge_of_software_system --csv --skip 1

-- default document types
.import csv/system_document_types.csv system_document_type --csv --skip 1
.import csv/system_document_formats.csv system_document_format --csv --skip 1

-- backend runbooks
.import csv/backend-runbooks.csv system_document --csv --skip 1

.import csv/document_relevant_systems.csv document_relevant_system --csv --skip 1
