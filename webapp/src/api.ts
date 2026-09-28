// Typed client for the guest-service contract (specs/guest-service.yaml).
// Defaults to the Specmatic stub; set VITE_API_BASE_URL=http://localhost:3001/api/v1 for the real service.
import { FlowVariant } from './lib';

export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:9000/api/v1';

export type AgendaStep = { title: string; subtitle: string };

export type Agenda = { variant: FlowVariant; steps: AgendaStep[] };

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

export type Meeting = {
  meetingId: string;
  organizerName: string;
  variant: FlowVariant;
  guestIds: string[];
  checklist: { venueReviewed: boolean };
};

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: init?.body ? { 'Content-Type': 'application/json' } : undefined,
  });
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(res.status, body?.code ?? 'HTTP_ERROR', body?.message ?? `Request failed with status ${res.status}`);
  }
  return body as T;
}

export const getAgenda = (variant: FlowVariant) => request<Agenda>(`/agenda?variant=${variant}`);

export const listGuests = async (count: number) => (await request<{ guests: Guest[] }>(`/guests?count=${count}`)).guests;

export const createMeeting = (meeting: { organizerName: string; variant: FlowVariant; guestIds: string[] }) =>
  request<Meeting>('/meetings', { method: 'POST', body: JSON.stringify(meeting) });

export const getMeeting = (meetingId: string) => request<Meeting>(`/meetings/${encodeURIComponent(meetingId)}`);

export const updateChecklist = (meetingId: string, venueReviewed: boolean) =>
  request<Meeting>(`/meetings/${encodeURIComponent(meetingId)}/checklist`, {
    method: 'PATCH',
    body: JSON.stringify({ venueReviewed }),
  });
