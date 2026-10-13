import React, { useState } from 'react';
import { Send, Sparkles, RotateCcw, AlertCircle, Link as LinkIcon, FileText, Tag, Heading } from 'lucide-react';

const SAMPLE_ARTICLES = [
  {
    headline: "NASA Officials Announce Mars Rover Results, Lawmakers To Review Cost",
    text: "NASA officials said on Tuesday that the agency would hold a press conference to announce results from the Perseverance rover mission. Scientists at the Jet Propulsion Laboratory said the rover collected core samples from an ancient river delta on Mars. Officials said the samples would be returned to Earth under a plan approved by Congress, and critics said the cost of the program was too high.",
    sourceUrl: "https://nasa.gov/missions/mars-2020-perseverance",
    category: "Science & Health"
  },
  {
    headline: "Shocking Secret Plot Exposed By Whistleblower Inside Elite Network",
    text: "An explosive secret leak has completely shattered the mainstream media narrative! High level globalists were reportedly caught holding secret emergency summit to secretly manipulate weather patterns and harvest untraceable frequencies. The shocking video has already been banned on major social networks within minutes of being posted! Share this everywhere before they delete it!",
    sourceUrl: "https://unverified-conspiracy-wire.org/alert/88219",
    category: "Politics"
  }
];

export default function NewsInputForm({ onSubmit, isLoading, apiError }) {
  const [headline, setHeadline] = useState('');
  const [text, setText] = useState('');
  const [sourceUrl, setSourceUrl] = useState('');
  const [category, setCategory] = useState('');
  const [clientErrors, setClientErrors] = useState({});

  const MAX_CHARS = 10000;
  const charCount = text.length;

  const validate = () => {
    const errors = {};
    if (!text.trim()) {
      errors.text = 'Article text or social-media post is required.';
    } else if (text.trim().length < 10) {
      errors.text = 'Text must be at least 10 characters long.';
    } else if (text.length > MAX_CHARS) {
      errors.text = `Text cannot exceed ${MAX_CHARS.toLocaleString()} characters.`;
    }

    if (sourceUrl.trim()) {
      const urlPattern = /^https?:\/\/[a-zA-Z0-9.-]+(?:\.[a-zA-Z]{2,})(?::\d+)?(?:\/\S*)?$/;
      if (!urlPattern.test(sourceUrl.trim())) {
        errors.sourceUrl = 'Please enter a valid HTTP or HTTPS URL (e.g., https://example.com/article).';
      }
    }

    setClientErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSubmit({
        headline: headline.trim() || null,
        text: text.trim(),
        sourceUrl: sourceUrl.trim() || null,
        category: category.trim() || null,
      });
    }
  };

  const loadSample = (index) => {
    const sample = SAMPLE_ARTICLES[index];
    setHeadline(sample.headline);
    setText(sample.text);
    setSourceUrl(sample.sourceUrl);
    setCategory(sample.category);
    setClientErrors({});
  };

  const handleReset = () => {
    setHeadline('');
    setText('');
    setSourceUrl('');
    setCategory('');
    setClientErrors({});
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      {/* Card Header */}
      <div className="bg-slate-900 text-white px-6 py-4 flex flex-wrap justify-between items-center gap-3">
        <div>
          <h2 className="text-lg font-serif font-semibold tracking-tight flex items-center gap-2">
            <FileText className="w-5 h-5 text-teal-400" />
            Input News Content For Classification
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Submit text to extract linguistic features and estimate resemblance to verified or fabricated corpora.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => loadSample(0)}
            className="text-xs font-medium px-2.5 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-teal-300 border border-slate-700 flex items-center gap-1 transition-colors"
            id="btn-sample-genuine"
          >
            <Sparkles className="w-3 h-3 text-teal-400" />
            Sample Verified
          </button>
          <button
            type="button"
            onClick={() => loadSample(1)}
            className="text-xs font-medium px-2.5 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-rose-300 border border-slate-700 flex items-center gap-1 transition-colors"
            id="btn-sample-fabricated"
          >
            <Sparkles className="w-3 h-3 text-rose-400" />
            Sample Fabricated
          </button>
        </div>
      </div>

      {/* Form Content */}
      <form onSubmit={handleSubmit} className="p-6 space-y-5" noValidate>
        {apiError && (
          <div className="p-4 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-900">Analysis Error</p>
              <p className="text-xs text-rose-700 mt-0.5">{apiError}</p>
            </div>
          </div>
        )}

        {/* Headline Input (Optional) */}
        <div>
          <label htmlFor="headline-input" className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1.5 flex items-center gap-1.5">
            <Heading className="w-3.5 h-3.5 text-slate-400" />
            Article Headline or Post Title <span className="text-slate-400 font-normal lowercase">(optional)</span>
          </label>
          <input
            id="headline-input"
            type="text"
            value={headline}
            onChange={(e) => setHeadline(e.target.value)}
            maxLength={500}
            placeholder="e.g., Congress Approves Bipartisan Infrastructure Funding Bill"
            className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 text-slate-900 text-sm focus:border-teal-500 focus:ring-1 focus:ring-teal-500 transition-colors"
          />
        </div>

        {/* Text Area (Required) */}
        <div>
          <div className="flex justify-between items-center mb-1.5">
            <label htmlFor="text-input" className="block text-xs font-semibold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-teal-600" />
              Full Article Body or Social-Media Text <span className="text-rose-500">*</span>
            </label>
            <span className={`text-xs font-mono ${charCount > MAX_CHARS ? 'text-rose-600 font-bold' : 'text-slate-400'}`}>
              {charCount.toLocaleString()} / {MAX_CHARS.toLocaleString()} characters
            </span>
          </div>
          <textarea
            id="text-input"
            rows={7}
            value={text}
            onChange={(e) => {
              setText(e.target.value);
              if (clientErrors.text) setClientErrors({ ...clientErrors, text: null });
            }}
            placeholder="Paste complete news article, paragraphs, or social-media post text here for analysis..."
            className={`w-full px-3.5 py-3 rounded-lg border text-sm text-slate-900 leading-relaxed font-sans transition-colors resize-y ${
              clientErrors.text
                ? 'border-rose-400 bg-rose-50/20 focus:border-rose-500 focus:ring-rose-500'
                : 'border-slate-300 focus:border-teal-500 focus:ring-1 focus:ring-teal-500'
            }`}
          />
          {clientErrors.text && (
            <p className="mt-1.5 text-xs text-rose-600 flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5" />
              {clientErrors.text}
            </p>
          )}
        </div>

        {/* Optional Source URL and Category Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Source URL */}
          <div>
            <label htmlFor="source-url-input" className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1.5 flex items-center gap-1.5">
              <LinkIcon className="w-3.5 h-3.5 text-slate-400" />
              Source URL / Attribution Link <span className="text-slate-400 font-normal lowercase">(optional)</span>
            </label>
            <input
              id="source-url-input"
              type="url"
              value={sourceUrl}
              onChange={(e) => {
                setSourceUrl(e.target.value);
                if (clientErrors.sourceUrl) setClientErrors({ ...clientErrors, sourceUrl: null });
              }}
              placeholder="https://example.com/article-source"
              className={`w-full px-3.5 py-2.5 rounded-lg border text-sm text-slate-900 transition-colors ${
                clientErrors.sourceUrl
                  ? 'border-rose-400 bg-rose-50/20'
                  : 'border-slate-300 focus:border-teal-500 focus:ring-1 focus:ring-teal-500'
              }`}
            />
            {clientErrors.sourceUrl && (
              <p className="mt-1.5 text-xs text-rose-600 flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5" />
                {clientErrors.sourceUrl}
              </p>
            )}
            <p className="text-[11px] text-slate-400 mt-1">
              Note: URLs are validated for format only and never fetched server-side (preventing SSRF).
            </p>
          </div>

          {/* Category */}
          <div>
            <label htmlFor="category-select" className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1.5 flex items-center gap-1.5">
              <Tag className="w-3.5 h-3.5 text-slate-400" />
              Topic Category <span className="text-slate-400 font-normal lowercase">(optional)</span>
            </label>
            <select
              id="category-select"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 bg-white text-sm text-slate-900 focus:border-teal-500 focus:ring-1 focus:ring-teal-500 transition-colors"
            >
              <option value="">Select a category (optional)...</option>
              <option value="Politics">Politics & Governance</option>
              <option value="Economy">Economy & Business</option>
              <option value="Science & Health">Science, Medicine & Health</option>
              <option value="Technology">Technology & AI</option>
              <option value="World News">World & International Affairs</option>
              <option value="Entertainment">Entertainment & Culture</option>
            </select>
          </div>
        </div>

        {/* Buttons / Actions */}
        <div className="pt-3 border-t border-slate-100 flex flex-wrap justify-between items-center gap-3">
          <button
            type="button"
            onClick={handleReset}
            disabled={isLoading || (!headline && !text && !sourceUrl && !category)}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-medium text-slate-600 hover:text-slate-800 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            id="btn-reset-form"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Clear Form
          </button>

          <button
            type="submit"
            disabled={isLoading}
            className="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold text-white bg-teal-600 hover:bg-teal-500 active:bg-teal-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg shadow transition-all"
            id="btn-analyze-submit"
          >
            {isLoading ? (
              <>
                <div className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                <span>Running Classifier...</span>
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                <span>Analyze Content</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
