# Playbook: try out Applitools + Specmatic, with and without Specmatic

This playbook shows the Community Meeting Planner webapp tested two ways:

- **Without Specmatic:** the webapp talks to the real `guest-service`. Most things work, but a few UI states can't be produced on demand, and some data changes on every run.
- **With Specmatic:** the webapp talks to a Specmatic stub generated from the API contract. Every screen is deterministic, and every UI state (empty list, server error, slow API, long names) can be requested per test.

In both cases, Applitools Eyes checks how every screen looks, and the tests are written as plain-English steps.

Everything runs locally. Nothing is deployed.

---

## 0. What's in the repo

| Path | What it is |
|---|---|
| `specs/guest-service.yaml` | The OpenAPI contract, which is the single source of truth |
| `specs/guest-service_examples/` | Default examples: the stub's responses and the contract-test cases |
| `specs/ui-states/` | Extra UI states (empty, 500, slow, long names), loaded one test at a time |
| `specmatic.yaml` | Specmatic config: stub on `:9000`, contract tests against `:3001` |
| `guest-service/` | The real API (Express + TypeScript), on `:3001` |
| `webapp/` | The React webapp; it reads its API URL from `VITE_API_BASE_URL` |
| `web-e2e/` | Playwright + Applitools Eyes tests |

**Ports**

| Port | Process |
|---|---|
| 9000 | Specmatic stub (`/api/v1`) |
| 3001 | `guest-service` (`/api/v1`) |
| 5173 | Webapp pointed at the stub |
| 5174 | Webapp pointed at `guest-service` (only when the tests run with `API_TARGET=service`) |

---

## 1. Prerequisites

- **Node.js ≥ 24.16**: `node -v`
- **Java 17 or later**, which the Specmatic npm package needs: `java -version`
- **An Applitools API key**, needed only for the Playwright + Eyes tests:
  ```bash
  export APPLITOOLS_API_KEY=<your key>
  ```
