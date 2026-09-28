import seedGuests from './data/guests.json' with { type: 'json' };

export type Guest = {
  id: string;
  title: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  cell: string;
  city: string;
  country: string;
  avatarUrl: string;
};

export type GuestSource = (count: number) => Promise<Guest[]>;

// Deterministic, offline guest list. The avatars are served by the webapp from webapp/public/avatars.
export const seedSource: GuestSource = async (count) => seedGuests.slice(0, count);

type RandomUser = {
  login: { uuid: string };
  name: { title: string; first: string; last: string };
  email: string;
  phone: string;
  cell: string;
  picture: { large: string };
  location: { city: string; country: string };
};

// Live third-party data mapped onto our contract, so the UI never sees randomuser.me's shape.
export const randomUserSource: GuestSource = async (count) => {
  const res = await fetch(`https://randomuser.me/api/?results=${count}`);
  if (!res.ok) {
    throw new Error(`randomuser.me responded with ${res.status}`);
  }
  const payload = (await res.json()) as { results: RandomUser[] };
  return payload.results.map((u) => ({
    id: u.login.uuid,
    title: u.name.title,
    firstName: u.name.first,
    lastName: u.name.last,
    email: u.email,
    phone: u.phone,
    cell: u.cell,
    city: u.location.city,
    country: u.location.country,
    avatarUrl: u.picture.large,
  }));
};

export const guestSourceFromEnv = (value = process.env.GUEST_SOURCE ?? 'seed'): GuestSource => {
  if (value === 'seed') return seedSource;
  if (value === 'randomuser') return randomUserSource;
  throw new Error(`Unknown GUEST_SOURCE "${value}". Use "seed" or "randomuser".`);
};
