import React, { useState } from 'react';
import { Eye, Trash2, Calendar, ExternalLink, ChevronLeft, ChevronRight, AlertCircle, FileText, X } from 'lucide-react';

export default function AnalysisHistoryTable({
  analyses = [],
  pagination = { page: 0, totalPages: 1, totalElements: 0 },
  onPageChange,
  onViewDetails,
  onDeleteAnalysis,
  isLoading,
}) {
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  const getLabelBadge = (label) => {
    if (label === 'Likely Genuine') {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
          Likely Genuine
        </span>
      );
    }
    if (label === 'Likely Fake') {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800">
          Likely Fake
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
        Uncertain
      </span>
    );
  };

  const formatDate = (isoString) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-12 text-center">
        <div className="w-8 h-8 border-4 border-teal-500/30 border-t-teal-500 rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm text-slate-500 font-medium">Loading analysis records...</p>
      </div>
    );
  }

  if (analyses.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-12 text-center space-y-3">
        <div className="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mx-auto">
          <FileText className="w-6 h-6" />
        </div>
        <h3 className="text-base font-serif font-semibold text-slate-800">No Previous Analyses</h3>
        <p className="text-xs text-slate-500 max-w-sm mx-auto">
          You haven't run any content classifications yet while signed in. Submit your first article on the Analyze page to begin tracking history.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      {/* Table responsive container */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-900 text-slate-200 uppercase font-mono tracking-wider text-[11px] border-b border-slate-800">
            <tr>
              <th scope="col" className="px-6 py-3.5">Date</th>
              <th scope="col" className="px-6 py-3.5">Content Preview</th>
              <th scope="col" className="px-6 py-3.5">Prediction</th>
              <th scope="col" className="px-6 py-3.5">Score</th>
              <th scope="col" className="px-6 py-3.5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-slate-700">
            {analyses.map((item) => (
              <tr key={item.id} className="hover:bg-slate-50/70 transition-colors">
                {/* Date */}
                <td className="px-6 py-4 whitespace-nowrap text-slate-500">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>{formatDate(item.createdAt)}</span>
                  </div>
                </td>

                {/* Content / Headline */}
                <td className="px-6 py-4 max-w-md">
                  {item.headline ? (
                    <div>
                      <span className="font-semibold text-slate-900 block truncate">{item.headline}</span>
                      <span className="text-slate-500 text-[11px] truncate block">{item.truncatedText}</span>
                    </div>
                  ) : (
                    <span className="text-slate-800 line-clamp-2">{item.truncatedText}</span>
                  )}
                  {item.sourceUrl && (
                    <a
                      href={item.sourceUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex items-center gap-1 text-[11px] text-teal-600 hover:text-teal-700 mt-0.5 truncate max-w-xs"
                    >
                      <ExternalLink className="w-3 h-3" />
                      {item.sourceUrl}
                    </a>
                  )}
                </td>

                {/* Verdict Badge */}
                <td className="px-6 py-4 whitespace-nowrap">
                  {getLabelBadge(item.label)}
                </td>

                {/* Score */}
                <td className="px-6 py-4 whitespace-nowrap font-mono text-slate-700 font-medium">
                  {item.score?.toFixed(4)}
                </td>

                {/* Actions */}
                <td className="px-6 py-4 whitespace-nowrap text-right space-x-2">
                  <button
                    onClick={() => onViewDetails(item.id)}
                    className="p-1.5 text-slate-500 hover:text-teal-600 hover:bg-teal-50 rounded transition-colors"
                    title="View Full Diagnosis"
                    aria-label={`View analysis ${item.id}`}
                  >
                    <Eye className="w-4 h-4" />
                  </button>

                  {deleteConfirmId === item.id ? (
                    <div className="inline-flex items-center gap-1 bg-rose-50 p-1 rounded border border-rose-200">
                      <span className="text-[10px] text-rose-700 font-semibold">Delete?</span>
                      <button
                        onClick={() => {
                          onDeleteAnalysis(item.id);
                          setDeleteConfirmId(null);
                        }}
                        className="px-1.5 py-0.5 text-[10px] font-bold text-white bg-rose-600 hover:bg-rose-700 rounded"
                      >
                        Yes
                      </button>
                      <button
                        onClick={() => setDeleteConfirmId(null)}
                        className="p-0.5 text-slate-400 hover:text-slate-600"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => setDeleteConfirmId(item.id)}
                      className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
                      title="Delete Record"
                      aria-label={`Delete analysis ${item.id}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {pagination.totalPages > 1 && (
        <div className="bg-slate-50 px-6 py-3 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
          <div>
            Showing page <span className="font-semibold text-slate-700">{pagination.page + 1}</span> of{' '}
            <span className="font-semibold text-slate-700">{pagination.totalPages}</span> ({pagination.totalElements} total records)
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => onPageChange(pagination.page - 1)}
              disabled={pagination.page <= 0}
              className="px-3 py-1.5 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1 transition-colors"
            >
              <ChevronLeft className="w-3.5 h-3.5" />
              Previous
            </button>
            <button
              onClick={() => onPageChange(pagination.page + 1)}
              disabled={pagination.page >= pagination.totalPages - 1}
              className="px-3 py-1.5 border border-slate-300 rounded bg-white hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1 transition-colors"
            >
              Next
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
