# Java AI Curriculum — Completed Exercise Pack

This report documents the Java-only exercise work for the repository and the associated curriculum.

## Scope

The work covers Java code comprehension, algorithm comprehension, debugging, performance debugging, code review, refactoring functions, refactoring patterns, and testing.

## Completed exercise areas

- Deepening Java knowledge: collections, enums, defensive copying, streams, and try-with-resources.
- Codebase comprehension: CLI → application service → domain model → storage, with parsing, priority, and synchronization utilities.
- Algorithm comprehension: task ID union, local-only and remote-only handling, conflict resolution, completed-status precedence, tag union, and synchronization actions.
- Factorial debugging: negative-input validation, base cases for 0 and 1, and recursive multiplication.
- ShoppingCart debugging: null rejection at the input boundary using `Objects.requireNonNull`.
- ImageProcessor performance: process one image at a time instead of retaining the complete input and output batches.
- FileManager review: `Path`/`Files`, try-with-resources, path validation, immutable results, buffer validation, and safer ZIP handling.
- CRM refactoring: separate preprocessing, validation, duplicate detection, transformation, persistence, and reporting responsibilities.
- Observer refactoring: introduce `WeatherObserver`, registration/removal, and notification through the abstraction.
- Testing: edge cases for parsing, dates, task merging, and conflict states.

## Verification approach

Each solution should be verified using the sequence:

`understand → hypothesize → change → test → inspect results`

For the Gradle Java project, run:

```bash
./gradlew clean test
./gradlew build
```

The repository's Java project uses Gradle, and the build/test commands above are the expected verification workflow.

## Important note

This report is a documentation deliverable for the Java exercise pack. The source implementations should be reviewed against the exact exercise files and compiled in the target Gradle project before treating the entire pack as production-ready.
