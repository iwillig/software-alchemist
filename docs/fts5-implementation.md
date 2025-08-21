# SQLite FTS5 Implementation for Document Search

## Overview

The `system_document` table has been enhanced with SQLite's FTS5
(Full-Text Search) capabilities to enable efficient searching through
document names and content.

## Database Schema Changes

### 1. Main Table Structure

The `system_document` table now includes a `content` column for storing
searchable text:

```sql
CREATE TABLE system_document (
    id INTEGER PRIMARY KEY,
    name TEXT UNIQUE,
    content TEXT,  -- New column for full-text content
    document_type TEXT REFERENCES system_document_type(slug),
    document_format TEXT REFERENCES system_document_format(slug)
);
```

### 2. FTS5 Virtual Table

A virtual table is created to enable full-text search:

```sql
CREATE VIRTUAL TABLE system_document_fts USING fts5(
    name,
    content,
    content=system_document,
    content_rowid=id
);
```

This configuration:

- Indexes both `name` and `content` fields
- Links to the main `system_document` table
- Uses the `id` column as the rowid reference

### 3. Synchronization Triggers

Three triggers keep the FTS index synchronized with the main table:

**INSERT Trigger:**

```sql
CREATE TRIGGER system_document_ai AFTER INSERT ON system_document
BEGIN
    INSERT INTO system_document_fts(rowid, name, content)
    VALUES (new.id, new.name, new.content);
END;
```

**DELETE Trigger:**

```sql
CREATE TRIGGER system_document_ad AFTER DELETE ON system_document
BEGIN
    DELETE FROM system_document_fts WHERE rowid = old.id;
END;
```

**UPDATE Trigger:**

```sql
CREATE TRIGGER system_document_au AFTER UPDATE ON system_document
BEGIN
    UPDATE system_document_fts 
    SET name = new.name, content = new.content
    WHERE rowid = new.id;
END;
```

## Using FTS5 with SQLAlchemy

### Basic Search Query

Search across both name and content fields:

```python
from sqlalchemy import text

def search_documents(session, search_query, limit=10):
    sql = text("""
        SELECT 
            sd.*,
            fts.rank
        FROM system_document sd
        JOIN system_document_fts fts ON sd.id = fts.rowid
        WHERE system_document_fts MATCH :query
        ORDER BY rank
        LIMIT :limit
    """)
    
    results = session.execute(sql, {"query": search_query, "limit": limit})
    return results.fetchall()
```

### Search with Snippets

Get text snippets showing where matches occurred:

```python
def search_with_snippets(session, search_query, limit=10):
    sql = text("""
        SELECT 
            sd.id,
            sd.name,
            snippet(system_document_fts, 1, '[', ']', '...', 32) as snippet,
            fts.rank
        FROM system_document sd
        JOIN system_document_fts fts ON sd.id = fts.rowid
        WHERE system_document_fts MATCH :query
        ORDER BY rank
        LIMIT :limit
    """)
    
    results = session.execute(sql, {"query": search_query, "limit": limit})
    return results.fetchall()
```

### Content-Only Search

Search only in the content field:

```python
def search_content_only(session, search_query, limit=10):
    sql = text("""
        SELECT sd.*
        FROM system_document sd
        JOIN system_document_fts fts ON sd.id = fts.rowid
        WHERE fts.content MATCH :query
        ORDER BY fts.rank
        LIMIT :limit
    """)
    
    results = session.execute(sql, {"query": search_query, "limit": limit})
    return results.fetchall()
```

## FTS5 Query Syntax

FTS5 supports advanced search operators:

- **Phrase search:** `"exact phrase"`
- **Boolean AND:** `term1 AND term2` or `term1 term2` (implicit AND)
- **Boolean OR:** `term1 OR term2`
- **Boolean NOT:** `term1 NOT term2`
- **Prefix search:** `pref*`
- **NEAR operator:** `term1 NEAR term2` (within 10 tokens)
- **Column filter:** `content:searchterm` (search only in content column)

### Examples

```python
# Search for documents containing both "API" and "authentication"
results = search_documents(session, "API authentication")

# Search for exact phrase
results = search_documents(session, '"REST endpoints"')

# Search with OR
results = search_documents(session, "API OR documentation")

# Prefix search
results = search_documents(session, "auth*")

# Search only in content field
results = search_documents(session, "content:security")
```

## Performance Considerations

1. **Index Size:** FTS5 creates an index approximately 10-15% the size of
   the indexed text
2. **Write Performance:** Triggers add minimal overhead to
   INSERT/UPDATE/DELETE operations
3. **Query Performance:** FTS5 queries are significantly faster than
   LIKE '%term%' queries
4. **Ranking:** The `rank` column provides relevance scoring
   (negative values, more negative = better match)

## Migration Notes

The Alembic migration (`initial-schema`) handles:

1. Creating the main table with the content column
2. Creating the FTS5 virtual table
3. Setting up synchronization triggers
4. Proper cleanup in the downgrade function

To apply the migration:

```bash
pipenv run alembic upgrade head
```

To rollback:

```bash
pipenv run alembic downgrade -1
```

## Testing FTS5 Functionality

```python
# Insert test document
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

engine = create_engine('sqlite:///archivist.db')
Session = sessionmaker(bind=engine)
session = Session()

# Insert a document
session.execute(text("""
    INSERT INTO system_document (name, content, document_type, document_format)
    VALUES (
        'API Guide',
        'Complete guide to our REST API including authentication and rate limiting',
        'technical',
        'markdown'
    )
"""))
session.commit()

# Search for it
results = search_documents(session, "authentication")
for row in results:
    print(f"Found: {row.name} with rank {row.rank}")
```

## Limitations and Considerations

1. **SQLite Only:** FTS5 is SQLite-specific. Migration to PostgreSQL would
   require using `tsvector`/`tsquery`
2. **No Stemming by Default:** Consider adding a stemming tokenizer for
   better search results
3. **Case Sensitivity:** FTS5 is case-insensitive by default
4. **Unicode Support:** FTS5 handles Unicode correctly with the default tokenizer
