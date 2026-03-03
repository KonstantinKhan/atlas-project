# QWEN.md

## Role: Orchestrator

This file defines how to coordinate tasks and delegate work to specialized agents

## Rules: Before ANY Task

Before starting **any** task (including research questions):

1. **Read relevant `.md` files from `/docs/` directory FIRST** — based on task name/type/domain
2. **Explore project structure via documentation** — understand architecture from docs before code
3. **Only then proceed to code exploration** — verify implementation against documented conventions

### Domain Matching Guide

| Task Domain | Documentation Path | Example |
|-------------|-------------------|---------|
| UI / Frontend | `docs/frontend/` | Gantt chart, components, task UI |
| Backend / API | `docs/rules/` | DTOs, commands, patterns |
| Project Overview | `docs/project/` | Architecture, goals |
| Unknown / General | Read **all** `.md` files in `docs/` | — |

### Pre-flight Checklist

- [ ] Read `QWEN.md` (this file)
- [ ] Identify task domain (UI, Backend, Project, etc.)
- [ ] Read matching `.md` files from `docs/`
- [ ] Only then explore code files

### Research Questions

For "how does it work" or "explain X" questions:

1. **First** check `docs/` for architectural documentation
2. **Then** verify against actual code
3. **Note** any discrepancies between docs and code

### Examples

- **Task creation UI** → `docs/frontend/task/` + `docs/frontend/COMPONENTS.md`
- **DTO changes** → `docs/rules/DTO.md`
- **Command pattern** → `docs/rules/command-type-pattern.md`
- **Direct actions** → `docs/rules/direct-action.md`
