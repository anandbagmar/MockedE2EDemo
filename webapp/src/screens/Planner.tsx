import { useLocation, useNavigate } from 'react-router-dom';
import {
  AGENDA_STEPS,
  ALTERNATE_AGENDA_STEPS,
  FlowState,
  fallbackFlow,
} from '../lib';
import { SectionTitle } from '../components';

export default function Planner() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);
  const isAlternate = flow.variant === 'alternate';
  const steps = isAlternate ? ALTERNATE_AGENDA_STEPS : AGENDA_STEPS;

  return (
    <main className={isAlternate ? 'alternate' : ''} data-testid="planner.screen">
      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/native-journey', { state: flow })}
          data-testid="planner.button.next"
        >
          Next: Native Detail
        </button>
      </div>

      <span className="mode-note">Responsive Web View</span>
      <SectionTitle
        eyebrow="Step 1"
        title={isAlternate ? 'Build the event atmosphere' : 'Build the event mood'}
        body={
          isAlternate
            ? 'This version keeps the same structure, with subtle layout and typography changes for visual comparison.'
            : 'This screen is intentionally scrollable so the demo includes a natural vertical scroll interaction.'
        }
        testid="planner.section.intro"
      />

      {steps.map((step, index) => (
        <div className="agenda-card" key={step.title} data-testid={`planner.agenda.${index + 1}.card`}>
          <p className="idx" data-testid={`planner.agenda.${index + 1}.index`}>
            0{index + 1}
          </p>
          <h3 data-testid={`planner.agenda.${index + 1}.title`}>{step.title}</h3>
          <p data-testid={`planner.agenda.${index + 1}.body`}>{step.subtitle}</p>
        </div>
      ))}

      <div className="tip-banner" data-testid="planner.tipBanner">
        <p className="tip-title" data-testid="planner.tipBanner.title">
          Demo Tip
        </p>
        <p data-testid="planner.tipBanner.text">
          Scroll through the agenda, then move forward to fetch attendee profiles
          from the API.
        </p>
      </div>
    </main>
  );
}
