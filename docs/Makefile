 .DEFAULT_GOAL := index.html

sections := $(shell yq 	'.sections[]' metadata.yml)


css/highlighting.css:
	 pandoc --highlight-style tango --template=./templates/highlighting.css cli-implementation-plan.md -t html -o css/highlighting.css

index.html: $(sections) metadata.yml
	pandoc --metadata-file metadata.yml \
	--toc --toc-depth=5 \
	--template templates/template.html \
	--highlight-style tango \
	--citeproc \
	-F pandoc-include \
	-F pandoc-plantuml \
	-F pandoc-numbering \
	-F pandoc-mustache  \
	-f markdown -t html $(sections) \
	-o $@
	@echo "build successful"

build_readme.md: readme.md metadata.yml
	pandoc --metadata-file metadata.yml \
	--toc --toc-depth=5 \
	--highlight-style tango \
	-F pandoc-include \
	-F pandoc-plantuml \
	-F pandoc-numbering \
	-F pandoc-mustache  \
	-f markdown -t markdown readme.md \
	-o $@
	cp $@ ../readme.md
	@echo "readme successful"


index.md: $(sections) metadata.yml
	pandoc --metadata-file metadata.yml \
	--toc --toc-depth=5 \
	--highlight-style zenburn \
	--citeproc \
	-F pandoc-include \
	-F pandoc-plantuml \
	-F pandoc-numbering \
	-F pandoc-mustache  \
	-f markdown -t markdown $(sections) \
	-o $@
	@echo "build successful"

.PHONY: lint
lint:
	markdownlint *.md

## Re Build All tasks
.PHONY: ba
ba: clean lint index.html index.md build_readme.md


.PHONY: typos
typos:
	typos -w

.PHONY: watch
watch:
	watchfiles "make index.html" metadata.yml *.md templates/template.html

.PHONY: serve
serve: index.html
	python -m http.server 4030

.PHONY: clean
clean:
	-rm index.html
	-rm index.md
	-rm build_readme.md
