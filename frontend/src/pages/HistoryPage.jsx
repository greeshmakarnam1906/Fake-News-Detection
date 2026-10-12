import React, { useState, useEffect } from 'react';
import api from '../api/client';
import AnalysisHistoryTable from '../components/AnalysisHistoryTable';
import ConfidenceIndicator from '../components/ConfidenceIndicator';
import { History, X, AlertCircle, FileText, Cpu, Trash2 } from 'lucide-react';

export default function HistoryPage() {
  const [analyses, setAnalyses] = useState([]);
  const [pagination, setPagination] = useState({ page: 0, totalPages: 1, totalElements: 0 });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Detail Modal State
  const [selectedAnalysis, setSelectedAnalysis] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const fetchHistory = async (page = 0) => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/analyses?page=${page}&size=10`);
      setAnalyses(res.data.content);
      setPagination({
        page: res.data.page,
        totalPages: res.data.totalPages,
        totalElements: res.data.totalElements,
      });
    } catch (err) {
      setError(err.customMessage || 'Failed to load analysis history');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory(0);
  }, []);

  const handlePageChange = (newPage) => {
    fetchHistory(newPage);
  };

  const handleViewDetails = async (id) => {
    setDetailLoading(true);
    try {
      const res = await api.get(`/api/analyses/${id}`);
      setSelectedAnalysis(res.data);
    } catch (err) {
      alert(err.customMessage || 'Could not fetch analysis details');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleDeleteAnalysis = async (id) => {
    try {
      await api.delete(`/api/analyses/${id}`);
      setAnalyses((prev) => prev.filter((item) => item.id !== id));
      if (selectedAnalysis?.id === id) {
        setSelectedAnalysis(null);
      }
    } catch (err) {
      alert(err.customMessage || 'Failed to delete record');
    }
  };

  return (
    <div className="max-w-6xl mx-auto py-8 space-y-6">
      <div className="flex flex-wrap justify-between items-center gap-4">
        <div>
          <h1 className="text-3xl font-serif font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
            <History className="w-7 h-7 text-teal-600" />
            Personal Analysis History
          </h1>
          <p className="text-sm text-slate-600 mt-0.5">
            View, review, and manage your previously evaluated news articles and social claims.
          </p>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* History Table */}
      <AnalysisHistoryTable
        analyses={analyses}
        pagination={pagination}
        onPageChange={handlePageChange}
        onViewDetails={handleViewDetails}
        onDeleteAnalysis={handleDeleteAnalysis}
        isLoading={isLoading}
      />

      {/* Detail Modal */}
      {selectedAnalysis && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl border border-slate-200">
            {/* Modal Header */}
            <div className="bg-slate-900 text-white p-6 sticky top-0 z-10 flex justify-between items-start">
              <div>
                <span className="text-[11px] font-mono text-teal-400 uppercase tracking-widest block">
                  Historical Record #{selectedAnalysis.id}
                </span>
                <h2 className="text-xl font-serif font-bold mt-1">
                  {selectedAnalysis.headline || 'Untitled Article Submission'}
                </h2>
                <span className="text-xs text-slate-400 block mt-0.5">
                  Analyzed on {new Date(selectedAnalysis.createdAt).toLocaleString()}
                </span>
              </div>
              <button
                onClick={() => setSelectedAnalysis(null)}
                className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
                aria-label="Close modal"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-6">
              {/* Verdict Summary */}
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50 space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-xs uppercase font-semibold text-slate-500">Verdict</span>
                  <span
                    className={`text-xs px-3 py-1 rounded-full font-bold ${
                      selectedAnalysis.label === 'Likely Genuine'
                        ? 'bg-emerald-100 text-emerald-800'
                        : selectedAnalysis.label === 'Likely Fake'
                        ? 'bg-rose-100 text-rose-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {selectedAnalysis.label}
                  </span>
                </div>
                <p className="text-xs text-slate-700 leading-relaxed font-sans">
                  {selectedAnalysis.explanation}
                </p>
                <ConfidenceIndicator score={selectedAnalysis.score} label={selectedAnalysis.label} />
              </div>

              {/* Explainability Breakdown */}
              <div className="space-y-3">
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                  <Cpu className="w-4 h-4 text-teal-600" />
                  Dominant Feature Terms
                </h4>
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div className="p-3 rounded-lg bg-rose-50 border border-rose-200">
                    <span className="font-bold text-rose-900 block mb-1.5">Fabricated Signals:</span>
                    {selectedAnalysis.topFakeFeatures?.length > 0 ? (
                      <ul className="space-y-1">
                        {selectedAnalysis.topFakeFeatures.map((f, i) => (
                          <li key={i} className="flex justify-between text-[11px]">
                            <span className="font-mono text-rose-900">{f.token}</span>
                            <span className="text-rose-700 font-mono">+{f.contribution.toFixed(4)}</span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <span className="text-slate-400 italic">None detected</span>
                    )}
                  </div>

                  <div className="p-3 rounded-lg bg-emerald-50 border border-emerald-200">
                    <span className="font-bold text-emerald-900 block mb-1.5">Verified Signals:</span>
                    {selectedAnalysis.topGenuineFeatures?.length > 0 ? (
                      <ul className="space-y-1">
                        {selectedAnalysis.topGenuineFeatures.map((f, i) => (
                          <li key={i} className="flex justify-between text-[11px]">
                            <span className="font-mono text-emerald-900">{f.token}</span>
                            <span className="text-emerald-700 font-mono">{f.contribution.toFixed(4)}</span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <span className="text-slate-400 italic">None detected</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Submitted Text Snippet */}
              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1 flex items-center gap-1.5">
                  <FileText className="w-4 h-4 text-slate-400" />
                  Original Submitted Body
                </h4>
                <div className="p-4 rounded-lg bg-slate-50 border border-slate-200 text-xs text-slate-800 leading-relaxed max-h-48 overflow-y-auto whitespace-pre-wrap font-sans">
                  {selectedAnalysis.text}
                </div>
              </div>
            </div>

            {/* Modal Footer */}
            <div className="p-4 bg-slate-50 border-t border-slate-200 flex justify-between items-center">
              <button
                onClick={() => handleDeleteAnalysis(selectedAnalysis.id)}
                className="flex items-center gap-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700 hover:bg-rose-50 px-3 py-1.5 rounded transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" />
                Delete This Record
              </button>
              <button
                onClick={() => setSelectedAnalysis(null)}
                className="px-4 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-100 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
