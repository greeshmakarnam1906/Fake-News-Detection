import React, { useState } from 'react';
import api from '../api/client';
import { Search, ExternalLink, ShieldCheck, AlertOctagon, AlertTriangle, HelpCircle } from 'lucide-react';

const VERDICTS = {
  DEBUNKED: { label: 'Debunked by fact-checkers', tone: 'rose', icon: AlertOctagon },
  CONTRADICTED: { label: 'Contradicted by news coverage', tone: 'rose', icon: AlertOctagon },
  DISPUTED: { label: 'Disputed', tone: 'amber', icon: AlertTriangle },
  FACT_CHECKED_TRUE: { label: 'Rated true by fact-checkers', tone: 'emerald', icon: ShieldCheck },
  SUPPORTED: { label: 'Supported by credible sources', tone: 'emerald', icon: ShieldCheck },
  COVERAGE_FOUND: { label: 'Credible coverage found', tone: 'emerald', icon: ShieldCheck },
  LIMITED_EVIDENCE: { label: 'Limited evidence', tone: 'amber', icon: HelpCircle },
  UNVERIFIED: { label: 'Unverified', tone: 'slate', icon: HelpCircle },
};

const TONES = {
  rose: 'bg-rose-500/10 border-rose-300 text-rose-800',
  amber: 'bg-amber-500/10 border-amber-300 text-amber-800',
  emerald: 'bg-emerald-500/10 border-emerald-300 text-emerald-800',
  slate: 'bg-slate-500/10 border-slate-300 text-slate-800',
};

export default function EvidencePanel({ headline, text }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const runCheck = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.post('/api/verify', { headline: headline || '', text });
      setData(response.data);
    } catch (err) {
      setError(err.customMessage || 'Fact check failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const v = data ? VERDICTS[data.verdict] || VERDICTS.UNVERIFIED : null;
  const Icon = v ? v.icon : Search;

  return (
    <section className="bg-white border border-slate-200 rounded-xl p-6 space-y-4">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h3 className="font-serif text-lg font-bold text-slate-900">Live fact check</h3>
          <p className="text-sm text-slate-600">
            Looks for fact-checker ratings and coverage by credible news outlets.
          </p>
        </div>
        <button
          type="button"
          onClick={runCheck}
          disabled={loading}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-teal-700 text-white text-sm font-semibold hover:bg-teal-800 disabled:opacity-60"
        >
          <Search className="w-4 h-4" />
          {loading ? 'Checking sources...' : data ? 'Check again' : 'Check facts'}
        </button>
      </div>

      {error && (
        <div className="p-3 rounded-lg border border-rose-300 bg-rose-50 text-rose-800 text-sm">{error}</div>
      )}

      {data && (
        <div className="space-y-4">
          <div className={`flex items-start gap-3 p-4 rounded-lg border ${TONES[v.tone]}`}>
            <Icon className="w-6 h-6 shrink-0" />
            <div>
              <p className="font-bold">{v.label}</p>
              <p className="text-sm">{data.summary}</p>
              {data.aiAssessment && (
                <p className="text-sm mt-2">
                  <strong>AI comparison ({data.aiAssessment.stance.replace(/_/g, ' ').toLowerCase()}):</strong>{' '}
                  {data.aiAssessment.explanation}
                </p>
              )}
            </div>
          </div>

          {data.factChecks?.length > 0 && (
            <div>
              <h4 className="text-sm font-bold text-slate-900 mb-2">Fact-checker ratings</h4>
              <ul className="space-y-2">
                {data.factChecks.map((f, i) => (
                  <li key={i} className="text-sm border border-slate-200 rounded-lg p-3">
                    <p className="text-slate-500">Matched claim: {f.claim}</p>
                    <p>
                      <strong>{f.rating || 'No rating'}</strong> by {f.publisher || 'unknown publisher'}
                      {f.url && (
                        <a href={f.url} target="_blank" rel="noopener noreferrer" className="ml-2 inline-flex items-center gap-1 text-teal-700 underline">
                          Read <ExternalLink className="w-3 h-3" />
                        </a>
                      )}
                    </p>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {data.newsSources?.length > 0 && (
            <div>
              <h4 className="text-sm font-bold text-slate-900 mb-2">News coverage found</h4>
              <ul className="space-y-2">
                {data.newsSources.map((n, i) => (
                  <li key={i} className="text-sm border border-slate-200 rounded-lg p-3">
                    <a href={n.url} target="_blank" rel="noopener noreferrer" className="font-semibold text-teal-700 underline">
                      {n.title}
                    </a>
                    <p className="text-slate-500">
                      {n.source}
                      {n.trusted && <span className="ml-2 text-emerald-700 font-semibold">Credible outlet</span>}
                    </p>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {data.notes?.length > 0 && (
            <ul className="text-xs text-slate-500 list-disc pl-5 space-y-1">
              {data.notes.map((note, i) => (
                <li key={i}>{note}</li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  );
}
