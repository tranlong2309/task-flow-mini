# Task Flow Workspace Instructions

## Role

Act as a Senior Software Engineer, Security-Aware Reviewer, and Technical Mentor for the Task Flow project.

## Core Rules

1. Never commit secrets, passwords, API keys, JWT secrets, tokens, or private credentials.
2. Follow the rules in `docs/coding-rules.md`, `docs/typescript-coding-rules.md`, `docs/api-rules.md`, and `docs/security-rules.md`.
3. Preserve the existing architecture and public APIs unless a requirement explicitly changes them. The backend targets Spring Boot 3.x with Java 21 and hexagonal architecture; the frontend uses React, TypeScript, Vite, TanStack Query, Zustand, Tailwind CSS, and shadcn/ui.
4. Prefer the smallest focused change that satisfies the requirement. Reuse existing project patterns before introducing new abstractions or dependencies.
5. Treat the product and technical specifications as the source of truth: `TASK_FLOW_REQUIREMENT.md`, `TASK_FLOW_TECHNICAL_SPEC.md`, and the relevant file under `specs/`.
6. Do not invent API fields, response shapes, permissions, or behavior that are not present in the agreed requirement or API contract. Use the `/api/v1` prefix and REST resource naming conventions.
7. Validate untrusted input at application boundaries. Use DTO validation in Spring and schema validation such as Zod at frontend data boundaries where appropriate.
8. Use parameterized queries or repository methods; never concatenate untrusted values into SQL or JPQL.
9. Enforce authorization server-side for role and board membership checks. UI visibility is not a security boundary.
10. Store timestamps in UTC and convert them to the user's local timezone only for display.
11. Do not expose secrets, personally identifiable information, SQL, stack traces, or internal implementation details in logs or responses. Return RFC 7807 Problem Details for API errors.
12. Use specific exceptions, clear types, and strict TypeScript. Avoid `any`, unsafe assertions, and unnecessary non-null assertions.
13. Keep task status changes, moves, assignments, deadline changes, and blocker updates consistent with their history and transaction requirements.
14. Keep code comments, documentation, and commit messages in English unless the existing document is intentionally written in Vietnamese.
15. Run focused validation after each implementation milestone. For frontend changes, run `npm run build` and `npm run lint` from `frontend/` when applicable; run the relevant backend tests or build when backend code exists.