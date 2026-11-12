# Project review — 2026-11-12

## Scope

Reviewed all 35 main Java source files, the context-load test, Maven dependencies and wrapper configuration, application YAML, Docker Compose, environment template, and repository housekeeping files. This review documents the existing implementation; application code and infrastructure configuration were not changed.

## Keycloak verdict

**Keycloak has been added at the infrastructure and JWT resource-server levels.** Evidence:

- `docker-compose.yml` declares Keycloak 25.0.0 and PostgreSQL 16, with a database health dependency and persistent database volume.
- `.env.example` supplies the Keycloak database and initial administrator settings.
- `application.yaml` points to issuer `http://localhost:8080/realms/iam-realm` and the realm's signing-key endpoint.
- `SecurityConfig` enables stateless OAuth2 resource-server JWT security and method authorization.
- `JwtAuthenticationConverter` reads `realm_access.roles`, uppercases each role, and prefixes it with `ROLE_`.
- Controllers require `ADMIN` for writes and `ADMIN` or `USER` for reads.

The repository does not prove that the realm or a client has been created in a running Keycloak instance. There is no realm import/export, Admin API integration, provisioning service, or mapping from a JWT subject to a local user. Database role assignments are not token role assignments. Local account flags do not block an otherwise valid token.

## How a request is processed

1. The client obtains a Keycloak access token outside this backend.
2. Spring Security validates the bearer JWT using the configured issuer and signing keys.
3. The custom converter builds authorities from scopes and realm roles.
4. Controller method rules determine whether the caller may access the operation.
5. Request DTO constraints validate input.
6. Transactional services perform resource checks, mapping, and persistence through JPA repositories.
7. Response mappers return nested public DTOs; local passwords are omitted from responses.
8. JPA auditing sets creation/update timestamps. Business and validation exceptions receive structured responses.

## Module observations

| Module | What it does / limitations |
| --- | --- |
| Bootstrap/configuration | Standard Spring application, JPA auditing, OpenAPI global bearer scheme. |
| Controllers | 21 mapped operations across users, roles, and permissions; no login or registration endpoint. |
| DTOs | Six request/response types. User PUT reuses creation validation despite updating only profile names and optional roles. |
| Entities | Three entities, UUID base class, two many-to-many join tables; no identity-provider subject field. |
| Repositories | UUID-based CRUD with username/email/name lookups. |
| User service | Creation uniqueness checks, profile updates, deletion, role assignment, local status toggles. Email verification and login timestamps have no exposed trigger. Password hashing is a TODO. |
| Role service | CRUD and permission assignment. Creation verifies all supplied permission IDs; update does not. Assigned-role deletion relies on database constraints. |
| Permission service | CRUD and name uniqueness checks. Assigned-permission deletion safety remains a TODO. |
| Mappers | Manual mapping, nested role/permission responses. User creation copies the raw password. |
| Exceptions | Business 400/404/409 and field validation handling. Catch-all handler needs narrower handling for security and malformed-request exceptions. |
| Security | JWT, stateless sessions, public docs and health, wildcard CORS. No explicit audience validator or local-account check. |
| Infrastructure | Keycloak/PostgreSQL only; no backend container, production TLS configuration, or realm bootstrap. |
| Tests | One context-load test; no API or authorization behavior coverage. |

## Verification evidence

| Check | Observed result |
| --- | --- |
| Git checkout | Cloned over SSH; `main` tracks `origin/main`; clean before documentation work. |
| Java | Java 17.0.18 available. |
| Docker | CLI 29.9.2 available; engine pipe absent. Same engine-unavailable outcome outside restricted execution. No containers started. |
| Initial restricted Maven attempt | Wrapper could not start in the restricted environment. Retried with approved access. |
| Approved `iam-backend\\mvnw.cmd test` | Compiled 35 main source files and one test source. One test executed, zero assertion failures, one context initialization error; Maven exited 1. |
| Confirmed test root cause | `java.lang.IllegalStateException: Cannot load driver class: org.h2.Driver`. YAML configures H2; the POM does not provide its driver. |
| End-to-end Keycloak authentication | Not executed: infrastructure unavailable and backend context fails. |
| Swagger UI / API screenshots | Not captured: no running application UI verified. No simulated screenshots were substituted. |

Generated Maven reports are under ignored directory `iam-backend/target/surefire-reports/`. Compilation success does not establish OpenAPI runtime compatibility or successful JWT authorization.

## Suggested implementation order

1. Resolve the application database mismatch and get the context-load test passing.
2. Decide how local user records relate to Keycloak identities; remove local credential storage or implement password hashing before accepting credentials.
3. Add reproducible realm/client bootstrap and tests for missing tokens, valid USER/ADMIN tokens, forbidden writes, issuer/audience rejection, and local account status policy.
4. Fix update relationship validation, assigned-resource deletion, user update semantics, and security exception handling.
5. Verify Swagger runtime compatibility and authenticated API workflows.
6. Capture sanitized Keycloak and Swagger screenshots from the working services.
7. Add deployment profiles, migrations, restricted CORS/networking, production authentication/TLS settings, and automated checks.

See the root [README](../README.md) for setup, API routes, architecture, and the detailed findings table.
