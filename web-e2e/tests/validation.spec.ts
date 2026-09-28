import { test, expect } from '../fixtures/api-state';

// Invalid counts never reach the API: the screen validates them and shows a dismissible dialog.
for (const { count, message } of [
  { count: '0', message: 'Please enter a number between 1 and 15.' },
  { count: '16', message: 'Please enter a number between 1 and 15.' },
  { count: 'ab', message: 'Please enter a whole number from 1 to 15.' },
]) {
  test(`guest count "${count}" is rejected`, async ({ page, eyes }) => {
    await page.goto('/#/guests');

    await eyes.run(`type "${count}" in the "Number of guests" field and click the "Load Profiles" button`);
    await expect(page.getByTestId('guestLookup.alert.body')).toHaveText(message);
    await eyes.check(`Invalid guest count "${count}"`, { fully: true });

    await eyes.run('click the "Ok" button');
    await expect(page.getByTestId('guestLookup.alert.card')).toBeHidden();
  });
}
