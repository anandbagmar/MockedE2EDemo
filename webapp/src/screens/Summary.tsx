import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, SUMMARY_RECAP, fallbackFlow } from '../lib';
import { getMeeting } from '../api';
import { useApi } from '../useApi';
import { PoweredByEssence } from '../components';

export default function Summary() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow = fallbackFlow(location.state as Partial<FlowState>);
  const meeting = useApi(() => getMeeting(flow.uniqueId), [flow.uniqueId]);
  const isAlternate = (meeting.status === 'ready' ? meeting.data.variant : flow.variant) === 'alternate';
  const organizerName = meeting.status === 'ready' ? meeting.data.organizerName : flow.name;

  // Web equivalent of the mobile "app log" — capturable via browser logs in tests.
  useEffect(() => {
    if (meeting.status !== 'ready') return;
    console.log(
      `[CommunityMeetingPlanner] Workflow complete for ${meeting.data.organizerName} - uniqueId=${meeting.data.meetingId}`,
    );
  }, [meeting]);

  return (
    <main className={isAlternate ? 'alternate' : ''} data-testid="summary.screen">
      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/')}
          data-testid="summary.button.restart"
        >
          Start Again
        </button>
      </div>

      <span className="mode-note">Responsive Web View</span>

      <div className="summary-card" data-testid="summary.card">
        <p className="s-eyebrow" data-testid="summary.eyebrow">
          Workflow Complete
        </p>
        <h2 data-testid="summary.title">
          {isAlternate
            ? 'Alternate demo ready on the web'
            : 'Demo ready on the web'}
        </h2>
        <p data-testid="summary.body">
          This flow mirrors the mobile app journey in a responsive web experience.
        </p>
      </div>

      {meeting.status === 'loading' ? (
        <div className="loader" data-testid="summary.loader">
          Loading your meeting…
        </div>
      ) : null}

      {meeting.status === 'error' ? (
        <div className="panel error-panel" data-testid="summary.error">
          <p className="error-title">Unable to load your meeting</p>
          <p className="section-body">{meeting.message}</p>
        </div>
      ) : null}

      {meeting.status === 'ready' ? (
        <div className="thankyou-card" data-testid="summary.thankYou">
          <p className="ty-text" data-testid="summary.thankYou.text">
            Thank you {organizerName}.
          </p>
          <p className="ty-label">Your unique id is:</p>
          <p className="ty-id" data-testid="summary.uniqueId">
            {meeting.data.meetingId}
          </p>
        </div>
      ) : null}

      {SUMMARY_RECAP.map((item, index) => (
        <div className="bullet-row" key={item} data-testid={`summary.recap.${index + 1}`}>
          <span className="dot" />
          <p data-testid={`summary.recap.${index + 1}.text`}>{item}</p>
        </div>
      ))}

      <PoweredByEssence testid="summary.brand.credit" />
    </main>
  );
}
