import eotLogo from './assets/eot-logo.jpg';

export function BrandBar() {
  return (
    <header className="brandbar" data-testid="brand.bar">
      <img className="logo" src={eotLogo} alt="Essence of Testing logo" data-testid="brand.bar.logo" />
      <div>
        <div className="title" data-testid="brand.bar.title">
          Community Meeting Planner
        </div>
        <div className="subtitle">Cross-platform automation demo</div>
      </div>
    </header>
  );
}

export function PoweredByEssence({ testid }: { testid?: string }) {
  return (
    <div className="brand-credit" data-testid={testid ?? 'brand.credit'}>
      <img src={eotLogo} alt="Essence of Testing logo" data-testid="brand.credit.logo" />
      <span data-testid="brand.credit.text">Powered by Essence of Testing</span>
    </div>
  );
}

export function SectionTitle({
  eyebrow,
  title,
  body,
  testid,
}: {
  eyebrow: string;
  title: string;
  body: string;
  testid: string;
}) {
  return (
    <div data-testid={testid}>
      <p className="eyebrow" data-testid={`${testid}.eyebrow`}>
        {eyebrow}
      </p>
      <h2 className="section-title" data-testid={`${testid}.title`}>
        {title}
      </h2>
      <p className="section-body" data-testid={`${testid}.body`}>
        {body}
      </p>
    </div>
  );
}

export { eotLogo };
