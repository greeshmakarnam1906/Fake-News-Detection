import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Newspaper, History, BarChart3, LogIn, LogOut, UserPlus, Menu, X } from 'lucide-react';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMobileMenuOpen(false);
  };

  const isActive = (path) => location.pathname === path;

  const navLinkClass = (path) =>
    `flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md transition-colors ${
      isActive(path)
        ? 'bg-slate-800 text-teal-400 font-semibold'
        : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
    }`;

  return (
    <header className="bg-slate-900 border-b border-slate-800 sticky top-0 z-50 shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo & Brand */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-lg bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 group-hover:border-teal-400 transition-colors">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <span className="font-serif text-lg font-bold text-white tracking-tight">VeritasNews</span>
              <span className="block text-[11px] text-teal-400/90 font-mono -mt-1 tracking-wider uppercase">Fake News Classifier</span>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-1">
            <Link to="/" className={navLinkClass('/')}>
              Home
            </Link>
            <Link to="/analyze" className={navLinkClass('/analyze')}>
              <Newspaper className="w-4 h-4" />
              Analyze
            </Link>
            {isAuthenticated && (
              <>
                <Link to="/history" className={navLinkClass('/history')}>
                  <History className="w-4 h-4" />
                  History
                </Link>
                <Link to="/dashboard" className={navLinkClass('/dashboard')}>
                  <BarChart3 className="w-4 h-4" />
                  Dashboard
                </Link>
              </>
            )}
          </nav>

          {/* Auth Actions (Desktop) */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <div className="text-xs text-slate-400">Signed in as</div>
                  <div className="text-sm font-medium text-slate-200">{user?.name}</div>
                </div>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-300 bg-slate-800 hover:bg-rose-900/40 hover:text-rose-200 border border-slate-700 rounded-md transition-all"
                  id="nav-logout-button"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  Log Out
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="flex items-center gap-1.5 px-3.5 py-1.5 text-sm font-medium text-slate-200 hover:text-white hover:bg-slate-800 rounded-md transition-colors"
                  id="nav-login-link"
                >
                  <LogIn className="w-4 h-4" />
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="flex items-center gap-1.5 px-3.5 py-1.5 text-sm font-medium text-white bg-teal-600 hover:bg-teal-500 rounded-md transition-colors shadow-sm"
                  id="nav-register-link"
                >
                  <UserPlus className="w-4 h-4" />
                  Register
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-slate-400 hover:text-white focus:outline-none"
              aria-label="Toggle mobile menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-slate-900 border-b border-slate-800 px-4 pt-2 pb-4 space-y-2">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-base font-medium text-slate-200 hover:bg-slate-800 rounded-md"
          >
            Home
          </Link>
          <Link
            to="/analyze"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-base font-medium text-slate-200 hover:bg-slate-800 rounded-md"
          >
            Analyze News
          </Link>
          {isAuthenticated ? (
            <>
              <Link
                to="/history"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-3 py-2 text-base font-medium text-slate-200 hover:bg-slate-800 rounded-md"
              >
                Analysis History
              </Link>
              <Link
                to="/dashboard"
                onClick={() => setMobileMenuOpen(false)}
                className="block px-3 py-2 text-base font-medium text-slate-200 hover:bg-slate-800 rounded-md"
              >
                Dashboard
              </Link>
              <div className="pt-2 border-t border-slate-800 flex justify-between items-center">
                <span className="text-xs text-slate-400">{user?.name}</span>
                <button
                  onClick={handleLogout}
                  className="px-3 py-1 text-xs text-rose-300 bg-rose-950/60 rounded border border-rose-800/40"
                >
                  Log Out
                </button>
              </div>
            </>
          ) : (
            <div className="pt-2 border-t border-slate-800 flex flex-col gap-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center py-2 text-sm font-medium text-slate-200 bg-slate-800 rounded-md"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center py-2 text-sm font-medium text-white bg-teal-600 rounded-md"
              >
                Register Account
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
}
