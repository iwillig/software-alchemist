-- ;;

PRAGMA foreign_keys = ON;

-- ;;

create table symbol (
       name primary key,
       type text,
       value text,
       value_type text
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
