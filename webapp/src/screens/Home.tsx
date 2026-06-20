import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  APP_VERSION,
  FEATURE_HIGHLIGHTS,
  FlowVariant,
  generateUniqueId,
  normalizeName,
} from '../lib';
import { BrandBar, PoweredByEssence, SectionTitle, eotLogo } from '../components';

export default function Home() {
  const navigate = useNavigate();
  const [name, setName] = useState('');

  const startFlow = (variant: FlowVariant) => {
    navigate('/planner', {
      state: {
        variant,
        name: normalizeName(name),
        uniqueId: generateUniqueId(),
      },
    });
  };

  return (
    <main data-testid="home.screen">
      <BrandBar />

      <section className="hero" data-testid="home.hero">
        <div className="hero-badge" data-testid="home.hero.badge">
          <img src={eotLogo} alt="Essence of Testing logo" />
        </div>
        <span className="eyebrow" data-testid="home.hero.eyebrow">
          Cross-platform Demo
        </span>
        <h1 data-testid="home.hero.title">App Automation Playground</h1>
        <p data-testid="home.hero.body">
          A small responsive workflow with scrolling, validation, live API data,
          and a checklist step.
        </p>
      </section>

      <div className="panel" data-testid="home.panel.name">
        <label className="field-label" htmlFor="home-name" data-testid="home.label.name">
          Your name
        </label>
        <input
          id="home-name"
          className="text-input"
          type="text"
          autoComplete="name"
          placeholder="Enter your name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          data-testid="home.input.name"
        />
        <p className="helper-text" data-testid="home.helper.name">
          Used to personalize your summary at the end of the workflow.
        </p>
      </div>

      <div className="panel" data-testid="home.panel.workflows">
        <span className="mode-note">Responsive Web View</span>
        <SectionTitle
          eyebrow="What It Shows"
          title="Choose a demo workflow"
          body="Both flows share the same journey; the alternate flow adds subtle visual differences for screenshot and visual testing."
          testid="home.section.workflows"
        />
        {FEATURE_HIGHLIGHTS.map((item, index) => (
          <div className="bullet-row" key={item} data-testid={`home.highlight.${index + 1}`}>
            <span className="dot" />
            <p data-testid={`home.highlight.${index + 1}.text`}>{item}</p>
          </div>
        ))}
      </div>

      <div className="top-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => startFlow('original')}
          data-testid="home.button.flow.original"
        >
          Start Original Flow
        </button>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => startFlow('alternate')}
          data-testid="home.button.flow.alternate"
        >
          Start Alternate Flow
        </button>
      </div>

      <p className="version-text" data-testid="home.version">
        {APP_VERSION}
      </p>
      <PoweredByEssence testid="home.brand.credit" />
    </main>
  );
}
