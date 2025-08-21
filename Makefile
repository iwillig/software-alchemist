.PHONY: help
help:
	@echo "Available targets:"
	@echo "  ba        - Build all documentation (delegates to docs/Makefile)"
	@echo "  docs      - Build documentation index.html"
	@echo "  docs-md   - Build documentation index.md"
	@echo "  docs-lint - Lint documentation markdown files"
	@echo "  docs-clean- Clean documentation build artifacts"
	@echo "  format    - Format Python code with black"
	@echo "  check     - Check Python formatting without changes"
	@echo "  migrate   - Apply database migrations"
	@echo "  test      - Run tests"

# Documentation targets - delegate to docs/Makefile
.PHONY: ba
ba:
	$(MAKE) -C docs ba

.PHONY: docs
docs:
	$(MAKE) -C docs index.html

.PHONY: docs-md
docs-md:
	$(MAKE) -C docs index.md

.PHONY: docs-lint
docs-lint:
	$(MAKE) -C docs lint

.PHONY: docs-clean
docs-clean:
	$(MAKE) -C docs clean

.PHONY: docs-watch
docs-watch:
	$(MAKE) -C docs watch

.PHONY: docs-serve
docs-serve:
	$(MAKE) -C docs serve

# Python formatting
.PHONY: format
format:
	pipenv run black .

.PHONY: check
check:
	pipenv run black --check .

# Database migrations
.PHONY: migrate
migrate:
	pipenv run alembic upgrade head

.PHONY: migration
migration:
	@read -p "Enter migration message: " msg; \
	pipenv run alembic revision --autogenerate -m "$$msg"

# Testing
.PHONY: test
test:
	pipenv run pytest

# Combined targets
.PHONY: all
all: check test docs

.PHONY: clean
clean: docs-clean
	find . -type d -name __pycache__ -exec rm -rf {} + 2>/dev/null || true
	find . -type f -name "*.pyc" -delete