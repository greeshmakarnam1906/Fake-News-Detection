import React from 'react';
import { Link } from 'react-router-dom';
import { HelpCircle, ArrowLeft } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="max-w-md mx-auto py-20 text-center space-y-4">
      <div className="w-16 h-16 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto">
        <HelpCircle className="w-8 h-8" />
      </div>
      <h1 className="text-3xl font-serif font-bold text-slate-900">404 - Page Not Found</h1>
      <p className="text-xs text-slate-500 leading-relaxed">
        The requested URL was not found on this server. Check the address or return to the application dashboard.
      </p>
      <div className="pt-2">
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-teal-600 hover:bg-teal-500 text-white text-xs font-semibold shadow transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          Return to Home
        </Link>
      </div>
    </div>
  );
}
