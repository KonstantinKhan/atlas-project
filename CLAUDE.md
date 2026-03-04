# CLAUDE.md — Atlas Project

## Primary Rule: docs/ Is the Project Map

Read documentation before reading code.
Documentation is authoritative. If it contradicts code — that is a docs bug; flag it.

---

## Documentation Structure (3 Levels)

Every domain follows the same layout:

```
docs/<domain>/
├── OVERVIEW.md      # Level 1: what the module does, boundaries, tech stack
├── INDEX.md         # Level 2: what files exist and what each one does
└── details/         # Level 3: details of specific files / components
    └── *.md
```

Domains: `frontend/`, `backend/`, `project/`, `rules/`
Root: `docs/ROAD_MAP.md` — product strategy and stages

---

## Navigation Protocol

### Step 1: Identify the task domain
- UI / component / hook / store → `frontend/`
- API / endpoint / DTO / domain model → `backend/`
- Planning / status / architecture decision → `project/`
- Coding conventions → `rules/`

### Step 2: Read the domain OVERVIEW.md
Learn: what is in scope, what is out of scope, technologies used.
One read. Stop if the task is already clear.

### Step 3: Read the domain INDEX.md
Use it as a table of contents: find the file / component / module you need.
Stop when you have the path to the relevant element.

### Step 4: Read only the matching details/*.md
Open only the file that INDEX pointed to.
Do not read other details/ files in the same domain.

### Step 5: Open a source file only to make an edit
By this point you know what to change and how.
Open exactly the one file that needs to be modified.

---

## Coding Rules (read before editing)

Before changes in specific areas:
- Adding a command type (frontend) → `docs/rules/command-type-pattern.md`
- Writing a Kotlin DTO → `docs/rules/DTO.md`
- Simple targeted fix → `docs/rules/direct-action.md`

---

## Forbidden

- DO NOT run `find`, `ls -R`, `grep -r` to discover structure — use INDEX.md instead
- DO NOT open multiple source files to "understand a pattern" — read the details/*.md instead
- DO NOT read files from another domain (no backend files for a frontend task)
- DO NOT open any source file before completing steps 1–4

---

## When Documentation Is Insufficient

Direct code search is allowed only in these cases:

| Situation | Action |
|-----------|--------|
| Feature is absent from docs (undocumented) | Read the relevant module source file |
| INDEX points to a file that does not exist | Report the docs error, search by intent |
| Docs contradict each other | Open code to resolve, then update docs |
