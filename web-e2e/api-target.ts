// Which backend the webapp talks to during the tests:
//   stub    (default) Specmatic stub on :9000, deterministic data and every UI state on demand
//   service the real guest-service on :3001, no Specmatic involved
export type ApiTarget = 'stub' | 'service';

export const API_TARGET: ApiTarget = process.env.API_TARGET === 'service' ? 'service' : 'stub';

export const TARGETS = {
  stub: {
    apiBaseUrl: 'http://localhost:9000/api/v1',
    command: 'npm --prefix ../guest-service run stub',
    // A separate webapp port per target, so a dev server built for one backend is never reused for the other.
    webappUrl: 'http://localhost:5173',
  },
  service: {
    apiBaseUrl: 'http://localhost:3001/api/v1',
    command: 'npm --prefix ../guest-service start',
    webappUrl: 'http://localhost:5174',
  },
}[API_TARGET];
