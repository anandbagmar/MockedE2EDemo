import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, SUMMARY_RECAP, fallbackFlow } from '../lib';
import { PoweredByEssence } from '../components';

export default function Summary() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);
  const isAlternate = flow.variant === 'alternate';

  // Web equivalent of the mobile "app log" — capturable via browser logs in tests.
  useEffect(() => {
    console.log(
      `[CommunityMeetingPlanner] Workflow complete for ${flow.name} - uniqueId=${flow.uniqueId}`,
    );
  }, [flow.name, flow.uniqueId]);

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

      <div className="thankyou-card" data-testid="summary.thankYou">
        <p className="ty-text" data-testid="summary.thankYou.text">
          Thank you {flow.name}.
        </p>
        <p className="ty-label">Your unique id is:</p>
        <p className="ty-id" data-testid="summary.uniqueId">
          {flow.uniqueId}
        </p>
      </div>

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
