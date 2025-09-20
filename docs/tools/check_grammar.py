#!/usr/bin/env python
"""
Grammar checker for org-mode files using LanguageTool API.

This tool checks all .org files in the project for grammar, spelling,
and style issues using the free LanguageTool HTTP API.
"""

import json
import re
import subprocess
import time
from pathlib import Path

import click
import requests
import yaml
from rich import box
from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn
from rich.table import Table
from rich.text import Text

# Console for rich output
console = Console()

# LanguageTool API configuration
LANGUAGETOOL_API_URL = "https://api.languagetool.org/v2/check"
RATE_LIMIT_DELAY = 3  # seconds between requests to respect rate limits

# Custom dictionary file path
CUSTOM_DICTIONARY_FILE = ".languagetool-dictionary.yml"


def load_custom_dictionary(path="."):
    """Load custom dictionary words from YAML file."""
    dict_path = Path(path) / CUSTOM_DICTIONARY_FILE
    custom_words = []

    if dict_path.exists():
        try:
            with open(dict_path, "r", encoding="utf-8") as f:
                data = yaml.safe_load(f)
                if isinstance(data, list):
                    custom_words = [str(word).strip() for word in data if word]
                else:
                    console.print(f"[yellow]Warning: {CUSTOM_DICTIONARY_FILE} must contain a YAML list[/yellow]")

            if custom_words:
                console.print(f"[green]✓ Loaded {len(custom_words)} words from custom dictionary[/green]")
        except yaml.YAMLError as e:
            console.print(f"[yellow]Warning: Invalid YAML in custom dictionary: {e}[/yellow]")
        except Exception as e:
            console.print(f"[yellow]Warning: Could not load custom dictionary: {e}[/yellow]")
    else:
        console.print(f"[dim]No custom dictionary found at {dict_path}[/dim]")
        console.print(f"[dim]Create {CUSTOM_DICTIONARY_FILE} with a YAML list of custom words to ignore[/dim]")

    return custom_words


def find_org_files(root_dir="."):
    """Find all .org files in the project, excluding certain directories."""
    root_path = Path(root_dir)
    exclude_dirs = {"tools", "data", "__pycache__", ".git", ".venv", "venv"}

    org_files = []
    for path in root_path.rglob("*.org"):
        # Skip if any parent directory is in exclude list
        if not any(part in exclude_dirs for part in path.parts):
            org_files.append(path)

    return sorted(org_files)


def org_to_text(org_content, file_path):
    """Convert org-mode content to plain text using pandoc."""
    try:
        result = subprocess.run(
            ["pandoc", "-f", "org", "-t", "plain"],
            input=org_content,
            capture_output=True,
            text=True,
            timeout=10,
        )
        if result.returncode == 0:
            return result.stdout
        else:
            console.print(f"[red]Pandoc conversion failed for {file_path}: {result.stderr}[/red]")
            return None
    except FileNotFoundError:
        console.print("[red]Error: pandoc is not installed. Please install pandoc to use grammar checking.[/red]")
        console.print("[yellow]Install with: brew install pandoc (macOS) or apt-get install pandoc (Linux)[/yellow]")
        exit(1)
    except subprocess.TimeoutExpired:
        console.print(f"[red]Pandoc conversion timed out for {file_path}[/red]")
        return None
    except subprocess.SubprocessError as e:
        console.print(f"[red]Pandoc error for {file_path}: {e}[/red]")
        return None


def check_grammar(text, language="en-US", custom_words=None):
    """Check grammar using LanguageTool API with custom dictionary support."""
    # Limit text size to avoid API limits (20KB max)
    max_chars = 20000
    if len(text) > max_chars:
        text = text[:max_chars]
        console.print(
            "[yellow]Note: Text truncated to 20KB for API limits[/yellow]"
        )

    params = {
        "text": text,
        "language": language,
        "enabledOnly": "false",
        # Disable some rules that don't work well with converted org text
        "disabledRules": "WHITESPACE_RULE,EN_QUOTES,EN_UNPAIRED_BRACKETS",
    }

    # Add custom words to ignore list
    if custom_words:
        # LanguageTool API accepts ignoreWords as comma-separated string
        params["ignoreWords"] = ",".join(custom_words)

    try:
        response = requests.post(LANGUAGETOOL_API_URL, data=params, timeout=30)
        response.raise_for_status()
        return response.json()
    except requests.RequestException as e:
        console.print(f"[red]Error calling LanguageTool API: {e}[/red]")
        return None