- **No Specmatic license.** Everything here works with open-source Specmatic (see [step 8](#8-optional-confirm-it-all-runs-on-open-source-specmatic)).

## 2. Install

Run from the repo root:

```bash
npm --prefix guest-service install   # also installs the specmatic CLI
npm --prefix webapp install
npm --prefix web-e2e install
```

You don't need to install the React Native/Appium dependencies at the repo root.

> npm may print `allow-scripts` warnings about `esbuild`. You can ignore them, because everything still runs.

---

## 3. Without Specmatic: the webapp against the real service

### 3.1 Click through it yourself

```bash
# Terminal 1: the real API
npm --prefix guest-service start
# → guest-service listening on http://localhost:3001/api/v1 (GUEST_SOURCE=seed)

# Terminal 2: the webapp, pointed at the real API
VITE_API_BASE_URL=http://localhost:3001/api/v1 npm --prefix webapp run dev
```

Open http://localhost:5173 and walk through the flow:

1. Type any name and click **Start Original Flow**.
2. Click through the Planner, Native Detail and Hybrid screens.
3. Enter **3** guests and click **Load Profiles**.
4. Click **Next: Open Checklist**, then **Mark checklist as ready**, then **Complete Workflow**.

What you'll notice:
- The **meeting id on the Summary screen is new every time** (for example `CMP-J9TRARSK`).
- There's no way to see the "No guests found" state, the "Unable to load profiles" error or the loading state for more than a split second. The real service doesn't produce them on demand.

**Optional:** to use live third-party data, restart terminal 1 with this command:
```bash
GUEST_SOURCE=randomuser npm --prefix guest-service start
```
Now names and avatars change on every load. The UI doesn't change at all, because `guest-service` maps randomuser.me's data onto the same contract.

Stop both terminals (Ctrl+C) before continuing.

### 3.2 Run the automated tests

```bash
npm --prefix web-e2e run test:service
```

Playwright starts `guest-service` on `:3001` and the webapp on `:5174` on its own, and stops them afterwards.

**Expected result:**
```
  4 skipped
  7 passed
Error: Test 'organizer plans a meeting end to end (original flow)' ... detected differences!
```

- **4 skipped:** the long-names, empty, 500 and slow states need the Specmatic stub. The real service can't be told to return them.
- **Eyes differences:** open the Eyes link. The only difference is the **meeting id on the Summary screen**, which is new on every run. Without Specmatic, the visual tests aren't stable.

> ⚠️ **Don't accept these differences in the Eyes dashboard.** The baselines should stay the ones produced with Specmatic, in step 4.2.

---

## 4. With Specmatic: the webapp against the contract stub

### 4.1 Click through it yourself

```bash
# Terminal 1: the Specmatic stub, generated from specs/guest-service.yaml
npm --prefix guest-service run stub
# → Mock server is running on ... http://localhost:9000/api/v1

# Terminal 2: the webapp (the stub is the default API)
npm --prefix webapp run dev
```

Open http://localhost:5173:

1. Type **Anand**, click **Start Original Flow**, and walk the flow with **3** guests.
2. The Summary screen shows **`CMP-DEMO0001`** every time. The Alternate flow shows `CMP-DEMO0002`.
3. Go back and try **15** guests. You get the 15 seeded guests from the examples.
4. Try another name or count (for example **7**). You still get a response that matches the contract, but its values are randomly generated by Specmatic, because no example matches that request. This shows that the stub always answers in the contract's shape.

**Switch the UI into another state.** Keep terminal 1 running. In a third terminal, pick a state:

```bash
# Guest API down
curl -X POST localhost:9000/_specmatic/expectations \
  -H 'Content-Type: application/json' \
  --data-binary @specs/ui-states/guests_500.json
```

Then, on the Guest lookup screen, enter **5** and click **Load Profiles**. You'll see the **"Unable to load profiles"** dialog.

Repeat with the other states:

| File | What you see after loading 5 guests |
|---|---|
| `specs/ui-states/guests_500.json` | "Unable to load profiles" dialog |
| `specs/ui-states/guests_empty.json` | "No guests found" panel |
| `specs/ui-states/guests_slow.json` | Loader visible for 3 seconds, then 5 guests |
| `specs/ui-states/guests_long_names.json` | Cards with very long names and emails |

Things to know:
- Each state is **used once**, because the examples are `transient`. The next request gets the default examples again.
- Specmatic **checks the example against the contract** before accepting it. An invalid state is rejected with an error, so a stub state can't claim something the real API would never return.

Stop both terminals (Ctrl+C) before continuing.

### 4.2 Run the automated tests

```bash
npm --prefix web-e2e test
```

Playwright starts the stub on `:9000` and the webapp on `:5173` on its own.

**Expected result:** `11 passed`, plus a link to the Eyes batch:

| Test file | What it covers |
|---|---|
| `happy-path.spec.ts` | Full journey, original and alternate, with a checkpoint on every screen |
| `guest-states.spec.ts` | One checkpoint per UI state: 3 guests, 15 guests, long names, empty, 500, slow |
| `validation.spec.ts` | Invalid counts (`0`, `16`, `ab`), shown in the dialog |

- **The first run creates the baselines.** Later runs compare against them and pass, because every response is deterministic.
- Each checkpoint is rendered by the Ultrafast Grid on Chrome, Firefox, Safari, an iPhone 15 and a Pixel 7.

Useful variants:
```bash
npm --prefix web-e2e test -- guest-states     # one file
npm --prefix web-e2e run test:headed          # watch the browser
npm --prefix web-e2e run report               # open the Playwright HTML report
```

### 4.3 Compare the two runs

| | Without Specmatic (`test:service`) | With Specmatic (`test`) |
|---|---|---|
| Needs a running backend | Yes (`guest-service`) | No |
| Summary screen | New meeting id every run → Eyes diff | `CMP-DEMO0001` every run → stable |
| Empty / 500 / slow / long-name states | Skipped (can't be produced) | One checkpoint each |
| Tests run / skipped | 7 / 4 | 11 / 0 |

---

## 5. Keep the stub honest: contract tests

A stub is only useful if it behaves like the real service. Specmatic runs the same contract as tests against `guest-service`.

```bash
# Terminal 1
npm --prefix guest-service start

# Terminal 2
npm --prefix guest-service run test:contract
```

**Expected result:** `Tests run: 15, Successes: 15, Failures: 0` and **91% API coverage**. The only untested response is `500` on `/guests`, which exists only as a UI state.

The HTML report is at `build/reports/specmatic/test/html/index.html`.

**Break it on purpose:**

1. In `guest-service/src/guests.ts`, change `seedSource` so that it drops the email:
   ```ts
   export const seedSource: GuestSource = async (count) =>
     seedGuests.slice(0, count).map(({ email, ...guest }) => guest as Guest);
   ```
2. Restart terminal 1, then run `npm --prefix guest-service run test:contract` again.
3. The `GET /guests` tests fail, reporting that the required `email` property is missing.
4. Undo the change.

---

## 6. Catch a breaking API change: backward-compatibility check

```bash
cp specs/guest-service.yaml /tmp/guest-service.old.yaml
```

In `specs/guest-service.yaml`, rename the `email` property of `Guest` to `emailAddress`. Change both the `required` list and the property name. Then run:

```bash
npx --prefix guest-service specmatic compare /tmp/guest-service.old.yaml specs/guest-service.yaml
```

**Expected result:**
```
>> RESPONSE.BODY.guests.email
    R2001: Missing required property
The newer contract is not backward compatible.
```

Put the spec back:
```bash
cp /tmp/guest-service.old.yaml specs/guest-service.yaml
```

---

## 7. Catch a UI change: visual diff

1. Make a visible change in the webapp. For example, in `webapp/src/brand.css` change `--eot-blue: #21309b;` to `--eot-blue: #2a8c9b;`.
2. Run `npm --prefix web-e2e test`.
3. Eyes reports differences on every screen that uses the blue. Open the batch link to see them highlighted.
4. Undo the change.
5. In the Eyes dashboard, reject the differences, or leave them unresolved, so the baselines stay correct.

This is the second safety net. If an API or UI change gets past the contract checks, Eyes shows what users would actually see.

---

## 8. Optional: confirm it all runs on open-source Specmatic

If a Specmatic Enterprise license is in `~/.specmatic/`, Specmatic uses it automatically. To run as a user without one:

```bash
JAVA_TOOL_OPTIONS=-Duser.home=/tmp/no-specmatic-license npm --prefix guest-service run test:contract
```

The log should say `Using Specmatic Open Source license`, and the results match step 5. The same prefix works for `run stub` and `specmatic compare`.

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `Port 9000 / 3001 / 5173 is already in use` | Something from an earlier step is still running. Stop it, or find it with `lsof -ti tcp:9000`. |
| The tests use the wrong backend | Playwright **reuses** servers that are already running on its ports. If you started a webapp on `:5173` pointed at `guest-service`, `npm test` uses it. Stop your manual servers before running the tests. |
| A state test shows the wrong state | A UI state from an earlier failed test wasn't used up. Restart the stub. |
| `Specmatic rejected UI state ...` | The example in `specs/ui-states/` no longer matches the contract. Run `npm --prefix guest-service run examples:validate`. |
| Eyes errors about the API key | Check that `APPLITOOLS_API_KEY` is exported in the same shell. |
| `specmatic` fails to start | It needs Java 17+ on your `PATH`. |
| Changed the seed data in `guest-service/src/data/` | Regenerate the examples with `npm --prefix guest-service run examples:generate`. |
