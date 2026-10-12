import React from 'react';
import { AlertTriangle, BookOpen, ExternalLink, Code2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-slate-900 border-t border-slate-800 text-slate-400 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Mission & Academic Disclaimer */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-slate-200 uppercase tracking-wider flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              Academic Disclaimer
            </h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              This application is an educational and research demonstrator powered by a TF-IDF and Logistic Regression
              classifier. Text-classification estimates statistical resemblance to training corpora and cannot
              conclusively establish journalistic truth. Always consult verified fact-checking institutions.
            </p>
          </div>

          {/* Research & Tech Architecture */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-slate-200 uppercase tracking-wider flex items-center gap-2">
              <Code2 className="w-4 h-4 text-teal-400" />
              Technology Architecture
            </h4>
            <ul className="text-xs space-y-1.5 text-slate-400">
              <li>• Pure Java ML Pipeline (TF-IDF + Logistic Regression)</li>
              <li>• Spring Boot 3.3 REST API with Spring Security & JWT</li>
              <li>• MySQL 8 with Versioned Flyway Migrations</li>
              <li>• React 19 with Vite & Tailwind CSS</li>
            </ul>
          </div>

          {/* Developer & API Links */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-slate-200 uppercase tracking-wider flex items-center gap-2">
              <BookOpen className="w-4 h-4 text-indigo-400" />
              Documentation & Endpoints
            </h4>
            <ul className="text-xs space-y-2">
              <li>
                <a
                  href="/swagger-ui.html"
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1.5 text-teal-400 hover:text-teal-300 transition-colors"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Swagger / OpenAPI Interactive UI
                </a>
              </li>
              <li>
                <a
                  href="/actuator/health"
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1.5 text-teal-400 hover:text-teal-300 transition-colors"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Spring Boot Actuator Health Check
                </a>
              </li>
              <li>
                <a
                  href="/v3/api-docs"
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1.5 text-teal-400 hover:text-teal-300 transition-colors"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Raw OpenAPI 3.0 Specification
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-6 border-t border-slate-800 text-center text-xs text-slate-500">
          Fake News Detection on Social Media — Java Full-Stack Academic Engineering Capstone &copy; {new Date().getFullYear()}
        </div>
      </div>
    </footer>
  );
}
