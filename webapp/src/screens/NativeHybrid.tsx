import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, fallbackFlow } from '../lib';
import { SectionTitle } from '../components';

/**
 * Web parity for the mobile "Native Hybrid" screen (Step 2B).
 *
 * On Android/iOS this step combines React Native layout with a fixed native
 * component and does not scroll. On the web the same step is a standard web
 * component, preserving the shared workflow and data-testid scheme:
 *
 *   Planner -> NativeJourney -> NativeHybrid -> GuestLookup -> ...
 */
export default function NativeHybrid() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);
  const isAlternate = flow.variant === 'alternate';

  return (
    <main className={isAlternate ? 'alternate' : ''} data-testid="nativeHybrid.screen">
      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/guests', { state: flow })}
          data-testid="nativeHybrid.button.continue"
        >
          Next: Load Guest Profiles
        </button>
      </div>

      <span className="mode-note" data-testid="nativeHybrid.mode.hybrid.web">
        Responsive Web View
      </span>

      <SectionTitle
        eyebrow="Step 2B"
        title={
          isAlternate
            ? 'Web hybrid views before profiles'
            : 'A compact web hybrid view'
        }
        body="This screen mirrors the mobile hybrid step that combines layout with a fixed component. On the web it is a standard, non-scrolling component."
        testid="nativeHybrid.section.intro"
      />

      <div className="panel native-view" data-testid="nativeHybrid.nativeView">
        <p className="eyebrow" data-testid="nativeHybrid.nativeView.eyebrow">
          Web Hybrid View
        </p>
        <p className="section-body" data-testid="nativeHybrid.nativeView.body">
          On mobile this region is a fixed native component embedded in the
          layout. On the web it is a standard component standing in for that
          step, keeping the workflow in sync across platforms.
        </p>
      </div>
    </main>
  );
}
