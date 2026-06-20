import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  FlowState,
  PlannerGuest,
  fallbackFlow,
  validateGuestCount,
} from '../lib';
import { SectionTitle } from '../components';

export default function GuestLookup() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);

  const [guestCount, setGuestCount] = useState('');
  const [guests, setGuests] = useState<PlannerGuest[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [alert, setAlert] = useState<{ title: string; message: string } | null>(null);

  const loadGuests = async () => {
    const message = validateGuestCount(guestCount);
    if (message) {
      setAlert({ title: 'Invalid guest count', message });
      return;
    }
    setIsLoading(true);
    try {
      const res = await fetch(`https://randomuser.me/api/?results=${guestCount.trim()}`);
      if (!res.ok) {
        throw new Error(`Request failed with status ${res.status}`);
      }
      const payload = await res.json();
      setGuests(Array.isArray(payload?.results) ? payload.results : []);
    } catch {
      setAlert({ title: 'Unable to load profiles', message: 'Please try again in a moment.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main data-testid="guestLookup.screen">
      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={loadGuests}
          disabled={isLoading}
          data-testid="guestLookup.button.fetch"
        >
          {isLoading ? 'Loading Profiles…' : 'Load Profiles'}
        </button>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/checklist', { state: flow })}
          disabled={guests.length === 0}
          data-testid="guestLookup.button.next"
        >
          Next: Open Checklist
        </button>
      </div>

      <span className="mode-note">Responsive Web View</span>
      <SectionTitle
        eyebrow="Step 2"
        title="Fetch guest profiles"
        body="Enter how many sample attendees you want to load. The screen validates the input before calling the API."
        testid="guestLookup.section.intro"
      />

      <div className="panel" data-testid="guestLookup.panel">
        <label className="field-label" htmlFor="guest-count" data-testid="guestLookup.label.count">
          Number of guests
        </label>
        <input
          id="guest-count"
          className="text-input"
          type="text"
          inputMode="numeric"
          maxLength={2}
          placeholder="Enter 1 to 15"
          value={guestCount}
          onChange={(e) => setGuestCount(e.target.value)}
          data-testid="guestLookup.input.count"
        />
        <p className="helper-text" data-testid="guestLookup.helper.count">
          Only numeric values from 1 to 15 are accepted.
        </p>
      </div>

      {isLoading ? (
        <div className="loader" data-testid="guestLookup.loader">
          Fetching guest profiles…
        </div>
      ) : null}

      {guests.length > 0 ? (
        <div data-testid="guestLookup.results">
          <SectionTitle
            eyebrow="Results"
            title={`Loaded ${guests.length} sample guest${guests.length > 1 ? 's' : ''}`}
            body="Each card below is rendered from the live API response."
            testid="guestLookup.section.results"
          />
          <div className="profile-grid" data-testid="guestLookup.cards.list">
            {guests.map((guest, index) => (
              <div className="profile-card" key={guest.login.uuid} data-testid={`guestLookup.profile.${index + 1}.card`}>
                <img
                  className="avatar"
                  src={guest.picture.large}
                  alt=""
                  data-testid={`guestLookup.profile.${index + 1}.avatar`}
                />
                <div>
                  <p className="pname" data-testid={`guestLookup.profile.${index + 1}.name`}>
                    {guest.name.title} {guest.name.first} {guest.name.last}
                  </p>
                  <p className="pmeta" data-testid={`guestLookup.profile.${index + 1}.email`}>
                    {guest.email}
                  </p>
                  <p className="pmeta" data-testid={`guestLookup.profile.${index + 1}.location`}>
                    {guest.location.city}, {guest.location.country}
                  </p>
                  <p className="pmeta" data-testid={`guestLookup.profile.${index + 1}.phone`}>
                    Phone: {guest.phone} | Cell: {guest.cell}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {alert ? (
        <div
          className="modal-backdrop"
          data-testid="guestLookup.alert.backdrop"
          onClick={() => setAlert(null)}
        >
          <div className="modal-card" data-testid="guestLookup.alert.card" onClick={(e) => e.stopPropagation()}>
            <h3 data-testid="guestLookup.alert.title">{alert.title}</h3>
            <p data-testid="guestLookup.alert.body">{alert.message}</p>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setAlert(null)}
              data-testid="guestLookup.alert.ok"
            >
              Ok
            </button>
          </div>
        </div>
      ) : null}
    </main>
  );
}
