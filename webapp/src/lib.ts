export type FlowVariant = 'original' | 'alternate';

export type FlowState = {
  variant: FlowVariant;
  name: string;
  uniqueId: string;
};

export type PlannerGuest = {
  login: { uuid: string };
  name: { title: string; first: string; last: string };
  email: string;
  phone: string;
  cell: string;
  picture: { large: string; medium: string; thumbnail: string };
  location: { city: string; country: string };
};

// Mirrors the mobile app's id format (App.tsx): CMP-<stamp><random>.
export const generateUniqueId = (): string => {
  const random = Math.random().toString(36).slice(2, 6).toUpperCase();
  const stamp = Date.now().toString(36).slice(-4).toUpperCase();
  return `CMP-${stamp}${random}`;
};

export const normalizeName = (value: string): string => {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : 'Guest';
};

// Validates the guest count exactly like the mobile app (1..15, digits only).
export const validateGuestCount = (value: string): string | null => {
  const trimmed = value.trim();
  if (!/^\d+$/.test(trimmed)) {
    return 'Please enter a whole number from 1 to 15.';
  }
  const parsed = Number(trimmed);
  if (parsed < 1 || parsed > 15) {
    return 'Please enter a number between 1 and 15.';
  }
  return null;
};

export const APP_VERSION = 'Version 1.1';

export const FEATURE_HIGHLIGHTS = [
  'Create a small event agenda',
  'Review vertically scrollable activity cards',
  'Fetch attendee profiles from a real API',
  'Validate input and show dismissible error dialogs',
  'Complete one lightweight checklist interaction',
];

export const AGENDA_STEPS = [
  { title: 'Welcome Wall', subtitle: 'Guests arrive, scan the day plan, and pick their pace.' },
  { title: 'Snack Voting', subtitle: 'The team chooses between coffee, juices, and street food.' },
  { title: 'Icebreaker Sprint', subtitle: 'Each group shares one app idea they would build in a weekend.' },
  { title: 'Mini Demos', subtitle: 'Product, QA, and automation folks each show a tiny win.' },
  { title: 'Wrap-up Notes', subtitle: 'Attendees leave with contacts and follow-up actions.' },
  { title: 'Photo Wall', subtitle: 'A slow stroll past a few snapshots from the day before everyone moves on.' },
  { title: 'Team Kudos', subtitle: 'A calm moment for a couple of shout-outs and small celebrations.' },
  { title: 'Exit Snacks', subtitle: 'A final friendly stop for refreshments, quick goodbyes, and the last scroll of the page.' },
];

export const ALTERNATE_AGENDA_STEPS = [
  { title: 'Welcome Wall', subtitle: 'Guests arrive, scan the updated day plan, and settle into a relaxed pace.' },
  { title: 'Snack Voting', subtitle: 'The team picks between coffee, juices, and neighborhood snacks.' },
  { title: 'Icebreaker Sprint', subtitle: 'Each group shares one app idea they would happily prototype over a weekend.' },
  { title: 'Mini Demos', subtitle: 'Product, QA, and automation folks each highlight one tiny win.' },
  { title: 'Wrap-up Notes', subtitle: 'Attendees leave with fresh contacts and a few follow-up actions.' },
  { title: 'Photo Wall', subtitle: 'A longer, slower pass where guests can browse snapshots from the day.' },
  { title: 'Team Kudos', subtitle: 'A final stretch for shout-outs, quick thank-yous, and a couple of small wins.' },
  { title: 'Exit Snacks', subtitle: 'One last scroll-friendly stop with refreshments and a quiet finish.' },
];

export const SUMMARY_RECAP = [
  'Multi-screen navigation across the workflow',
  'Vertical scrolling in the agenda and results',
  'User input validation with a dismissible alert dialog',
  'Live API rendering from randomuser.me',
  'A simple checklist interaction built into the flow',
];

// Default flow state when a screen is opened directly (e.g. deep link / refresh)
// without having walked the flow from Home.
export const fallbackFlow = (state: Partial<FlowState> | null | undefined): FlowState => ({
  variant: state?.variant ?? 'original',
  name: state?.name ?? 'Guest',
  uniqueId: state?.uniqueId ?? generateUniqueId(),
});
