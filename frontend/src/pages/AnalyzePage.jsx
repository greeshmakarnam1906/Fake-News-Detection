import React, { useState } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import NewsInputForm from '../components/NewsInputForm';
import PredictionResult from '../components/PredictionResult';
import { Newspaper, Info, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function AnalyzePage() {
  const { isAuthenticated, user } = useAuth();
  const [result, setResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState(null);

  const handleAnalyze = async (formData) => {
    setIsLoading(true);
    setApiError(null);

    try {
      const response = await api.post('/api/analyses', formData);
      setResult(response.data);
      // Smooth scroll to top of result
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      setApiError(err.customMessage || 'Analysis failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAnalyzeAnother = () => {
    setResult(null);
    setApiError(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="max-w-4xl mx-auto py-8 space-y-6">
      {/* Page Title */}
      <div className="space-y-1">
        <h1 className="text-3xl font-serif font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
          <Newspaper className="w-7 h-7 text-teal-600" />
          News Content Analysis
        </h1>
        <p className="text-sm text-slate-600">
          Enter an article headline, body paragraphs, or a social-media claim to evaluate linguistic and stylistic signals.
        </p>
      </div>

      {/* Account History Sync Status Banner */}
      {isAuthenticated ? (
        <div className="p-3.5 rounded-lg bg-teal-50 border border-teal-200 text-teal-900 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle className="w-4 h-4 text-teal-600 shrink-0" />
            <span>
              Signed in as <strong>{user?.name}</strong>. Your analysis results are automatically synced to your personal history.
            </span>
          </div>
          <Link to="/history" className="font-semibold text-teal-700 hover:text-teal-800 underline">
            View History
          </Link>
        </div>
      ) : (
        <div className="p-3.5 rounded-lg bg-slate-100 border border-slate-200 text-slate-700 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Info className="w-4 h-4 text-slate-500 shrink-0" />
            <span>
              Guest mode: You can run unlimited evaluations. Sign in to save analyses to your personal account history.
            </span>
          </div>
          <Link to="/login" className="font-semibold text-teal-600 hover:text-teal-700 underline">
            Sign In
          </Link>
        </div>
      )}

      {/* Primary Workspace: Form or Results */}
      {result ? (
        <PredictionResult result={result} onAnalyzeAnother={handleAnalyzeAnother} />
      ) : (
        <NewsInputForm onSubmit={handleAnalyze} isLoading={isLoading} apiError={apiError} />
      )}
    </div>
  );
}
