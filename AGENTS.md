# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Some experience coding and using git in a 2-man project
* IDE and level of expertise: IntelliJ, not very good with intelliJ (Used VScode previously)

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Post-code-update UI validation:

After every update to source code or other user-visible application behavior:

1. Review `test/ui-test-plan.md` and update it when the change adds, removes, or changes a user-visible command, input, or expected console output. Keep each affected test case's aim, inputs, and expected output accurate.
2. Invoke the project-specific `test-ui` skill after the code update by running its test session from the repository root:

   ```powershell
   pwsh -NoProfile -File .codex/skills/test-ui/scripts/run_ui_tests.ps1
   ```

   Preserve the console input/output record in the report. If a test fails, stop immediately, report the actual and expected outputs, and do not continue to later test cases.

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
