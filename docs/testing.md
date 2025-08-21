# Testing Guide

## Overview

The Software Archivist project uses pytest for testing. The tests verify that:

- Alembic migrations create the correct database schema
- SQLAlchemy models reflect correctly from the migrated schema
- FTS5 full-text search works properly
- All ORM operations function as expected

## Prerequisites

Ensure you have the development dependencies installed:

```bash
# Install all dependencies including dev packages
pipenv install --dev

# Or if pytest is not in Pipfile yet
pipenv install pytest pytest-cov
```

## Running Tests

### Run All Tests

```bash
# Using pipenv
pipenv run pytest

# Or activate the virtual environment first
pipenv shell
pytest
```

### Run Specific Test Files

```bash
# Run only model tests
pipenv run pytest tests/test_models.py

# Run with verbose output
pipenv run pytest tests/test_models.py -v

# Run with even more verbose output (shows print statements)
pipenv run pytest tests/test_models.py -vv
```

### Run Specific Test Classes or Methods

```bash
# Run a specific test class
pipenv run pytest tests/test_models.py::TestORMOperations

# Run a specific test method
pipenv run pytest tests/test_models.py::TestORMOperations::test_query_all_people

# Run tests matching a pattern
pipenv run pytest -k "test_fts"  # Runs all tests with "test_fts" in the name
```

### Run with Coverage

```bash
# Generate coverage report
pipenv run pytest --cov=software_archivist tests/

# Generate HTML coverage report
pipenv run pytest --cov=software_archivist --cov-report=html tests/

# View the HTML report
open htmlcov/index.html  # On macOS
# or
xdg-open htmlcov/index.html  # On Linux
```

### Run with Different Output Formats

```bash
# Show failed tests only
pipenv run pytest --tb=short

# Show local variables in tracebacks
pipenv run pytest --showlocals

# Stop after first failure
pipenv run pytest -x

# Stop after N failures
pipenv run pytest --maxfail=2

# Show slowest N tests
pipenv run pytest --durations=10
```

## Test Structure

### Test Files Location

```text
software-archivist/
├── tests/
│   ├── conftest.py       # Pytest configuration and fixtures (if needed)
│   └── test_models.py    # Model and ORM tests
```

### What the Tests Cover

1. **Model Reflection** (`TestModelReflection`)
   - Verifies all models are correctly reflected from the database
   - Checks model attributes match the schema

2. **ORM Operations** (`TestORMOperations`)
   - Tests CRUD operations using SQLAlchemy ORM
   - Insert, update, delete, and query operations

3. **FTS5 Search** (`TestFTS5Search`)
   - Tests full-text search functionality
   - Verifies FTS5 triggers work with ORM operations
   - Tests search methods: `Document.search()`, `search_with_snippets()`, `search_by_content()`

4. **Custom Methods** (`TestCustomMethods`)
   - Tests helper methods added to models
   - `Person.get_disciplines()`, `System.get_documents()`, `System.get_repos()`

5. **Complex Queries** (`TestComplexQueries`)
   - Tests JOIN operations
   - Tests aggregation queries

## Understanding Test Fixtures

The tests use several pytest fixtures defined in `test_models.py`:

- **`in_memory_engine`**: Creates an in-memory SQLite database with all
  migrations applied
- **`session`**: Provides a database session for tests
- **`models`**: Imports and configures the project's actual models for testing
- **`populated_session`**: Provides a session with test data already inserted

## Debugging Failed Tests

### Show More Details

```bash
# Show print statements and logging
pipenv run pytest -s tests/test_models.py

# Show full diff for assertion failures
pipenv run pytest -vv tests/test_models.py

# Drop into debugger on failures
pipenv run pytest --pdb tests/test_models.py
```

### Common Issues and Solutions

#### 1. Import Errors

If you see import errors for `software_archivist`:

```bash
# Install the package in development mode
pipenv install -e .

# Or add to PYTHONPATH
export PYTHONPATH="${PYTHONPATH}:$(pwd)"
pipenv run pytest
```

#### 2. Alembic Configuration Not Found

The tests expect `alembic.ini` in the project root. If it's missing:

```bash
# Ensure you're in the project root
cd /path/to/software-archivist

# Check alembic.ini exists
ls alembic.ini
```

#### 3. Migration Errors

If migrations fail in tests:

```bash
# Test migrations manually
pipenv run alembic upgrade head
pipenv run alembic downgrade base
```

#### 4. FTS5 Not Available

SQLite FTS5 should be available by default in Python's sqlite3. To verify:

```python
import sqlite3
conn = sqlite3.connect(':memory:')
conn.execute('CREATE VIRTUAL TABLE test USING fts5(content)')
# Should not raise an error
```

## Continuous Integration

### GitHub Actions Example

Create `.github/workflows/test.yml`:

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up Python
      uses: actions/setup-python@v2
      with:
        python-version: '3.9'
    
    - name: Install pipenv
      run: pip install pipenv
    
    - name: Install dependencies
      run: pipenv install --dev
    
    - name: Run tests
      run: pipenv run pytest --cov=software_archivist tests/
```

## Writing New Tests

### Adding Test Data

To add more test data, modify the `populated_session` fixture:

```python
@pytest.fixture
def populated_session(session, models):
    # Add your test data here
    new_entity = models.YourModel(
        field1='value1',
        field2='value2'
    )
    session.add(new_entity)
    session.commit()
    return session
```

### Testing New Model Methods

Add tests to the `TestCustomMethods` class:

```python
class TestCustomMethods:
    def test_your_new_method(self, populated_session, models):
        """Test YourModel.your_method()."""
        obj = populated_session.query(models.YourModel).first()
        result = obj.your_method(populated_session)
        assert result == expected_value
```

### Testing New FTS5 Features

Add tests to the `TestFTS5Search` class:

```python
class TestFTS5Search:
    def test_advanced_search(self, populated_session, models):
        """Test advanced FTS5 search features."""
        # Test phrase search
        results = models.Document.search(populated_session, '"exact phrase"')
        assert len(results) > 0
```

## Performance Testing

For performance-sensitive code like FTS5 searches:

```bash
# Profile test execution
pipenv run pytest --profile tests/test_models.py

# Benchmark specific tests
pipenv run pytest tests/test_models.py::TestFTS5Search --benchmark-only
```

## Test Database Inspection

To inspect the test database during debugging:

```python
# Add this to any test to inspect the database
def test_inspect_db(self, in_memory_engine):
    from sqlalchemy import inspect
    inspector = inspect(in_memory_engine)
    
    # List all tables
    print("Tables:", inspector.get_table_names())
    
    # Inspect a specific table
    columns = inspector.get_columns('system_document')
    for col in columns:
        print(f"Column: {col['name']} - Type: {col['type']}")
```

## Quick Test Commands Reference

```bash
# Most common commands
pipenv run pytest                           # Run all tests
pipenv run pytest -v                        # Verbose output
pipenv run pytest -x                        # Stop on first failure
pipenv run pytest --lf                      # Run last failed tests
pipenv run pytest -k "search"               # Run tests matching "search"
pipenv run pytest --cov=software_archivist  # With coverage
```
