import React, { useState } from 'react';
import { CheckSquare, Square, CheckCircle, ExternalLink, ShieldAlert } from 'lucide-react';

const FACT_CHECK_LINKS = [
  { name: 'Snopes', url: 'https://www.snopes.com' },
  { name: 'PolitiFact', url: 'https://www.politifact.com' },
  { name: 'FactCheck.org', url: 'https://www.factcheck.org' },
  { name: 'Reuters Fact Check', url: 'https://www.reuters.com/fact-check/' },
];

export default function VerificationChecklist({ actions = [] }) {
  const defaultActions = actions.length > 0 ? actions : [
    "Verify claims against primary sources and official wire reports.",
    "Check publication date and ensure quotes are not taken out of historical context.",
    "Consult verified fact-checking databases (e.g., PolitiFact, Snopes, FactCheck.org).",
    "Evaluate domain reputability and search for corroborating reporting by independent outlets."
  ];

  const [checkedItems, setCheckedItems] = useState({});

  const toggleCheck = (index) => {
    setCheckedItems((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  };

  const checkedCount = Object.values(checkedItems).filter(Boolean).length;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-base font-serif font-bold text-slate-900 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-teal-600" />
            Human Verification Checklist
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Statistical classification is never a substitute for independent fact-checking.
          </p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
          {checkedCount} / {defaultActions.length} completed
        </span>
      </div>

      {/* Interactive Verification Steps */}
      <ul className="space-y-2.5">
        {defaultActions.map((action, idx) => {
          const isChecked = !!checkedItems[idx];
          return (
            <li
              key={idx}
              onClick={() => toggleCheck(idx)}
              className={`p-3 rounded-lg border text-xs cursor-pointer flex items-start gap-3 transition-colors ${
                isChecked
                  ? 'bg-teal-50/60 border-teal-200 text-teal-900'
                  : 'bg-slate-50/50 border-slate-200 hover:bg-slate-50 text-slate-700'
              }`}
            >
              <div className="mt-0.5 shrink-0 text-teal-600">
                {isChecked ? <CheckSquare className="w-4 h-4" /> : <Square className="w-4 h-4 text-slate-400" />}
              </div>
              <span className={`leading-relaxed ${isChecked ? 'line-through text-slate-500' : ''}`}>
                {action}
              </span>
            </li>
          );
        })}
      </ul>

      {/* Reputable Fact-Checking External Portals */}
      <div className="pt-3 border-t border-slate-100">
        <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 block mb-2">
          Recommended Independent Fact-Checkers:
        </span>
        <div className="flex flex-wrap gap-2">
          {FACT_CHECK_LINKS.map((link) => (
            <a
              key={link.name}
              href={link.url}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 px-2.5 py-1 rounded border border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-medium transition-colors"
            >
              {link.name}
              <ExternalLink className="w-3 h-3 text-slate-400" />
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}
