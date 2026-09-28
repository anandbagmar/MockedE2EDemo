import { test, expect } from '../fixtures/api-state';
import { API_TARGET } from '../api-target';

// The full journey, written as English steps. The stub's default examples make every screen
// deterministic, down to the meeting id on the Summary screen. Against the real service
// (API_TARGET=service) the meeting id is new on every run, so Eyes reports a diff on Summary.
for (const { variant, button, meetingId } of [
  { variant: 'original', button: 'Start Original Flow', meetingId: 'CMP-DEMO0001' },
  { variant: 'alternate', button: 'Start Alternate Flow', meetingId: 'CMP-DEMO0002' },
]) {
  test(`organizer plans a meeting end to end (${variant} flow)`, async ({ page, eyes }) => {
    await page.goto('/');
    await eyes.check('Home', { fully: true });

    await eyes.run(`type "Anand" in the "Your name" field and click the "${button}" button`);
    await expect(page.getByTestId('planner.agenda.8.card')).toBeVisible();
    await eyes.check('Planner – agenda from GET /agenda', { fully: true });

    await eyes.run('click the "Next: Native Detail" button');
    await eyes.run('click the "Next: Hybrid Native Views" button');
    await eyes.run('click the "Next: Load Guest Profiles" button');
    await eyes.run('type "3" in the "Number of guests" field and click the "Load Profiles" button');
    await expect(page.getByTestId('guestLookup.profile.3.card')).toBeVisible();
    await eyes.check('Guest lookup – 3 guests', { fully: true });

    await eyes.run('click the "Next: Open Checklist" button');
    await eyes.run('click the "Mark checklist as ready" button');
    await expect(page.getByTestId('webChecklist.ready')).toBeVisible();
    await eyes.check('Checklist – confirmed', { fully: true });

    await eyes.run('click the "Complete Workflow" button');
    await expect(page.getByTestId('summary.uniqueId')).toHaveText(API_TARGET === 'stub' ? meetingId : /^CMP-[A-Z0-9]{8}$/);
    await eyes.check('Summary – meeting from GET /meetings/{meetingId}', { fully: true });
  });
}
