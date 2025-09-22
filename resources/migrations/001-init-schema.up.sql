-- ;;

PRAGMA foreign_keys = ON;

-- ;;

create table file (
       id integer primary key,
       path text unique
);


-- ;;

create table symbol (
       id integer primary key
       name text unique
       type text,
       value text,
       value_type text
);

-- ;;

create table namespace (
       id integer primary key,
       name text unique
);


-- ;;

create table component (
       id integer primary key,
       name text unique
);


-- ;;

create table document (
       id integer primary key,
       name text unique
);


-- ;;

CREATE VIRTUAL TABLE symbol_fts USING fts5(
  name,
  value,
  content='symbol',
  content_rowid='name'
);

-- ;;

CREATE TRIGGER symbol_ai AFTER INSERT ON symbol BEGIN
  INSERT INTO symbol_fts (rowid, name, value) VALUES (new.name, new.name, new.value);
END;
