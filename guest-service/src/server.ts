import { createApp } from './app.js';
import { guestSourceFromEnv } from './guests.js';

const port = Number(process.env.PORT ?? 3001);
const source = process.env.GUEST_SOURCE ?? 'seed';

createApp(guestSourceFromEnv(source)).listen(port, () => {
  console.log(`guest-service listening on http://localhost:${port}/api/v1 (GUEST_SOURCE=${source})`);
});
