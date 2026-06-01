# DITA Open Toolkit Agent Guide

## General Design

- The core toolkit code lives under `src/main/java/org/dita/dost`. Package names generally reflect pipeline roles such as readers, writers, modules, platform integration, logging, and utilities.
- The runnable toolkit layout is assembled under `src/main`. Treat `src/main/bin`, `src/main/config`, and `src/main/resources` as runtime assets that support the distribution, not as disconnected samples.
- Built-in transforms live under `src/main/plugins`. These plugin directories are also Gradle subprojects for format-specific behavior such as HTML5, HTML Help, and PDF processing. Keep format-specific changes in the owning plugin when possible; move logic into shared `org.dita.dost` code only when multiple transforms need it.
- `src/main/docsrc` is a Git submodule for the documentation source. Treat work there as documentation-repo work, not as ordinary changes to the core toolkit repository.
- `src/main/lib` and generated plugin assets are populated by the build. Prefer changing source files or build logic over hand-editing generated output.

## Working Style

- Follow the coding and test style already used in the surrounding files. Match naming, imports, assertion style, helper usage, and the level of abstraction already present nearby.
- Keep changes confined to the feature or fix being worked on. Do not mix unrelated cleanup, refactors, or formatting churn into the same change.
- Prefer existing extension points, pipeline stages, builders, and helpers over introducing new abstractions for one-off cases.
- For Java formatting, use `./gradlew spotlessApply`. Verify formatting with `./gradlew spotlessCheck`.

## Tests

- Unit tests are named `*Test` and run with `./gradlew test`. Put them under `src/test/java` in the matching package for the code under test.
- Integration tests are named `IT*` and run with `./gradlew integrationTest`. Reuse `AbstractIntegrationTest` and the fixture layout in `src/test/resources` when the change affects toolkit processing behavior rather than an isolated class.
- End-to-end tests are named `EndToEndTest*` and run with `./gradlew e2eTest`.
- XSLT behavior is covered by XSpec files under `src/test/xsl/**/*.xspec`. Add or update XSpec coverage when changing XSL templates or XPath/XSLT helper functions.
- Keep bug reproductions as small as possible. Prefer the smallest focused fixture, map, topic, or command invocation that demonstrates the problem.
- While iterating, run the narrowest relevant test task first. Before finishing, run the full relevant Gradle task for the area you changed. Use `./gradlew check` when the change spans multiple test layers.

## Issues

- Keep issue descriptions terse.
- Start with the use case that prompted the issue.
- If reporting a bug, include a test case. Make the test case as small as possible while still reproducing the issue.
- State expected behavior and actual behavior plainly.

## Pull Requests

- Keep pull request descriptions terse.
- Start with the use case that prompted the change.
- Include the test cases that validate the new or updated behavior, including any new or updated automated tests.
- Keep changes confined to the feature or fix.
- If the pull request fixes or implements an issue, include a link to the issue.
