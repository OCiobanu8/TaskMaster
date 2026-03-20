# TaskMaster Plan Checklist

## Goal
- Build v1 as a native Android app (Kotlin + Compose) with Firebase-first backend.
- Scope focus: collaborative project/task workflows now; calendar + gamification after MVP stability.

## Vertical Slice (Kickoff) Checklist
- [x] Create in-place architecture boundaries (`core-auth`, `core-data`, `feature-projects`, `feature-tasks`).
- [x] Define repository contracts:
  - [x] `AuthRepository`
  - [x] `ProjectRepository`
  - [x] `TaskRepository`
- [x] Implement Firebase-backed repositories for auth/projects/tasks.
- [x] Add app navigation shell with routes:
  - [x] `SignIn`
  - [x] `Projects`
  - [x] `TaskBoard`
- [x] Implement Google sign-in flow and authenticated state handling.
- [x] Implement project creation + list (`projects` + `projectMembers` write path).
- [x] Implement task creation + list + status transitions (`todo -> in_progress -> done`, plus `skipped`).
- [x] Add Firestore rules file for member-scoped access to:
  - [x] `projects`
  - [x] `projectMembers`
  - [x] `taskInstances`
- [x] Add baseline tests:
  - [x] Unit: task status transitions
  - [x] Unit: Firestore mapping functions
  - [x] Instrumentation scaffold: emulator-backed rules test
- [x] Emulator smoke check completed:
  - [x] App launches on emulator
  - [x] Sign-in flow verified

## Immediate Next Checklist (Current Sprint)
- [x] Replace deprecated Google Sign-In APIs with Credential Manager.
- [ ] Add Firestore composite indexes as needed from real query logs.
- [ ] Expand rules integration tests for full member/non-member matrix.
- [ ] Add UI instrumentation smoke tests for create project/task/status flow.
- [ ] Improve error states/toasts for auth and Firestore failures.
- [ ] Split in-place architecture into true Gradle modules (`core-*`, `feature-*`).

## Delivery Phases Checklist
- [x] Phase 0: foundation
  - [x] Base app setup and navigation shell
  - [x] Auth wiring
  - [x] Firestore slice schema/rules
  - [x] Baseline checks/tests
- [ ] Phase 1: collaborative project/task MVP hardening
  - [ ] Multi-project CRUD polish
  - [ ] Membership invites/roles UX
  - [ ] Recurrence generation for templates
- [ ] Phase 2: calendar integration
  - [ ] Google Calendar scopes and dedicated app calendar creation
  - [ ] Task-event sync + two-way reconciliation
  - [ ] Background sync/reconciliation jobs
- [ ] Phase 3: gamification + hardening
  - [ ] Points/streaks/leaderboard
  - [ ] Telemetry and error handling improvements
  - [ ] Budget/usage monitoring and pilot rollout
- [ ] Phase 4: planning assistant
  - [ ] Suggest task placement into free slots
  - [ ] Approval flow before calendar write-back

## Test Checklist
- [x] Unit: status transitions
- [x] Unit: mapper contracts
- [x] Integration scaffold: Firestore rules with emulator
- [ ] Integration: Cloud Function idempotency (future)
- [ ] UI instrumentation: sign-in/project/task full happy path
- [ ] E2E pilot scenarios (offline/edit conflict/recurrence no-duplicates)

## Assumptions
- Google Sign-In remains mandatory for v1.
- Firestore remains primary backend and source of truth.
- Initial target scale is small (family/small team), prioritize shipping and reliability.
- Calendar and planning-assistant capabilities are intentionally deferred after MVP stabilization.
