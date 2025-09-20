.DEFAULT_GOAL := index.html

sections := $(shell yq '.sections[]' metadata.yml)

index.html: $(sections) metadata.yml
	pandoc --metadata-file metadata.yml \
	--toc --toc-depth=5 \
	--template templates/template.html \
	--citeproc \
	-F pandoc-include \
	-F pandoc-plantuml \
	-F pandoc-numbering \
	-f org -t html $(sections) \
	-o $@
	@echo "build successful"

## Python linting and formatting
.PHONY: lint
lint:
	@echo "Running Ruff linter..."
	@pipenv run ruff check tools/*.py
	@echo "Checking Python formatting..."
	@pipenv run ruff format --check tools/*.py

.PHONY: format
format:
	@echo "Formatting Python code with Ruff..."
	@pipenv run ruff format tools/*.py
	@echo "Fixing auto-fixable lint issues..."
	@pipenv run ruff check --fix tools/*.py

.PHONY: lint-fix
lint-fix:
	@echo "Auto-fixing Python lint issues..."
	@pipenv run ruff check --fix tools/*.py

## Re Build All tasks
.PHONY: ba
ba: clean lint index.html

.PHONY: typos
typos:
	typos -w
.PHONY: grammar
grammar:
	@pipenv run python tools/check_grammar.py

.PHONY: check
check: lint typos grammar
	@echo "All checks passed!"

.PHONY: watch
watch:
	watchfiles "make index.html" metadata.yml chapters/*.org templates/template.html

.PHONY: serve
serve: index.html
	python -m http.server 4030

.PHONY: clean
clean:
	-rm index.html
	-rm index.md
	-rm build_readme.md