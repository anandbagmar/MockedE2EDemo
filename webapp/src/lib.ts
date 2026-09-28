export type FlowVariant = 'original' | 'alternate';

// uniqueId is the meetingId returned by POST /meetings; it is only known after GuestLookup.
export type FlowState = {
  variant: FlowVariant;
  name: string;
  uniqueId?: string;
};

// Demo meetings that both the Specmatic stub and guest-service know about.
export const DEMO_MEETING_IDS: Record<FlowVariant, string> = {
  original: 'CMP-DEMO0001',
  alternate: 'CMP-DEMO0002',
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

export const SUMMARY_RECAP = [
  'Multi-screen navigation across the workflow',
  'Vertical scrolling in the agenda and results',
  'User input validation with a dismissible alert dialog',
  'Live API rendering from the guest-service contract',
  'A simple checklist interaction built into the flow',
];

// Default flow state when a screen is opened directly (e.g. deep link / refresh)
// without having walked the flow from Home. Falls back to the demo meeting for the variant.
export const fallbackFlow = (state: Partial<FlowState> | null | undefined): Required<FlowState> => {
  const variant = state?.variant ?? 'original';
  return {
    variant,
    name: state?.name ?? 'Guest',
    uniqueId: state?.uniqueId ?? DEMO_MEETING_IDS[variant],
  };
};
