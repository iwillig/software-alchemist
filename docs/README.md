# Software alchemist Docs

Software Alchemist Docs

## Setup

This project uses `pipenv` for dependency management. To install dependencies:

```bash
pipenv install
```

## Quick Start

1. **Write your documentation** in org-mode format in the `chapters/` directory
2. **Build the documentation**: `make`
3. **Preview locally**: `make serve` (opens at http://localhost:4030)

## Project Structure

```
docs/
├── chapters/           # Your documentation chapters (org files)
├── css/               # Styling (Pico.css + custom)
├── templates/         # HTML templates for Pandoc
├── tools/             # Utility scripts
├── metadata.yml       # Book metadata and configuration
├── Makefile          # Build commands
└── Pipfile           # Python dependencies
```

## Available Commands

### Building

- `make` or `make index.html` - Build the documentation
- `make clean` - Remove generated files
- `make ba` - Clean and rebuild everything

### Development

- `make serve` - Start local web server (port 4030)
- `make watch` - Auto-rebuild on file changes
- `make grammar` - Check grammar in all org files
- `make typos` - Check for typos
- `make check` - Run all checks (lint, typos, grammar)

### Code Quality

- `make lint` - Check Python code style
- `make format` - Format Python code
- `make lint-fix` - Auto-fix linting issues

## Writing Documentation

### Adding New Chapters

1. Create a new `.org` file in the `chapters/` directory
2. Add the file path to the `sections` list in `metadata.yml`
3. Rebuild with `make`

### Org-Mode Syntax

The documentation uses org-mode syntax. Here are common elements:

```org
* Heading 1
** Heading 2
*** Heading 3

*bold* /italic/ _underline_ +strikethrough+ =verbatim= ~code~

- Bullet list
  - Nested item

1. Numbered list
2. Second item

#+BEGIN_SRC python
def example():
    return "code block"
#+END_SRC

[[https://example.com][External link]]
```

## Features

- **Pandoc-based build** - Converts org-mode to beautiful HTML
- **Pico.css styling** - Clean, responsive design
- **Table of contents** - Auto-generated from headings
- **Syntax highlighting** - For code blocks
- **Grammar checking** - Via LanguageTool API
- **Live reload** - With `make watch`
- **Python tooling** - Managed with pipenv

## Requirements

- Python 3.13+
- Pandoc
- yq (for YAML processing)
- Make
- Internet connection (for grammar checking)

### Installing Dependencies

#### macOS
```bash
brew install pandoc yq
pipenv install
```

#### Ubuntu/Debian
```bash
apt-get install pandoc
snap install yq
pipenv install
```

## Author

Ivan Willig <iwillig@gmail.com>

## License

Copyright © 2025 Ivan Willig