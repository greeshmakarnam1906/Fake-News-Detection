import React from 'react';
import { BarChart3, AlertOctagon, ShieldCheck, AlertTriangle, Layers, Calendar, ArrowRight, Info } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function DashboardSummary({ summary, isLoading }) {
  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-12 text-center">
        <div className="w-8 h-8 border-4 border-teal-500/30 border-t-teal-500 rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm text-slate-500 font-medium">Aggregating classification statistics...</p>
      </div>
    );
  }

  const {
    totalAnalyses = 0,
    likelyFakeCount = 0,
    likelyGenuineCount = 0,
    uncertainCount = 0,
    recentAnalyses = [],
    disclaimer = "All counts reflect machine-learning statistical predictions and do not constitute verified truth.",
  } = summary || {};

  const fakePct = totalAnalyses > 0 ? (likelyFakeCount / totalAnalyses) * 100 : 0;
  const genuinePct = totalAnalyses > 0 ? (likelyGenuineCount / totalAnalyses) * 100 : 0;
  const uncertainPct = totalAnalyses > 0 ? (uncertainCount / totalAnalyses) * 100 : 0;

  return (
    <div className="space-y-6">
      {/* Disclaimer Banner */}
      <div className="p-4 rounded-xl bg-slate-900 text-slate-200 text-xs flex items-start gap-3 border border-slate-800 shadow-sm">
        <Info className="w-4 h-4 text-teal-400 shrink-0 mt-0.5" />
        <div className="leading-relaxed">
          <span className="font-semibold text-white">Statistical Metric Notice: </span>
          {disclaimer}
        </div>
      </div>

      {/* Summary KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Analyses */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-start mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Total Scans</span>
            <div className="p-2 rounded-lg bg-slate-100 text-slate-700">
              <Layers className="w-4 h-4" />
            </div>
          </div>
          <div className="text-3xl font-serif font-bold text-slate-900">{totalAnalyses}</div>
          <span className="text-[11px] text-slate-400 mt-1 block">Articles analyzed</span>
        </div>

        {/* Likely Genuine */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-start mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-emerald-700">Likely Genuine</span>
            <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="text-3xl font-serif font-bold text-emerald-800">{likelyGenuineCount}</div>
          <span className="text-[11px] text-emerald-600 font-medium mt-1 block">
            {genuinePct.toFixed(1)}% of submissions
          </span>
        </div>

        {/* Uncertain */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-start mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-amber-700">Uncertain Zone</span>
            <div className="p-2 rounded-lg bg-amber-50 text-amber-600">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="text-3xl font-serif font-bold text-amber-800">{uncertainCount}</div>
          <span className="text-[11px] text-amber-600 font-medium mt-1 block">
            {uncertainPct.toFixed(1)}% of submissions
          </span>
        </div>

        {/* Likely Fake */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-start mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-rose-700">Likely Fake</span>
            <div className="p-2 rounded-lg bg-rose-50 text-rose-600">
              <AlertOctagon className="w-4 h-4" />
            </div>
          </div>
          <div className="text-3xl font-serif font-bold text-rose-800">{likelyFakeCount}</div>
          <span className="text-[11px] text-rose-600 font-medium mt-1 block">
            {fakePct.toFixed(1)}% of submissions
          </span>
        </div>
      </div>

      {/* Distribution Proportion Bar */}
      {totalAnalyses > 0 && (
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
          <div className="flex justify-between items-center text-xs">
            <h3 className="font-semibold uppercase tracking-wider text-slate-700 flex items-center gap-2">
              <BarChart3 className="w-4 h-4 text-teal-600" />
              Aggregate Model Prediction Distribution
            </h3>
            <span className="font-mono text-slate-500">{totalAnalyses} records</span>
          </div>

          <div className="h-4 w-full bg-slate-100 rounded-full overflow-hidden flex shadow-inner">
            <div
              className="bg-emerald-500 hover:bg-emerald-600 transition-colors"
              style={{ width: `${genuinePct}%` }}
              title={`Likely Genuine: ${likelyGenuineCount} (${genuinePct.toFixed(1)}%)`}
            />
            <div
              className="bg-amber-400 hover:bg-amber-500 transition-colors"
              style={{ width: `${uncertainPct}%` }}
              title={`Uncertain: ${uncertainCount} (${uncertainPct.toFixed(1)}%)`}
            />
            <div
              className="bg-rose-500 hover:bg-rose-600 transition-colors"
              style={{ width: `${fakePct}%` }}
              title={`Likely Fake: ${likelyFakeCount} (${fakePct.toFixed(1)}%)`}
            />
          </div>

          <div className="flex justify-between text-xs text-slate-600 pt-1">
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-full bg-emerald-500" />
              <span>Genuine: {genuinePct.toFixed(1)}% ({likelyGenuineCount})</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-full bg-amber-400" />
              <span>Uncertain: {uncertainPct.toFixed(1)}% ({uncertainCount})</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-full bg-rose-500" />
              <span>Fake: {fakePct.toFixed(1)}% ({likelyFakeCount})</span>
            </div>
          </div>
        </div>
      )}

      {/* Recent Analyses Snippets */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-base font-serif font-bold text-slate-900">Recent Classifications</h3>
          <Link
            to="/history"
            className="text-xs font-semibold text-teal-600 hover:text-teal-700 flex items-center gap-1"
          >
            View Full History
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {recentAnalyses.length === 0 ? (
          <p className="text-xs text-slate-400 italic">No recent analyses found.</p>
        ) : (
          <div className="divide-y divide-slate-100">
            {recentAnalyses.map((item) => (
              <div key={item.id} className="py-3 flex justify-between items-center gap-4">
                <div className="min-w-0">
                  <p className="text-xs font-semibold text-slate-800 truncate">
                    {item.headline || item.truncatedText}
                  </p>
                  <span className="text-[11px] text-slate-400">
                    {new Date(item.createdAt).toLocaleDateString()} &bull; Score: {item.score?.toFixed(4)}
                  </span>
                </div>
                <span
                  className={`text-[11px] px-2 py-0.5 rounded-full font-medium shrink-0 ${
                    item.label === 'Likely Genuine'
                      ? 'bg-emerald-100 text-emerald-800'
                      : item.label === 'Likely Fake'
                      ? 'bg-rose-100 text-rose-800'
                      : 'bg-amber-100 text-amber-800'
                  }`}
                >
                  {item.label}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
