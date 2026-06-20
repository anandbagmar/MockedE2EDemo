import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, fallbackFlow } from '../lib';
import { SectionTitle } from '../components';

export default function Checklist() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);
  const [completed, setCompleted] = useState(false);

  return (
    <main data-testid="webChecklist.screen">
      <div data-testid="webChecklist.top">
        <span className="mode-note">Responsive Web View</span>
        <SectionTitle
          eyebrow="Step 3"
          title="Finish one checklist task"
          body="Confirm the venue checklist was reviewed to unlock the final screen."
          testid="webChecklist.section.intro"
        />
        {completed ? (
          <span className="ready-badge" data-testid="webChecklist.ready">
            Checklist confirmed
          </span>
        ) : null}
      </div>

      <div className="panel">
        <p className="section-body" style={{ marginBottom: 16 }}>
          Tap the button below to confirm that the venue checklist was reviewed.
        </p>
        <button
          type="button"
          className="btn btn-success"
          onClick={() => setCompleted(true)}
          disabled={completed}
          data-testid="webChecklist.button.confirm"
        >
          {completed ? 'Checklist marked as ready' : 'Mark checklist as ready'}
        </button>
      </div>

      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/summary', { state: flow })}
          disabled={!completed}
          data-testid="webChecklist.button.continue"
        >
          {completed ? 'Complete Workflow' : 'Complete the checklist first'}
        </button>
      </div>
    </main>
  );
}
