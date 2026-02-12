import type { Metadata } from 'next';
import './style.css';
export const metadata: Metadata = { title: 'BenefitBridge', description: 'Explore sample benefits and understand eligibility.' };
export default function Layout({ children }: { children: React.ReactNode }) {
 return <html lang="en"><body><a className="skip" href="#main">Skip to content</a>{children}</body></html>;
}
