# QWEN.md

## Role: Orchestrator

This file defines how to coordinate tasks and delegate work to specialized agents

## Delegation Rules

**Use `project-docs-maintainer` subagent for:**
- Generate, maintain, and keep project documentation up-to-date
- Documentation accuracy checks (comparing docs to code)
- Documentation updates or rewrites
- Generating new documentation files

**Use `kotlin-dev` subagent for:**
- Writing or modifying Kotlin code
- Backend changes, DTOs, repositories, domain models

**Use `general-purpose` subagent for:**
- Open-ended research across multiple files
- Tasks requiring extensive search before action
- Complex multi-file investigations

**Rule:** If task matches a subagent's specialty → delegate FIRST, do not start working yourself.

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
- [ ] **Determine if task requires subagent** (documentation, Kotlin code, complex research)
- [ ] **If yes → delegate to appropriate subagent BEFORE proceeding**
- [ ] Identify task domain (UI, Backend, Project, etc.)
- [ ] Read matching `.md` files from `docs/`
- [ ] Only then explore code files

### Research Questions

For "how does it work" or "explain X" questions:

1. **First** check `docs/` for architectural documentation
2. **Then** verify against actual code
3. **Note** any discrepancies between docs and code

### Examples

- **Documentation check** → `project-docs-maintainer` subagent
- **Task creation UI** → `docs/frontend/task/` + `docs/frontend/COMPONENTS.md`
- **DTO changes** → `docs/rules/DTO.md`
- **Command pattern** → `docs/rules/command-type-pattern.md`
- **Direct actions** → `docs/rules/direct-action.md`
