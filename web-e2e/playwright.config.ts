import { defineConfig, devices } from '@playwright/test';
import type { EyesFixture } from '@applitools/eyes-playwright/fixture';
import { API_TARGET, TARGETS } from './api-target';

export default defineConfig<EyesFixture>({
  testDir: './tests',
  // Every test sets its API state on the one shared Specmatic stub, so tests must not overlap.
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  timeout: 120_000,
  use: {
    baseURL: TARGETS.webappUrl,
    trace: 'retain-on-failure',
    eyesConfig: {
      appName: 'Community Meeting Planner',
      // Both targets compare against the same baselines; the batch name shows which backend ran.
      batch: { name: `Community Meeting Planner – contract-driven UI states (API: ${API_TARGET})` },
      // English steps (eyes.run) need Chromium with a debugging port; the fixture launches it.
      nlpOptions: { enabled: true },
      // Steps run once in Chromium; the Ultrafast Grid renders every checkpoint on these targets.
      type: 'ufg',
      browsersInfo: [
        { name: 'chrome', width: 1280, height: 800 },
        { name: 'firefox', width: 1280, height: 800 },
        { name: 'safari', width: 1280, height: 800 },
        { iosDeviceInfo: { deviceName: 'iPhone 15' } },
        { chromeEmulationInfo: { deviceName: 'Pixel 7' } },
      ],
      failTestsOnDiff: 'afterAll',
    },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      // API_TARGET=stub: Specmatic stub for specs/guest-service.yaml (../specmatic.yaml)
      // API_TARGET=service: the real guest-service
      command: TARGETS.command,
      url: `${TARGETS.apiBaseUrl}/agenda?variant=original`,
      reuseExistingServer: true,
      timeout: 120_000,
    },
    {
      command: `npm --prefix ../webapp run dev -- --port ${new URL(TARGETS.webappUrl).port} --strictPort`,
      url: TARGETS.webappUrl,
      reuseExistingServer: true,
      env: { VITE_API_BASE_URL: TARGETS.apiBaseUrl },
    },
  ],
});
