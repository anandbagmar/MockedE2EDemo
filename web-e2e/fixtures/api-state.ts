import fs from 'node:fs';
import path from 'node:path';
import { test as eyesTest } from '@applitools/eyes-playwright/fixture';
import { API_TARGET } from '../api-target';

export { expect } from '@applitools/eyes-playwright/fixture';

const UI_STATES_DIR = path.resolve(__dirname, '../../specs/ui-states');
const EXPECTATIONS_URL = 'http://localhost:9000/_specmatic/expectations';

export type UiState = 'guests_500' | 'guests_empty' | 'guests_long_names' | 'guests_slow';

export type ApiState = {
  /**
   * Makes the Specmatic stub answer the next matching request with a named UI state from
   * specs/ui-states. Specmatic validates the example against the contract before accepting it,
   * and the example is transient, so it is used once and then the stub's defaults apply again.
   * With API_TARGET=service there is no stub, so the test is skipped: the real service cannot be
   * asked for an empty list, a 500 or a slow response.
   */
  use(state: UiState): Promise<void>;
};

export const test = eyesTest.extend<{ apiState: ApiState }>({
  apiState: async ({ request }, use, testInfo) => {
    await use({
      async use(state) {
        testInfo.skip(API_TARGET === 'service', `UI state "${state}" needs the Specmatic stub`);
        const example = fs.readFileSync(path.join(UI_STATES_DIR, `${state}.json`), 'utf-8');
        const res = await request.post(EXPECTATIONS_URL, {
          headers: { 'Content-Type': 'application/json' },
          data: example,
        });
        if (!res.ok()) {
          throw new Error(`Specmatic rejected UI state "${state}" (${res.status()}): ${await res.text()}`);
        }
      },
    });
  },
});
