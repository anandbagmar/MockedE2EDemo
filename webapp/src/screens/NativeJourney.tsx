import { useLocation, useNavigate } from 'react-router-dom';
import { FlowState, fallbackFlow } from '../lib';
import { SectionTitle } from '../components';

/**
 * Web parity for the mobile "Native Journey" detail screen (Step 2A).
 *
 * On Android/iOS this step renders a platform-native view. On the web the same
 * journey step is presented as a standard, scrollable web component so the
 * cross-platform workflow stays structurally identical:
 *
 *   Planner -> NativeJourney -> NativeHybrid -> GuestLookup -> ...
 *
 * The data-testid scheme mirrors the mobile testID scheme so the shared teswiz
 * locators line up across platforms.
 */
export default function NativeJourney() {
  const navigate = useNavigate();
  const location = useLocation();
  const flow: FlowState = fallbackFlow(location.state as Partial<FlowState>);
  const isAlternate = flow.variant === 'alternate';

  return (
    <main className={isAlternate ? 'alternate' : ''} data-testid="nativeJourney.screen">
      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate('/native-hybrid', { state: flow })}
          data-testid="nativeJourney.button.continue"
        >
          Next: Hybrid Native Views
        </button>
      </div>

      <span className="mode-note" data-testid="nativeJourney.mode.native.web">
        Responsive Web View
      </span>

      <SectionTitle
        eyebrow="Step 2A"
        title={
          isAlternate
            ? 'Web journey detail with extra breathing room'
            : 'A longer web journey screen'
        }
        body="This scrollable screen mirrors the mobile native detail step, rendered entirely with standard web components before the workflow continues."
        testid="nativeJourney.section.intro"
      />

      <div className="panel native-view" data-testid="nativeJourney.nativeView">
        <p className="eyebrow" data-testid="nativeJourney.nativeView.eyebrow">
          Web Journey View
        </p>
        <p className="section-body" data-testid="nativeJourney.nativeView.body">
          On mobile this region is a platform-native view. On the web it is a
          standard component standing in for that step, keeping the workflow in
          sync across Android, iOS, and the browser.
        </p>
      </div>
    </main>
  );
}
