# Benefits Explorer

A benefits discovery and eligibility project exploring configurable rules and explainable decisions.

## Status

Initial project scaffold. Application code, tests, and deployment are not implemented yet. The architecture below is the intended implementation, not a list of completed features.

## Planned stack

- Next.js and TypeScript for the member interface
- Java and Spring Boot for REST APIs and server-side access checks
- PostgreSQL for profiles and program definitions
- JSON Schema for versioned eligibility rule contracts
- JUnit and Playwright for API and browser tests

## Planned behavior

Display a catalog of programs, rank eligible programs first, explain unmet conditions, and distinguish missing attributes from ineligible decisions. Re-check access on the server before enrollment. Use synthetic member records and generic program rules for demonstrations.

## Implementation milestones

1. Define member, program, rule, and evaluation contracts with sample JSON.
2. Build an evaluator with eligible, ineligible, and unknown results.
3. Add persistence, discovery endpoints, and enrollment authorization.
4. Build the catalog UI and cover ranking, missing attributes, and denied access with tests.

This is an independent educational project and is not affiliated with a benefits provider or identity platform.
