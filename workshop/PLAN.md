# Plan: Applitools + Specmatic — Contract-Driven Visual Testing

**Base repo:** [anandbagmar/MockedE2EDemo](https://github.com/anandbagmar/MockedE2EDemo). Work goes on a new branch, `applitools-specmatic-demo`.
**Persona:** A developer who owns both a web UI and the API behind it.
**Formats:** (a) a live demo of about 15 minutes, and (b) a self-paced walkthrough that anyone can complete alone. Both run **locally** (no deployment).
**Stack:** Playwright + TypeScript, `@applitools/eyes-playwright` fixtures (≥ 1.49, with English-based steps), the Applitools MCP server in VS Code + Copilot agent mode, and the Specmatic npm package. CI runs on GitHub Actions, and Eyes renders through Ultrafast Grid (UFG).

---

## 1. The story: why these tools belong together

| Developer pain | Specmatic | Applitools |
|---|---|---|
| UI tests are flaky because backend data is random or unavailable | Stubs the API from its OpenAPI spec, which gives deterministic data | Visual baselines stay stable because the data is stable |
| Hard to reach edge states (empty list, 15 guests, long names, 500 error, slow API) | Named **examples** in the contract, and each one becomes a UI state | One visual checkpoint per state gives full UI-state coverage cheaply |
| Is the stub lying? | **Contract tests** check that the real service still matches the spec | Baselines captured against the stub stay valid for the real service |
| An API change silently breaks the UI | **Backward-compatibility check** fails the PR | If a change slips through anyway, Eyes shows the broken UI as a diff (a second safety net) |
| Selector-heavy tests are hard to write and review | — | **English-based steps**: `eyes.run('type "Anand" in the "Your name" field')` |
| Setting up visual tests and triaging diffs is tedious | — | **Applitools MCP**: set up, add checkpoints, and review, accept or reject diffs from chat |

**One-line pitch:** *The contract defines the UI states, Specmatic makes them reproducible, and Applitools verifies how each one looks, all written in plain English and driven from the IDE.*

---

## 2. Changes to MockedE2EDemo

The webapp currently calls `randomuser.me` directly, and the agenda, the unique id and the checklist all live in the client. I'll move them behind our own API so that every screen is contract-driven.

### 2.1 New API: `guest-service` (base path `/api/v1`)

| Endpoint | Used by screen | Replaces |
|---|---|---|
| `GET /agenda?variant=original\|alternate` | Planner | Hard-coded `AGENDA_STEPS` / `ALTERNATE_AGENDA_STEPS` in `lib.ts` |
| `GET /guests?count=1..15` | GuestLookup | `fetch('https://randomuser.me/...')` |
| `POST /meetings` `{organizerName, variant, guestIds[]}` → `201 {meetingId, ...}` | GuestLookup → Next | Client-side `generateUniqueId()`. The stub makes the id deterministic, so the Summary screen is visually stable |
| `PATCH /meetings/{meetingId}/checklist` `{venueReviewed: true}` | Checklist | Local `useState` |
| `GET /meetings/{meetingId}` | Summary | Router state |

- The spec lives at `specs/guest-service.yaml`. It includes `400` responses (count out of range), `404` responses (unknown meeting) and `5xx` responses.
- The implementation lives in `guest-service/`: Express + TypeScript with in-memory storage and a seeded guest dataset. It has no downstream dependency, so it's deterministic.
- Avatars are served as local static images, not remote URLs, which removes image-loading flakiness from visual tests.

### 2.2 Webapp changes (small)
- Add `webapp/src/api.ts`, which reads its base URL from `VITE_API_BASE_URL` (default `http://localhost:9000/api/v1`, the stub).
- Screens call `api.ts`. The existing `data-testid` values stay unchanged, so the Appium and Java suites are unaffected.
- Error and loading UI already exist (the alert modal and the loader). These are the states that fault injection triggers.

### 2.3 Specmatic examples: named UI states (`specs/guest-service_examples/`)

| Example | Contract | UI state to verify visually |
|---|---|---|
| `guests_3_happy` | 200, 3 guests | Normal cards |
| `guests_15_max` | 200, 15 guests | Long scrolling grid |
| `guests_long_names` | 200, names and emails over 40 characters | Text wrapping and truncation |
| `guests_empty` | 200, `[]` | Empty state (a small UI addition) |
| `guests_400` | count=0 | Error dialog |
| `guests_500` | 500 | "Unable to load profiles" dialog |
| `guests_slow` | 200 with a delay | Loader visible |
| `agenda_alternate` | variant=alternate | Alternate styling |
| `meeting_created` | 201 with fixed `meetingId: CMP-DEMO0001` | Deterministic Summary |

Per-test state is selected through Specmatic's dynamic expectations API (`POST /_specmatic/expectations`), which a small Playwright fixture wraps as `useApiState('guests_500')`.

### 2.4 New folders

```
specs/                      OpenAPI spec + examples (the single source of truth)
specmatic.yaml              Specmatic config (stub + test sources)
guest-service/              Express + TS implementation
web-e2e/                    Playwright TS + Eyes fixtures
  playwright.config.ts      nlpOptions.enabled, UFG browsers, webServer: stub + webapp
  fixtures/api-state.ts     sets Specmatic expectations per test
  tests/
    happy-path.spec.ts      English steps, full journey
    guest-states.spec.ts    one test per example → one Eyes checkpoint each
    validation.spec.ts      invalid counts → dialog
workshop/                   Self-paced walkthrough (one markdown file per module)
  DEMO-SCRIPT.md            15-min live demo run sheet
.vscode/mcp.json            Applitools + Playwright MCP, keys via ${input:} / env (never committed)
.github/workflows/
  contract.yml              contract test + backward-compat check on PRs touching specs/ or guest-service/
  e2e-visual.yml            stub + webapp + Playwright/Eyes on UFG
```

The existing React Native, Appium and Java folders stay untouched.

---

## 3. What a test looks like (target style)

```ts
test('organizer sees an error when the guest API is down', async ({ page, eyes, apiState }) => {
  await apiState.use('guests_500');
  await page.goto('/');
  await eyes.run('type "Anand" in the "Your name" field and click the "Start Original Flow" button');
  await eyes.run('click the "Next: Native Detail" button');
  // ...
  await eyes.run('type "5" in the "Number of guests" field and click the "Load Profiles" button');
  await eyes.check('Guest API down – error dialog');
});
```

The English steps run in Chromium. UFG then renders every checkpoint on Chrome, Firefox, Safari and mobile viewports, which works around the Chromium-only limit on English steps.

---

## 4. Self-paced modules (they double as the demo outline)

| # | Module | What the developer does | Tool highlighted |
|---|---|---|---|
| 0 | Setup | Node ≥ 24.16, Java 17, clone, `npm i`, API keys. Ask Copilot to call `eyes_verify_api_key` | MCP |
| 1 | Contract first | Read the spec, run `npm run stub`, and point the webapp at the stub. The app works with no backend | Specmatic stub |
| 2 | Keep the stub honest | Run the contract test against `guest-service`, introduce a bug (e.g. drop `email`), and watch the test fail | Specmatic test |
| 3 | Tests in English | Write the happy-path journey with `eyes.run(...)`, with no selectors | English steps |
| 4 | Add Eyes via chat | Prompt Copilot to run `eyes_setup_project`, `eyes_add_checkpoints_to_test` and `eyes_setup_ufg`, then run the tests and get baselines | MCP + UFG |
| 5 | Every state, visually | Loop over the Specmatic examples to get one checkpoint per state (empty, max, long names, 500, slow) | Both together |
| 6 | Break it, twice | (a) Rename `email` → `emailAddress` in the spec, and the backward-compatibility check fails. (b) Make a CSS or copy change in the webapp, and Eyes shows a diff. Then prompt "review my last batch", accept one change and reject the other | Specmatic compat + MCP review/resolve |
| 7 | CI | Push a PR and see both workflows run. The contract gate and visual gate are independent | GitHub Actions |

**15-minute live demo path:** M1 (stub, 2 min) → M3/M4 (English test + MCP setup, 4 min) → M5 (states grid in the Eyes dashboard, 3 min) → M6 (compat failure + visual diff triaged via MCP, 5 min) → CI screenshot (1 min).

Each module ends with a git tag checkpoint (`m1-done`, `m2-done`, …) so self-paced users can catch up with `git checkout m4-done`.

---

## 5. Build order

1. Spec, examples and `specmatic.yaml`, then verify stub and contract tests from the CLI.
2. `guest-service`, passing the contract test.
3. Webapp `api.ts` refactor, running against the stub and against the real service.
4. `web-e2e`: Playwright config, the `apiState` fixture, and English-step tests (first without Eyes).
5. Add Eyes through the MCP prompts, recording the prompts for the workshop docs. Then UFG.
6. The edge-state suite and the two "break it" scenarios.
7. GitHub Actions workflows.
8. Workshop markdown, demo script and tags.

---

## 6. Decisions

1. **Runs locally only.** The demo runs only on a local machine (stub or `guest-service` + Vite webapp + Playwright). It is not deployed. The existing Pages workflow stays as it is on `main`, and this branch doesn't touch it.
2. **randomuser.me is kept** as an optional "live API" mode. `guest-service` gets `GUEST_SOURCE=seed|randomuser` (default `seed`). In `randomuser` mode, the service maps randomuser's response to our contract, so the UI and the contract stay the same. This adds a small story: the contract protects the UI from changes in a third-party API.
3. **Branch:** `applitools-specmatic-demo`, in `~/projects/specmatic/MockedE2EDemo`.
4. **Applitools keys** come from environment variables. `APPLITOOLS_API_KEY` is already set. The MCP read and resolve tools also need `APPLITOOLS_READ_KEY` and `APPLITOOLS_WRITE_KEY` (the env currently has `APPLITOOLS_VIEW_KEY`). `.vscode/mcp.json` will pass these through from the environment.
5. **Specmatic license:** an Enterprise license is available at `~/.specmatic/license.json`.

## 6a. Implementation notes (build steps 1–4)

- **Everything runs on open-source Specmatic** (npm `specmatic` 2.54.1). The Enterprise license expired on Aug 24, 2026. The stub, contract tests, dynamic expectations (including `transient` and `delay-in-milliseconds`) and `backward-compatibility-check` were all verified with the license hidden.
- **Two example folders, not one.** `specs/guest-service_examples/` holds examples with unique requests: the stub's defaults and the contract tests (15/15 pass, 91% coverage; only `500` is untested, on purpose). `specs/ui-states/` holds `guests_empty`, `guests_long_names`, `guests_500` and `guests_slow`. They share a request (`count=5`), so they are loaded per test as one-shot (`transient`) expectations and are never run against the real service.
- Both folders are generated from `guest-service/src/data/*.json` (`npm run examples:generate` in `guest-service/`).
- **Stub requests are concrete.** Open-source Specmatic has no `$match` matchers, so `POST /meetings` returns `CMP-DEMO0001`/`0002` only for organizer "Anand" and guests `g-001..003`. Any other input gets a contract-valid response that Specmatic generates, which is worth pointing out in Module 1.
- **Packages:** `guest-service/` (Express + TS, also owns the `specmatic` dependency and the `stub`/`test:contract` scripts) and `web-e2e/` (Playwright 1.63 + eyes-playwright 1.49.3) are separate npm packages, so workshop users never install the React Native/Appium root.
- **Tests run with `workers: 1`,** because every test sets its state on one shared stub. The state tests deep-link to `/#/guests` instead of walking the whole flow.
- The existing Java web suite (`runWeb` against `localhost:5173`) now needs the stub or `guest-service` running.

## 7. Open questions and risks

1. **Open-source vs. Enterprise Specmatic:** Self-paced users may not have an Enterprise license. I'll build on open-source features wherever possible and clearly mark any step that needs Enterprise, for example dynamic expectations, delays or the backward-compatibility check if those turn out to be gated.
2. **Secrets hygiene:** Your VS Code user `mcp.json` has the read and write keys in plain text. Rotate them if that file has ever been shared.
