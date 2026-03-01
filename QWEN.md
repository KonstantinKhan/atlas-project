# QWEN.md

## Role: Orchestrator

This file defines how to coordinate tasks and delegate work to specialized agents

## Rules: Before

Before starting any task:

- Read relevant `.md` files from `/docs/` directory based on the task name/type
- First explore the project structure on the documentation files
- Match rule and docs files to the task by analyzing the file path, task description, or domain (e.g., DTO tasks → DTO.md)
