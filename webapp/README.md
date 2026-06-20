# Community Meeting Planner — Web

A responsive web mirror of the mobile demo workflow (`../App.tsx`), branded with
Essence of Testing. Built for web test automation: every relevant element exposes
a stable `data-testid` that mirrors the mobile app's `testID` scheme.

## Tech stack

- Vite + React + TypeScript
- React Router (`HashRouter`) — stable, deep-linkable URLs with no server rewrites
- Plain CSS with EOT brand tokens, mobile-first responsive layout

## Run locally

```bash
cd webapp
npm install
npm run dev        # http://localhost:5173
```

Point your web tests at `http://localhost:5173`.

## Build / preview

```bash
npm run build      # outputs to webapp/dist (typechecks first)
npm run preview    # serves the production build locally
```

The build uses a relative base (`base: './'`), so `dist/` works unchanged whether
served locally or from a sub-path.

## Hosting (free)

A GitHub Actions workflow (`../.github/workflows/deploy-webapp.yml`) publishes
`webapp/dist` as a GitHub Pages **project** site at
`https://anandbagmar.github.io/MockedE2EDemo/`. This is a separate deployment from
the `anandbagmar.github.io` user site and does not affect it.

To enable: in the repo settings → Pages, set Source = "GitHub Actions". Push to
`main` (changes under `webapp/`) or run the workflow manually.

Equivalent free alternatives (config-only, host-agnostic build): Netlify, Vercel,
Cloudflare Pages.

## Screens & flow

`Home → Planner → Guest lookup → Checklist → Summary`

The `name` and generated `uniqueId` are carried through router navigation state.
The Summary screen shows `Thank you <name>. Your unique id is: <id>` and logs the
id to the browser console (the web equivalent of the mobile app log).

## Locators

`data-testid` values match the mobile `testID`s where screens overlap
(`home.input.name`, `guestLookup.input.count`, `summary.thankYou`,
`summary.uniqueId`, `summary.button.restart`, …), so Appium and web suites can
share a locator vocabulary.
