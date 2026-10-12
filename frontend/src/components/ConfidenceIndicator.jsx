import React from 'react';
import { HelpCircle, Info } from 'lucide-react';

export default function ConfidenceIndicator({ score, label }) {
  // Score is between 0.00 and 1.00
  const percentage = Math.min(Math.max(score * 100, 0), 100);

  let labelColor = 'text-amber-700 bg-amber-50 border-amber-200';
  let barGradient = 'from-emerald-500 via-amber-400 to-rose-500';

  if (label === 'Likely Genuine') {
    labelColor = 'text-emerald-700 bg-emerald-50 border-emerald-200';
  } else if (label === 'Likely Fake') {
    labelColor = 'text-rose-700 bg-rose-50 border-rose-200';
  }

  return (
    <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-3">
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-1.5">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-700">Model Classification Score</span>
          <div className="group relative">
            <HelpCircle className="w-3.5 h-3.5 text-slate-400 cursor-help" />
            <div className="hidden group-hover:block absolute bottom-full mb-1 left-1/2 -translate-x-1/2 w-64 p-2.5 bg-slate-900 text-white text-[11px] rounded shadow-lg z-10 leading-snug">
              This value measures statistical lexical resemblance to training articles. Higher scores reflect stylistic alignment with fabricated stories. It is <strong>not</strong> a calibrated probability of factual truth.
            </div>
          </div>
        </div>
        <span className="font-mono text-sm font-bold text-slate-800">
          {score.toFixed(4)} <span className="text-xs text-slate-500 font-normal">/ 1.0000</span>
        </span>
      </div>

      {/* Tri-Zone Gauge Bar */}
      <div className="relative pt-2 pb-1">
        {/* Progress Background */}
        <div className="h-3 w-full rounded-full bg-slate-200 overflow-hidden relative flex">
          <div className="w-[40%] bg-emerald-500/80" title="Likely Genuine Zone (0.00 - 0.40)" />
          <div className="w-[20%] bg-amber-400/80" title="Uncertain Zone (0.40 - 0.60)" />
          <div className="w-[40%] bg-rose-500/80" title="Likely Fake Zone (0.60 - 1.00)" />
        </div>

        {/* Needle Marker */}
        <div
          className="absolute top-0 -ml-2 transition-all duration-500 flex flex-col items-center"
          style={{ left: `${percentage}%` }}
        >
          <div className="w-4 h-4 bg-slate-900 border-2 border-white rounded-full shadow-md" />
          <div className="w-0.5 h-3 bg-slate-900" />
        </div>
      </div>

      {/* Zone Legend */}
      <div className="flex justify-between text-[11px] text-slate-500 font-mono">
        <span className="text-emerald-700 font-medium">0.00 (Genuine)</span>
        <span className="text-amber-700 font-medium text-center">0.40 – 0.60 (Uncertain)</span>
        <span className="text-rose-700 font-medium text-right">1.00 (Fake)</span>
      </div>

      <div className="pt-2 border-t border-slate-200/60 flex items-start gap-2 text-xs text-slate-500">
        <Info className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
        <span>
          Decision Rule: Scores &le; 0.40 = <em>Likely Genuine</em>, &ge; 0.60 = <em>Likely Fake</em>, and 0.40–0.60 = <em>Uncertain / Needs Verification</em>.
        </span>
      </div>
    </div>
  );
}
