# TaskMaster Plan: Multi-Project Task + Calendar App (Android, Family-to-Small-Team Scale)

## Summary
Build v1 as a native Android app (Kotlin + Compose) with a Firebase-first backend, focused on collaborative project backlogs, manual scheduling, and two-way Google Calendar sync via a dedicated app calendar per user.

Ship v1 as "MVP + gamification basics" (points, streaks, leaderboard), then add a planning-assistant phase that proposes backlog tasks into free time slots.

## Implementation Changes
- Architecture and stack:
  - Android: Kotlin, Jetpack Compose, MVVM + Repository pattern, WorkManager for background sync/reconciliation.
  - Backend: Firebase Auth (Google Sign-In only), Firestore (EU region), Cloud Functions for sync/webhook logic.
  - Portability guardrails: domain interfaces (`TaskRepository`, `ProjectRepository`, `CalendarSyncService`, `GamificationService`) so future backend migration is isolated.
- Core domain model (v1):
  - `User`, `Project`, `ProjectMember` (project-level roles), `TaskTemplate` (for recurrence), `TaskInstance`, `Assignment`, `CalendarLink`, `ActivityLog`, `ScoreEntry`, `Streak`.
  - Support multiple user-defined projects per user; each project has independent members/roles.
- Core product behaviors (v1):
  - Create/join projects, manage backlog, assign tasks, task states (`todo`, `in_progress`, `done`, `skipped`), priorities, due date/time window.
  - Manual scheduling first: user picks time blocks; app syncs tasks as calendar events.
  - Recurrence: simple daily/weekly/monthly templates generating task instances.
  - Calendar scope: read schedule context (busy/free) from selected calendars; write app tasks to dedicated app-managed calendar; two-way sync for app-owned events.
  - Gamification: points on completion, daily streak tracking, team leaderboard per project.
- Security and reliability:
  - Firestore Security Rules enforce project-level access control.
  - Sync idempotency keys for event/task updates to avoid duplicates.
  - Conflict policy: last-write-wins with audit log and "conflict detected" marker for manual review in edge cases.
  - Budget controls: Firebase budget alerts and usage dashboards from day one.

## Public Interfaces and APIs
- App-to-backend contracts:
  - Auth: Google OAuth sign-in token exchange through Firebase Auth.
  - Firestore collections: `projects`, `projectMembers`, `taskTemplates`, `taskInstances`, `calendarLinks`, `scores`, `streaks`, `activityLogs`.
  - Cloud Functions endpoints/triggers:
    - `syncTaskToCalendar(taskInstanceId)`
    - `reconcileCalendarEventChange(calendarEventId)`
    - `generateRecurringTasks(projectId, date)`
    - `recomputeLeaderboard(projectId, period)`
- Client feature modules:
  - `feature-projects`, `feature-tasks`, `feature-calendar`, `feature-gamification`, `core-sync`, `core-auth`, `core-data`.

## Delivery Phases
- Phase 0 (Week 1): foundation
  - Project/module setup, auth wiring, Firestore schema + rules, base navigation, CI checks.
- Phase 1 (Weeks 2-3): collaborative project/task MVP
  - Multi-project CRUD, membership/invites, backlog + assignments, task lifecycle, recurrence generation.
- Phase 2 (Weeks 4-5): calendar integration
  - Google Calendar auth scopes, dedicated app calendar creation, task-event sync, two-way reconciliation, background jobs.
- Phase 3 (Week 6): gamification basics + hardening
  - Points/streaks/leaderboard, telemetry, error handling, budget/usage monitoring, pilot rollout to first users.
- Phase 4 (Future iteration): planning assistant
  - Daily planning mode that proposes task placements using free slots + priority + due urgency + user constraints; user approves before write-back.

## Test Plan
- Unit tests:
  - Recurrence generation, priority sorting, points/streak calculations, sync conflict resolution, repository contracts.
- Integration tests:
  - Firestore rules (member vs non-member access), Cloud Function idempotency, task/calendar reconciliation flows.
- Instrumentation/UI tests:
  - Sign-in, project creation/join, task scheduling, completion flow, leaderboard updates.
- End-to-end pilot scenarios:
  - Multi-user shared project workflow.
  - External edit to app-managed calendar event and correct app reconciliation.
  - Offline task edits and later sync convergence.
  - Recurring tasks generated and synced without duplicates.

## Assumptions and Defaults
- Initial scale is under 50 users; optimize for speed/reliability over heavy infra complexity.
- Google Sign-In is mandatory in v1.
- Firestore hosted in EU region.
- Calendar integration uses dedicated app-managed calendars (not arbitrary edits across all calendars).
- "Points + steaks + team leaderboard" interpreted as points + streaks + per-project leaderboard.
- Planning assistant is deferred to post-v1, but data model and sync logs are designed to support it.
