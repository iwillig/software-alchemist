.PHONY: help
help:
	@echo "Available targets:"
	@echo "  format    - Format Python code with black"
	@echo "  check     - Check Python formatting without changes"
	@echo "  migrate   - Apply database migrations"
	@echo "  test      - Run tests"

# Combined targets
.PHONY: all
all: clean

.PHONY: clean
clean:
	rm -rf target
