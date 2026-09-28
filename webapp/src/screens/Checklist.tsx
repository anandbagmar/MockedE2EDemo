import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, fallbackFlow } from '../lib';
import { updateChecklist } from '../api';
import { SectionTitle } from '../components';

export default function Checklist() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow = fallbackFlow(location.state as Partial<FlowState>);
  const [completed, setCompleted] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const confirmChecklist = async () => {
    setIsSaving(true);
    setError(null);
    try {
      const meeting = await updateChecklist(flow.uniqueId, true);
      setCompleted(meeting.checklist.venueReviewed);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setIsSaving(false);
    }
  };

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
          onClick={confirmChecklist}
          disabled={completed || isSaving}
          data-testid="webChecklist.button.confirm"
        >
          {completed ? 'Checklist marked as ready' : isSaving ? 'Saving…' : 'Mark checklist as ready'}
        </button>
        {error ? (
          <p className="error-text" data-testid="webChecklist.error">
            Unable to update the checklist: {error}
          </p>
        ) : null}
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
