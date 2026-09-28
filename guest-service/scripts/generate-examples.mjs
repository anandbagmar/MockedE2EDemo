// Regenerates the Specmatic examples from the guest-service seed data, so the stub, the contract
// tests and the real service all agree on the same guests and agenda.
//
//   specs/guest-service_examples/  loaded by the stub at start-up and run as contract tests
//   specs/ui-states/               one-shot (transient) expectations that Playwright loads per test
//
// Usage: npm run examples:generate (from guest-service/)
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const guests = JSON.parse(fs.readFileSync(`${root}/guest-service/src/data/guests.json`));
const agenda = JSON.parse(fs.readFileSync(`${root}/guest-service/src/data/agenda.json`));
const ex = `${root}/specs/guest-service_examples`, ui = `${root}/specs/ui-states`;
fs.mkdirSync(ex, { recursive: true }); fs.mkdirSync(ui, { recursive: true });
const json = { 'Content-Type': 'application/json' };
const write = (dir, name, obj) => fs.writeFileSync(`${dir}/${name}.json`, JSON.stringify(obj, null, 2) + '\n');
const res = (status, body) => ({ status, headers: json, body });

for (const v of ['original', 'alternate'])
  write(ex, `agenda_${v}`, { 'http-request': { method: 'GET', path: `/agenda?variant=${v}` }, 'http-response': res(200, { variant: v, steps: agenda[v] }) });
write(ex, 'guests_3_happy', { 'http-request': { method: 'GET', path: '/guests?count=3' }, 'http-response': res(200, { guests: guests.slice(0, 3) }) });
write(ex, 'guests_15_max', { 'http-request': { method: 'GET', path: '/guests?count=15' }, 'http-response': res(200, { guests }) });
write(ex, 'guests_400', { 'http-request': { method: 'GET', path: '/guests?count=0' }, 'http-response': res(400, { code: 'INVALID_COUNT', message: 'count must be a whole number from 1 to 15' }) });

const ids = { original: 'CMP-DEMO0001', alternate: 'CMP-DEMO0002' };
const demoGuests = guests.slice(0, 3).map((g) => g.id);
for (const [v, id] of Object.entries(ids)) {
  const meeting = (reviewed, org = 'Anand') => ({ meetingId: id, organizerName: org, variant: v, guestIds: demoGuests, checklist: { venueReviewed: reviewed } });
  write(ex, `meeting_created_${v}`, {
    'http-request': { method: 'POST', path: '/meetings', headers: json, body: { organizerName: 'Anand', variant: v, guestIds: demoGuests } },
    'http-response': res(201, meeting(false)),
  });
  write(ex, `checklist_reviewed_${v}`, {
    'http-request': { method: 'PATCH', path: `/meetings/${id}/checklist`, headers: json, body: { venueReviewed: true } },
    'http-response': res(200, meeting(true)),
  });
  write(ex, `meeting_get_${v}`, { 'http-request': { method: 'GET', path: `/meetings/${id}` }, 'http-response': res(200, meeting(true)) });
}
write(ex, 'meeting_404', { 'http-request': { method: 'GET', path: '/meetings/CMP-NOTFOUND' }, 'http-response': res(404, { code: 'MEETING_NOT_FOUND', message: 'No meeting with id CMP-NOTFOUND' }) });

// UI states: loaded per test as one-shot (transient) dynamic expectations, never at stub start-up.
const long = guests.slice(0, 5).map((g, i) => ({
  ...g,
  firstName: ['Maximiliana-Alexandrina', 'Bartholomew-Christopher', 'Anastasiya-Konstantina', 'Venkataraghavan', 'Wolfeschlegelsteinhausen'][i],
  lastName: ['Featherstonehaugh-Montgomery', 'Vandenberghe-Oppenheimer', 'Papadopoulou-Karamanlis', 'Subramaniam-Ramachandran', 'Bergerdorff-Lindqvist'][i],
  email: `${['maximiliana.alexandrina', 'bartholomew.christopher', 'anastasiya.konstantina', 'venkataraghavan.subramaniam', 'wolfeschlegelsteinhausen'][i]}@a-very-long-company-domain.example.com`,
  city: ['Llanfairpwllgwyngyll', 'Oberschleissheim-Nord', 'Thessaloniki', 'Thiruvananthapuram', 'Garmisch-Partenkirchen'][i],
}));
const req5 = { method: 'GET', path: '/guests?count=5' };
write(ui, 'guests_long_names', { 'http-request': req5, 'http-response': res(200, { guests: long }), transient: true });
write(ui, 'guests_empty', { 'http-request': req5, 'http-response': res(200, { guests: [] }), transient: true });
write(ui, 'guests_500', { 'http-request': req5, 'http-response': res(500, { code: 'GUEST_SOURCE_UNAVAILABLE', message: 'The guest source did not respond' }), transient: true });
write(ui, 'guests_slow', { 'http-request': req5, 'http-response': res(200, { guests: guests.slice(0, 5) }), 'delay-in-milliseconds': 3000, transient: true });
write(ex, 'agenda_400', { 'http-request': { method: 'GET', path: '/agenda?variant=weekly' }, 'http-response': res(400, { code: 'INVALID_VARIANT', message: 'variant must be "original" or "alternate"' }) });
write(ex, 'meeting_400', {
  'http-request': { method: 'POST', path: '/meetings', headers: json, body: { organizerName: 'Anand', variant: 'original', guestIds: [] } },
  'http-response': res(400, { code: 'INVALID_MEETING', message: 'organizerName, variant and 1 to 15 guestIds are required' }),
});
write(ex, 'checklist_404', {
  'http-request': { method: 'PATCH', path: '/meetings/CMP-NOTFOUND/checklist', headers: json, body: { venueReviewed: true } },
  'http-response': res(404, { code: 'MEETING_NOT_FOUND', message: 'No meeting with id CMP-NOTFOUND' }),
});
