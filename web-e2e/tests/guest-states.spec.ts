import { test, expect, UiState } from '../fixtures/api-state';

// One test per contract example, one Eyes checkpoint per UI state. The first two come from the
// stub's default examples (specs/guest-service_examples); the rest are loaded per test from
// specs/ui-states.
const states: { name: string; count: string; state?: UiState; ready: string }[] = [
  { name: '3 guests', count: '3', ready: 'guestLookup.profile.3.card' },
  { name: '15 guests (maximum)', count: '15', ready: 'guestLookup.profile.15.card' },
  { name: 'long names and emails', count: '5', state: 'guests_long_names', ready: 'guestLookup.profile.5.card' },
  { name: 'no guests', count: '5', state: 'guests_empty', ready: 'guestLookup.empty' },
  { name: 'guest API down', count: '5', state: 'guests_500', ready: 'guestLookup.alert.card' },
  { name: 'slow guest API', count: '5', state: 'guests_slow', ready: 'guestLookup.loader' },
];

for (const { name, count, state, ready } of states) {
  test(`guest lookup shows: ${name}`, async ({ page, eyes, apiState }) => {
    if (state) await apiState.use(state);
    await page.goto('/#/guests');

    await eyes.run(`type "${count}" in the "Number of guests" field and click the "Load Profiles" button`);
    await expect(page.getByTestId(ready)).toBeVisible();
    await eyes.check(`Guest lookup – ${name}`, { fully: true });
  });
}
