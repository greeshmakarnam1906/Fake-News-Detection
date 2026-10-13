import React from 'react';
import ConfidenceIndicator from './ConfidenceIndicator';
import VerificationChecklist from './VerificationChecklist';
import { ShieldCheck, AlertTriangle, AlertOctagon, HelpCircle, ArrowRight, RotateCcw, Cpu, FileText } from 'lucide-react';
import EvidencePanel from './EvidencePanel';

export default function PredictionResult({ result, onAnalyzeAnother }) {
  if (!result) return null;

  const {
    label,
    score,
    explanation,
    topFakeFeatures = [],
    topGenuineFeatures = [],
    limitations = [],
    verificationActions = [],
    modelVersion,
    characterCount,
    wordCount,
  } = result;

  let badgeStyle = {
    bg: 'bg-amber-500/10 border-amber-300 text-amber-800',
    icon: <AlertTriangle className="w-6 h-6 text-amber-600" />,
    titleColor: 'text-amber-800',
  };

  if (label === 'Likely Genuine') {
    badgeStyle = {
      bg: 'bg-emerald-500/10 border-emerald-300 text-emerald-800',
      icon: <ShieldCheck className="w-6 h-6 text-emerald-600" />,
      titleColor: 'text-emerald-800',
    };
  } else if (label === 'Likely Fake') {
    badgeStyle = {
      bg: 'bg-rose-500/10 border-rose-300 text-rose-800',
      icon: <AlertOctagon className="w-6 h-6 text-rose-600" />,
      titleColor: 'text-rose-800',
    };
  }

  return (
    <div className="space-y-6" id="prediction-result-panel">
      {/* Primary Classification Card */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        {/* Banner with Verdict */}
        <div className={`p-6 border-b flex flex-wrap items-center justify-between gap-4 ${badgeStyle.bg}`}>
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-xl bg-white shadow-sm border border-slate-200/50">
              {badgeStyle.icon}
            </div>
            <div>
              <span className="text-xs uppercase tracking-wider font-semibold text-slate-500 block">
                Model Classification Outcome
              </span>
              <h2 className={`text-2xl font-serif font-bold ${badgeStyle.titleColor}`}>
                {label}
              </h2>
            </div>
          </div>

          <button
            onClick={onAnalyzeAnother}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-lg shadow-sm transition-colors"
            id="btn-analyze-another-top"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Analyze Another Item
          </button>
        </div>

        {/* Details & Explanation */}
        <div className="p-6 space-y-6">
          {/* Explanation Text */}
          <div>
            <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Diagnostic Summary
            </h3>
            <p className="text-sm text-slate-800 leading-relaxed font-sans">
              {explanation}
            </p>
          </div>

          {/* Model Score Gauge */}
          <ConfidenceIndicator score={score} label={label} />

          {/* Explainability Breakdown: Top Features with genuine model coefficients */}
          <div>
            <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2 flex items-center gap-1.5">
              <Cpu className="w-4 h-4 text-teal-600" />
              Linguistic Feature Explainability (Top Active Vocabulary Weights)
            </h3>
            <p className="text-xs text-slate-500 mb-3">
              Features found in the submitted text that contributed most strongly toward class scores based on trained model weights:
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Words pushing toward Fake */}
              <div className="p-4 rounded-lg bg-rose-50/50 border border-rose-200">
                <span className="text-xs font-bold text-rose-900 block mb-2">
                  Top Terms Pushing Toward Fabricated / Fake:
                </span>
                {topFakeFeatures.length > 0 ? (
                  <ul className="space-y-1.5">
                    {topFakeFeatures.map((feat, idx) => (
                      <li key={idx} className="flex justify-between items-center text-xs">
                        <span className="font-mono bg-white px-2 py-0.5 rounded border border-rose-200 text-rose-900">
                          {feat.token}
                        </span>
                        <span className="font-mono text-rose-700 text-[11px]">
                          +{feat.contribution.toFixed(4)}
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-slate-400 italic">No strong fabricated lexical markers detected.</p>
                )}
              </div>

              {/* Words pushing toward Genuine */}
              <div className="p-4 rounded-lg bg-emerald-50/50 border border-emerald-200">
                <span className="text-xs font-bold text-emerald-900 block mb-2">
                  Top Terms Pushing Toward Verified / Genuine:
                </span>
                {topGenuineFeatures.length > 0 ? (
                  <ul className="space-y-1.5">
                    {topGenuineFeatures.map((feat, idx) => (
                      <li key={idx} className="flex justify-between items-center text-xs">
                        <span className="font-mono bg-white px-2 py-0.5 rounded border border-emerald-200 text-emerald-900">
                          {feat.token}
                        </span>
                        <span className="font-mono text-emerald-700 text-[11px]">
                          {feat.contribution.toFixed(4)}
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-slate-400 italic">No strong genuine lexical markers detected.</p>
                )}
              </div>
            </div>
          </div>

          {/* Model Metadata Footer */}
          <div className="pt-4 border-t border-slate-100 flex flex-wrap justify-between items-center text-[11px] text-slate-400 font-mono gap-2">
            <span>Model Version: {modelVersion || '1.0.0'}</span>
            <span>Analyzed {wordCount} words ({characterCount} chars)</span>
            <span>Algorithm: TF-IDF + L2 Logistic Regression</span>
          </div>
        </div>
      </div>

      {/* Model Limitations Card */}
      <div className="bg-amber-50/60 rounded-xl border border-amber-200/80 p-6 space-y-3">
        <h3 className="text-sm font-serif font-bold text-amber-950 flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-amber-600" />
          Critical Operational Limitations
        </h3>
        <ul className="text-xs text-amber-900/90 space-y-1.5 list-disc list-inside leading-relaxed">
          {limitations.map((lim, idx) => (
            <li key={idx}>{lim}</li>
          ))}
        </ul>
      </div>
      <EvidencePanel headline={result.headline} text={result.text} />
      <VerificationChecklist actions={verificationActions} />
      {/* Verification Checklist */}
      <VerificationChecklist actions={verificationActions} />

      {/* Bottom CTA Button */}
      <div className="text-center pt-2">
        <button
          onClick={onAnalyzeAnother}
          className="inline-flex items-center gap-2 px-6 py-3 text-sm font-semibold text-white bg-slate-900 hover:bg-slate-800 rounded-lg shadow-sm transition-colors"
          id="btn-analyze-another-bottom"
        >
          <RotateCcw className="w-4 h-4" />
          Analyze Another Item
        </button>
      </div>
    </div>
  );
}
