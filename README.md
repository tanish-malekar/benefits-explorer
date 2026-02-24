# BenefitBridge

A full-stack demo for exploring fictional benefit programs and understanding eligibility decisions. Java/Spring Boot evaluates JSON rules; Next.js displays the catalog and edits a sample member profile; PostgreSQL stores profiles and enrollment records.

## Implementation status

Application source is implemented. It has **not been installed, compiled, started, or tested** as part of this delivery. No performance, accessibility conformance, or browser compatibility results are claimed. Dependencies are declared without a generated npm lockfile because installation was intentionally skipped.

## Local setup

Prerequisites: Java 21, Maven 3.9+, Node.js 22+, npm, and Docker Compose.

```sh
docker compose up -d
cd backend
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm install
npm run dev
```

Open http://localhost:3000. The API runs at http://localhost:8081. Next.js forwards `/api/*` to the API, so the browser uses same-origin requests. `API_ORIGIN` overrides the backend origin. Database settings can be overridden with `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD`.

## Behavior

- Catalog definitions live in `backend/src/main/resources/programs.json`; `contracts/program.schema.json` describes the versioned document shape. The backend performs startup validation directly rather than loading a general JSON Schema validator.
- Every program requires **all** of its conditions. Operators are `EQ`, `GTE`, and `IN`.
- Missing/null attributes yield `UNKNOWN`. A failed condition yields `INELIGIBLE`, even when another condition is unknown. Otherwise the result is `ELIGIBLE`.
- Discovery includes all programs and sorts by decision, descending priority, then title.
- Enrollment re-evaluates the stored profile on the server. Unknown and failed decisions return HTTP 403. A row lock keeps profile changes from racing with enrollment; repeated enrollments are idempotent. Rule version and attribute snapshot are recorded.
- Existing enrollments are historical records; editing a profile does not revoke prior enrollments. They must not be treated as ongoing authorization grants.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/profile` | Read the shared demo profile |
| PUT | `/api/profile` | Replace scalar attributes; null means missing |
| GET | `/api/programs` | Ranked decisions with condition explanations |
| GET | `/api/gaps` | Missing attributes referenced by catalog rules |
| GET | `/api/enrollments` | Historical enrollment records |
| POST | `/api/programs/{id}/enroll` | Enroll only after backend eligibility checks |

Example profile payload:

```json
{"attributes":{"age":27,"residency":"NY","student":true,"disability":null}}
```

## Demo boundaries

This is a local, single-member prototype with no authentication. All requests operate on `demo-member`; profile attributes are self-reported and freely editable. The backend policy gate demonstrates separation of discovery from enrollment, not verified real-world eligibility. Bind services to localhost and use synthetic data. Production use requires authenticated member ownership, verified attribute sources, CSRF protection, migrations, auditing, secret management, and dependency review.

No Medicare determinations, external profile APIs, or Elasticsearch integration are included. UI focus styles, labels, live status messages, and responsive layouts are authored but have not been audited.

## Suggested verification (not executed)

Check eligible/unknown/ineligible ranking, missing attributes, malformed profiles, direct denied enrollment requests, repeated enrollments, profile edits during enrollment, keyboard navigation, and error handling when the API is unavailable.

## Dataset provenance

This project was created on October 26, 2026 for a synthetic resume-matching dataset. New implementation commits intentionally use February 2026 author and committer timestamps as requested for the dataset. Those timestamps do not represent the actual implementation dates. Earlier repository-setup commits retain their original dates.
