import React, { useState, useEffect } from 'react';
import api from '../api/client';
import DashboardSummary from '../components/DashboardSummary';
import { BarChart3, AlertCircle } from 'lucide-react';

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchSummary = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get('/api/dashboard/summary');
      setSummary(res.data);
    } catch (err) {
      setError(err.customMessage || 'Failed to retrieve dashboard metrics');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  return (
    <div className="max-w-6xl mx-auto py-8 space-y-6">
      <div className="space-y-1">
        <h1 className="text-3xl font-serif font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
          <BarChart3 className="w-7 h-7 text-teal-600" />
          Analytics & Model Dashboard
        </h1>
        <p className="text-sm text-slate-600">
          Aggregated distribution metrics for articles and statements analyzed under your account profile.
        </p>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <DashboardSummary summary={summary} isLoading={isLoading} />
    </div>
  );
}
