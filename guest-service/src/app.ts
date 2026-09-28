import express, { NextFunction, Request, Response } from 'express';
import agenda from './data/agenda.json' with { type: 'json' };
import { GuestSource } from './guests.js';

type Variant = 'original' | 'alternate';

type Meeting = {
  meetingId: string;
  organizerName: string;
  variant: Variant;
  guestIds: string[];
  checklist: { venueReviewed: boolean };
};

const VARIANTS: Variant[] = ['original', 'alternate'];
const isVariant = (v: unknown): v is Variant => VARIANTS.includes(v as Variant);

const error = (res: Response, status: number, code: string, message: string) =>
  res.status(status).json({ code, message });

// Same format as the mobile app's ids: CMP-<stamp><random>.
const newMeetingId = (): string => {
  const random = Math.random().toString(36).slice(2, 6).toUpperCase().padEnd(4, '0');
  const stamp = Date.now().toString(36).slice(-4).toUpperCase();
  return `CMP-${stamp}${random}`;
};

// Meetings live in memory. The two demo meetings match the ids the Specmatic stub returns,
// so the contract test can read them back and the Summary screen looks the same on both.
const seedMeetings = (): Map<string, Meeting> => {
  const guestIds = ['g-001', 'g-002', 'g-003'];
  return new Map(
    (
      [
        ['CMP-DEMO0001', 'original'],
        ['CMP-DEMO0002', 'alternate'],
      ] as const
    ).map(([meetingId, variant]) => [
      meetingId,
      { meetingId, organizerName: 'Anand', variant, guestIds, checklist: { venueReviewed: false } },
    ]),
  );
};

export const createApp = (guestSource: GuestSource) => {
  const meetings = seedMeetings();
  const app = express();
  app.use(express.json());

  // The webapp runs on a different port, so allow cross-origin calls.
  app.use((req, res, next) => {
    res.set({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET,POST,PATCH,OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
    });
    if (req.method === 'OPTIONS') {
      res.sendStatus(204);
      return;
    }
    next();
  });

  const api = express.Router();

  api.get('/agenda', (req, res) => {
    const variant = req.query.variant;
    if (!isVariant(variant)) {
      error(res, 400, 'INVALID_VARIANT', 'variant must be "original" or "alternate"');
      return;
    }
    res.json({ variant, steps: agenda[variant] });
  });

  api.get('/guests', async (req, res) => {
    const raw = String(req.query.count ?? '');
    const count = Number(raw);
    if (!/^\d+$/.test(raw) || count < 1 || count > 15) {
      error(res, 400, 'INVALID_COUNT', 'count must be a whole number from 1 to 15');
      return;
    }
    try {
      res.json({ guests: await guestSource(count) });
    } catch (e) {
      console.error(e);
      error(res, 500, 'GUEST_SOURCE_UNAVAILABLE', 'The guest source did not respond');
    }
  });

  api.post('/meetings', (req, res) => {
    const { organizerName, variant, guestIds } = req.body ?? {};
    const validGuestIds =
      Array.isArray(guestIds) &&
      guestIds.length >= 1 &&
      guestIds.length <= 15 &&
      guestIds.every((id) => typeof id === 'string');
    if (typeof organizerName !== 'string' || organizerName.length === 0 || !isVariant(variant) || !validGuestIds) {
      error(res, 400, 'INVALID_MEETING', 'organizerName, variant and 1 to 15 guestIds are required');
      return;
    }
    const meeting: Meeting = {
      meetingId: newMeetingId(),
      organizerName,
      variant,
      guestIds,
      checklist: { venueReviewed: false },
    };
    meetings.set(meeting.meetingId, meeting);
    res.status(201).json(meeting);
  });

  api.get('/meetings/:meetingId', (req, res) => {
    const meeting = meetings.get(req.params.meetingId);
    if (!meeting) {
      error(res, 404, 'MEETING_NOT_FOUND', `No meeting with id ${req.params.meetingId}`);
      return;
    }
    res.json(meeting);
  });

  api.patch('/meetings/:meetingId/checklist', (req, res) => {
    const meeting = meetings.get(req.params.meetingId);
    if (!meeting) {
      error(res, 404, 'MEETING_NOT_FOUND', `No meeting with id ${req.params.meetingId}`);
      return;
    }
    if (typeof req.body?.venueReviewed !== 'boolean') {
      error(res, 400, 'INVALID_CHECKLIST', 'venueReviewed must be a boolean');
      return;
    }
    meeting.checklist = { venueReviewed: req.body.venueReviewed };
    res.json(meeting);
  });

  app.use('/api/v1', api);

  app.use((_req, res) => error(res, 404, 'NOT_FOUND', 'Unknown endpoint'));
  // Malformed JSON bodies and anything unexpected still answer in the contract's Error shape.
  app.use((err: Error & { status?: number }, _req: Request, res: Response, _next: NextFunction) => {
    const status = err.status ?? 500;
    error(res, status, status === 400 ? 'INVALID_JSON' : 'INTERNAL_ERROR', err.message);
  });

  return app;
};
