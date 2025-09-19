import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import './PageLayout.css';

type ActionLink = {
  label: string;
  to: string;
  emphasis?: 'primary' | 'secondary';
};

type PageLayoutProps = {
  title: string;
  description?: string;
  actions?: ActionLink[];
  children?: ReactNode;
};

export function PageLayout({ title, description, actions, children }: PageLayoutProps) {
  return (
    <section className="page-layout">
      <header className="page-layout__header">
        <div>
          <h1>{title}</h1>
          {description && <p className="page-layout__description">{description}</p>}
        </div>
        {actions && actions.length > 0 && (
          <nav className="page-layout__actions">
            {actions.map((action) => (
              <Link
                key={action.to}
                to={action.to}
                className={`page-layout__action page-layout__action--${action.emphasis ?? 'secondary'}`}
              >
                {action.label}
              </Link>
            ))}
          </nav>
        )}
      </header>
      <div className="page-layout__body">{children}</div>
    </section>
  );
}
