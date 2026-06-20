import { HashRouter, Routes, Route } from 'react-router-dom';
import Home from './screens/Home';
import Planner from './screens/Planner';
import GuestLookup from './screens/GuestLookup';
import Checklist from './screens/Checklist';
import Summary from './screens/Summary';

export default function App() {
  return (
    <HashRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="app-shell" data-testid="app.shell">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/planner" element={<Planner />} />
          <Route path="/guests" element={<GuestLookup />} />
          <Route path="/checklist" element={<Checklist />} />
          <Route path="/summary" element={<Summary />} />
        </Routes>
      </div>
    </HashRouter>
  );
}
