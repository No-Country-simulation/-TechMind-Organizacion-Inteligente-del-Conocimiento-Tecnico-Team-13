# Upgrade Plan: -TechMind-Organizacion-Inteligente-del-Conocimiento-Tecnico-Team-13 (20260808141931)

- **Generated**: 2026-08-08T09:20:23-05:00
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 17: C:\Users\USUARIO\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\17\bin (current project base JDK)
- JDK 21: C:\Users\USUARIO\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\21\bin (**available, will be used by upgrade steps**)

**Build Tools**
- Maven Wrapper: distributionUrl -> apache-maven-3.9.12 (from .mvn/wrapper/maven-wrapper.properties) — will be used (wrapper present)
- System Maven: not found on this machine (mvn not installed globally)

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260808141931
- Run tests before and after the upgrade: true
- Auto-execution mode: true (plan will be executed automatically once confirmed)

## Upgrade Goals

- Upgrade project Java runtime: 17 → 21 (Latest LTS targeted)

## Technology Stack

| Technology/Dependency    | Current | Min Compatible | Why Incompatible                               |
| ------------------------ | ------- | -------------- | ---------------------------------------------- |
| Java                     | 17      | 21             | User requested latest LTS                       |
| Spring Boot (parent)     | 3.1.5   | 3.1.5+         | SB 3.1.x supports Java 17+ (compatible with Java 21) |
| Vaadin                   | 24.3.2  | 24.x           | Expected compatible with Java 21; verify runtime |
| Maven Wrapper            | 3.9.12  | 3.9.12         | Wrapper present and sufficient for JDK 21 builds |

## Derived Upgrades

- Update the project property `<java.version>` from 17 → 21 in pom.xml.
- Add/ensure maven-compiler-plugin configured to `release` 21 (recommended maven-compiler-plugin 3.11.0+).
- Update Dockerfile base images from `eclipse-temurin:25-*` → `eclipse-temurin:21-*`.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|-----------|---------|--------|--------|--------|
| pom.xml | `<java.version>` property | 17 | upgrade | 21 | User requested Java 21 LTS |
| pom.xml | maven-compiler-plugin | not present (use parent) | add/configure | 3.11.0 (recommended) | Ensure `release`=21 and reproducible compile behavior |

### Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| N/A | Various | Uses Java 17 APIs possibly | Run compile under Java 21 and fix any compilation issues (records in Execution) | Some APIs removed or encapsulated; reflection or internal API usage may fail at runtime |

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|----------------|--------|
| Dockerfile | base image | eclipse-temurin:25-jdk / 25-jre | change to eclipse-temurin:21-jdk / 21-jre | Align runtime image with target JDK 21 |
| .mvn/wrapper/maven-wrapper.properties | distributionUrl | apache-maven-3.9.12 | keep (3.9.12) | Maven 3.9.12 is compatible with JDK21; no change required |

### CI/CD Changes

- No GitHub Actions / Azure Pipelines / Jenkinsfile detected in repository root. No CI files were found to update.

### Risks & Warnings

- Reflection/inaccessible JDK internals: code using `setAccessible(true)` on JDK-internal classes may fail at runtime on Java 21. Mitigation: run full test suite and search for reflective access; apply targeted rewrites or add `--add-opens` as temporary workaround if necessary.
- Vaadin runtime compatibility: Vaadin 24.x is typically compatible with modern JDKs but certain frontend build steps may require Node/npm toolchains — verify `vaadin-maven-plugin` build-frontend under Java 21.
- Dockerfile previously targeted Java 25; changing to 21 changes runtime behavior slightly — test container run.

## Upgrade Steps

- Step 1: Setup Environment
  - Rationale: Ensure required JDKs and wrapper are available locally for builds and verification.
  - Changes to Make: Verify JDK 21 present, ensure `./mvnw` is executable and uses wrapper (3.9.12)
  - Verification: `C:\Users\USUARIO\...\.mvn\wrapper\maven-wrapper.properties` exists; `java -version` against JDK 21 path. Expected: success.

- Step 2: Baseline (optional but performed)
  - Rationale: Capture pre-upgrade build/test status for comparison.
  - Changes to Make: Run baseline build using current default JDK (17 if available) via `./mvnw -DskipTests=false clean test` to record results.
  - Verification: `./mvnw -q -DskipTests=false clean test` (uses wrapper, JDK 17 environment). Expected: build and tests run (record pass rate).

- Step 3: Update build files to target Java 21
  - Rationale: Change project settings so compilation targets Java 21.
  - Changes to Make: Modify `pom.xml` `<java.version>` 17→21 and add maven-compiler-plugin configuration to set `<release>21` (plugin version 3.11.0). Update any module-level poms if present.
  - Verification: `./mvnw -q -DskipTests=true clean test-compile` using JDK 21 — expected: compilation success or list of compile errors to fix.

- Step 4: Update Dockerfile
  - Rationale: Match runtime Docker images to JDK 21.
  - Changes to Make: Replace `eclipse-temurin:25-jdk`/`25-jre` with `eclipse-temurin:21-jdk`/`21-jre` in Dockerfile(s)
  - Verification: Dockerfile updated; optionally build image: `docker build -t app:java21 .` (skipped in degraded environments)

- Step 5: Fix compile errors introduced by the upgrade
  - Rationale: Resolve any Java 21 incompatibilities found during compilation.
  - Changes to Make: Apply code changes (imports, APIs, reflective access fixes) or adjust plugin configuration. Each remediation is recorded in progress.md and committed.
  - Verification: `./mvnw -q -DskipTests=true clean test-compile` → success.

- Step 6: Run tests and fix failures
  - Rationale: Ensure behavior preserved; pass rate must equal baseline or be 100% per policy.
  - Changes to Make: Fix test code changes, update test dependencies if needed.
  - Verification: `./mvnw -q clean test` using JDK 21 — expected: 100% pass (or baseline-preserving if baseline run failed pre-upgrade).

- Step 7: CVE validation & fix
  - Rationale: Scan direct dependencies for known vulnerabilities and remediate as needed.
  - Changes to Make: Run dependency:list to extract direct deps, call validation tool, bump versions where CVEs are found, recompile and re-scan.
  - Verification: CVE scan returns no unfixed issues for direct dependencies.

- Step 8: Final Validation & Summary
  - Rationale: Confirm all goals met and produce artifacts (progress.md, summary.md)
  - Changes to Make: Ensure all TODOs resolved or documented, run `./mvnw -q -DskipTests=false clean test` and produce summary.
  - Verification: All tests pass; summary.md written.


---

Notes:
- GIT_AVAILABLE=false for this workspace; changes will be made to the working directory but not committed to git. Plan execution will proceed and record files changed in progress.md; user should commit changes into their VCS manually or run the mvn modifications in a git-enabled environment.
- Auto-execution: plan is set to auto-execute (user requested). Proceeding to execution phase now.