def display_issues(issues, file_path, text):
    """Display grammar issues in a formatted way."""
    if not issues:
        console.print(f"[green]✓ No grammar issues found[/green]")
        return

    console.print(f"[yellow]Found {len(issues)} issue(s):[/yellow]")

    for i, issue in enumerate(issues, 1):
        # Create issue panel
        issue_text = Text()
        issue_text.append(f"Issue {i}: ", style="bold yellow")
        issue_text.append(issue["message"])

        # Add context if available
        context = issue.get("context", {})
        if context:
            offset = context.get("offset", 0)
            length = context.get("length", 0)
            context_text = context.get("text", "")

            if context_text and offset is not None and length:
                before = context_text[:offset]
                problem = context_text[offset : offset + length]
                after = context_text[offset + length :]

                context_display = Text()
                context_display.append("  Context: ")
                context_display.append(before)
                context_display.append(problem, style="bold red underline")
                context_display.append(after)

        # Get suggestions
        replacements = issue.get("replacements", [])
        suggestions = [r["value"] for r in replacements[:3]]  # Top 3 suggestions

        # Create a nice display
        console.print(f"├─ Issue {i}: {issue['message']}")
        if issue.get("rule"):
            rule = issue["rule"]
            console.print(
                f"│  ├─ Type: {rule.get('category', {}).get('name', 'Unknown')} ({rule.get('id', 'N/A')})"
            )
        if "context" in issue and "text" in issue["context"]:
            console.print(f"│  ├─ Context: ...{context_display}...")
        if suggestions:
            console.print(f"│  └─ Suggestions: {', '.join(suggestions)}")

        console.print()


def create_summary_table(results):
    """Create a summary table of all checked files."""
    table = Table(title="Grammar Check Summary", box=box.ROUNDED)
    table.add_column("File", style="cyan")
    table.add_column("Issues", justify="center")
    table.add_column("Status", justify="center")

    total_issues = 0
    for file_path, issue_count in results.items():
        total_issues += issue_count
        file_name = Path(file_path).stem
        status_color = "green" if issue_count == 0 else "yellow"
        status_text = "Pass" if issue_count == 0 else "Needs Review"

        table.add_row(
            file_name,
            str(issue_count),
            f"[{status_color}]{status_text}[/{status_color}]",
        )

    # Add total row
    table.add_section()
    status_color = "green" if total_issues == 0 else "yellow"
    status_text = "All Pass" if total_issues == 0 else "Review Needed"
    table.add_row(
        "[bold]Total[/bold]",
        f"[bold]{total_issues}[/bold]",
        f"[bold {status_color}]{status_text}[/bold {status_color}]",
    )

    return table


@click.command()
@click.option(
    "--language",
    "-l",
    default="en-US",
    help="Language code for grammar checking (e.g., en-US, en-GB)",
)
@click.option(
    "--path",
    "-p",
    default=".",
    help="Path to search for .org files",
    type=click.Path(exists=True),
)
def main(language, path):
    """Check grammar in all org files using LanguageTool."""
    # Display header
    console.print(
        Panel.fit(
            "[bold cyan]LanguageTool Grammar Check[/bold cyan]\n"
            "[dim]Using LanguageTool HTTP API[/dim]",
            border_style="cyan",
        )
    )
    console.print()

    # Load custom dictionary
    custom_words = load_custom_dictionary(path)
    console.print()

    # Find org files
    org_files = find_org_files(path)
    if not org_files:
        console.print("[yellow]No .org files found in the project[/yellow]")
        return

    console.print(f"Found [cyan]{len(org_files)}[/cyan] org file(s) to check")
    console.print()

    # Check each file
    results = {}
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        console=console,
    ) as progress:
        for file_path in org_files:
            task = progress.add_task(f"Checking: {file_path.name}...", total=None)

            # Read file
            try:
                with open(file_path, encoding="utf-8") as f:
                    org_content = f.read()
            except Exception as e:
                console.print(f"[red]Error reading {file_path}: {e}[/red]")
                results[str(file_path)] = -1
                continue

            # Convert to text
            text = org_to_text(org_content, file_path)

            # Skip if conversion failed
            if text is None:
                results[str(file_path)] = -1
                continue

            # Check grammar with custom dictionary
            result = check_grammar(text, language, custom_words)

            progress.stop_task(task)
            progress.update(task, description=f"Checked: {file_path.name}")

            if result is None:
                results[str(file_path)] = -1
                continue

            # Display results for this file
            console.print(f"\n[bold]Checking:[/bold] {file_path}")
            issues = result.get("matches", [])
            display_issues(issues, file_path, text)
            results[str(file_path)] = len(issues)

            # Rate limiting
            if org_files.index(file_path) < len(org_files) - 1:
                time.sleep(RATE_LIMIT_DELAY)

    # Display summary
    console.print("\n" + "─" * 40 + "\n")
    console.print(create_summary_table(results))

    # Exit code based on results
    total_issues = sum(count for count in results.values() if count > 0)
    if total_issues > 0:
        console.print(
            f"\n[yellow]Found {total_issues} total issue(s) to review[/yellow]"
        )
        exit(1)
    else:
        console.print("\n[green]✓ All files passed grammar check![/green]")


if __name__ == "__main__":
    main()

