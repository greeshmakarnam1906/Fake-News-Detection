import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, Search, Database, Cpu, CheckCircle2, AlertTriangle, ArrowRight, Layers, Lock, Sparkles } from 'lucide-react';

const WORKFLOW_STEPS = [
  {
    step: '01',
    title: 'Input Collection & Sanitization',
    desc: 'Accepts news headlines, article text, and optional metadata. Strict validation guards against SSRF without automatic server-side URL scraping.',
    icon: <Search className="w-5 h-5 text-teal-400" />
  },
  {
    step: '02',
    title: 'Text Cleaning & Leakage Removal',
    desc: 'Normalizes unicode, strips punctuation, and removes known wire attribution tags (e.g. Reuters/AP headers) to prevent trivial dataset label leakage.',
    icon: <Layers className="w-5 h-5 text-indigo-400" />
  },
  {
    step: '03',
    title: 'TF-IDF Feature Representation',
    desc: 'Vectorizes text into 6,000 top informative n-grams using sublinear term frequencies and smoothed inverse document frequency weights.',
    icon: <Database className="w-5 h-5 text-teal-400" />
  },
  {
    step: '04',
    title: 'Trained Logistic Classifier',
    desc: 'Evaluates the feature vector against pre-trained, regularized model weights trained on the public George McIntire benchmark dataset in 100% pure Java.',
    icon: <Cpu className="w-5 h-5 text-indigo-400" />
  },
  {
    step: '05',
    title: 'Bounded Model Scoring',
    desc: 'Calculates a deterministic model score between 0.00 and 1.00 representing statistical resemblance to fabricated news patterns.',
    icon: <Sparkles className="w-5 h-5 text-teal-400" />
  },
  {
    step: '06',
    title: 'Tri-Zone Decision Logic',
    desc: 'Assigns cautious labels: Likely Genuine (<=0.40), Likely Fake (>=0.60), or Uncertain / Needs Verification (0.40–0.60) to avoid false certainty.',
    icon: <CheckCircle2 className="w-5 h-5 text-indigo-400" />
  },
  {
    step: '07',
    title: 'Human Verification Guidance',
    desc: 'Pairs predictions with feature explainability (top active terms) and an actionable fact-checking checklist with primary source links.',
    icon: <ShieldCheck className="w-5 h-5 text-teal-400" />
  }
];

export default function LandingPage() {
  return (
    <div className="space-y-16 py-8">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-slate-900 text-white rounded-3xl p-8 sm:p-14 border border-slate-800 shadow-xl">
        <div className="max-w-3xl space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-teal-500/10 border border-teal-500/30 text-teal-400 text-xs font-mono tracking-wide uppercase">
            <ShieldCheck className="w-4 h-4" />
            Java Full-Stack Academic Engineering Capstone
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-serif font-bold tracking-tight text-white leading-tight">
            Fake News Detection <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-teal-400 to-indigo-300">
              On Social Media
            </span>
          </h1>

          <p className="text-base sm:text-lg text-slate-300 font-sans leading-relaxed">
            A transparent machine-learning system built to analyze journalistic and social-media writing patterns.
            Combines TF-IDF feature extraction, L2-regularized logistic classification, and rigorous explainability
            to support critical media literacy.
          </p>

          {/* Action CTAs */}
          <div className="pt-2 flex flex-wrap items-center gap-4">
            <Link
              to="/analyze"
              className="inline-flex items-center gap-2 px-6 py-3.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-semibold text-sm transition-all shadow-lg hover:shadow-teal-500/20"
              id="hero-analyze-cta"
            >
              Analyze News Article
              <ArrowRight className="w-4 h-4" />
            </Link>

            <Link
              to="/register"
              className="inline-flex items-center gap-2 px-6 py-3.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-semibold text-sm transition-colors"
            >
              Create Free Account
            </Link>
          </div>
        </div>

        {/* Academic Principles Disclaimer Callout */}
        <div className="mt-10 p-4 rounded-xl bg-slate-800/80 border border-slate-700/80 text-xs text-slate-300 flex items-start gap-3">
          <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
          <p className="leading-relaxed">
            <strong className="text-amber-300">Core Academic Principle: </strong>
            Model predictions reflect statistical text patterns, never absolute proof of truth or falsehood.
            Satire, evolving vocabulary, and breaking stories require independent human verification.
          </p>
        </div>
      </section>

      {/* 7-Stage Detection Workflow Pipeline */}
      <section className="space-y-8">
        <div className="text-center max-w-2xl mx-auto space-y-2">
          <h2 className="text-xs font-mono font-semibold text-teal-600 uppercase tracking-widest">
            End-To-End Architecture
          </h2>
          <h3 className="text-3xl font-serif font-bold text-slate-900">
            Seven-Stage Detection Pipeline
          </h3>
          <p className="text-sm text-slate-600">
            Every submission passes through a deterministic pipeline designed for reproducibility and explainability.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {WORKFLOW_STEPS.map((step) => (
            <div
              key={step.step}
              className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow relative overflow-hidden flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <div className="p-2.5 rounded-xl bg-slate-900 shadow-sm">
                    {step.icon}
                  </div>
                  <span className="font-mono text-xs font-bold text-slate-400">
                    STAGE {step.step}
                  </span>
                </div>
                <h4 className="text-base font-serif font-bold text-slate-900">
                  {step.title}
                </h4>
                <p className="text-xs text-slate-600 leading-relaxed">
                  {step.desc}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Technology Stack & Model Performance Highlight */}
      <section className="bg-slate-100/70 rounded-3xl p-8 sm:p-12 border border-slate-200 space-y-6">
        <div className="max-w-2xl">
          <h3 className="text-xs font-mono font-semibold text-teal-700 uppercase tracking-widest">
            Verified Benchmarks
          </h3>
          <h4 className="text-2xl font-serif font-bold text-slate-900 mt-1">
            Genuine Model Evaluation Results
          </h4>
          <p className="text-xs text-slate-600 mt-1">
            Trained and evaluated on the George McIntire benchmark dataset (6,297 distinct articles after deduplication) with an 80/20 stratified split.
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="bg-white p-5 rounded-xl border border-slate-200 text-center shadow-sm">
            <span className="text-[11px] font-mono text-slate-500 uppercase block">Test Accuracy</span>
            <span className="text-2xl sm:text-3xl font-serif font-bold text-teal-600 mt-1 block">93.33%</span>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 text-center shadow-sm">
            <span className="text-[11px] font-mono text-slate-500 uppercase block">Precision</span>
            <span className="text-2xl sm:text-3xl font-serif font-bold text-slate-800 mt-1 block">91.99%</span>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 text-center shadow-sm">
            <span className="text-[11px] font-mono text-slate-500 uppercase block">Recall</span>
            <span className="text-2xl sm:text-3xl font-serif font-bold text-slate-800 mt-1 block">94.91%</span>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 text-center shadow-sm">
            <span className="text-[11px] font-mono text-slate-500 uppercase block">F1-Score</span>
            <span className="text-2xl sm:text-3xl font-serif font-bold text-teal-600 mt-1 block">0.9343</span>
          </div>
        </div>
      </section>
    </div>
  );
}
