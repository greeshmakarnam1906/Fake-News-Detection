import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import AnalysisHistoryTable from '../components/AnalysisHistoryTable';

describe('AnalysisHistoryTable Component', () => {
  const mockAnalyses = [
    {
      id: 1,
      headline: 'NASA Mars Discovery',
      truncatedText: 'Scientists confirmed rover findings...',
      sourceUrl: 'https://nasa.gov/sample',
      label: 'Likely Genuine',
      score: 0.22,
      createdAt: '2026-10-11T12:00:00Z',
    },
    {
      id: 2,
      headline: null,
      truncatedText: 'Shocking conspiracy plot exposed...',
      sourceUrl: null,
      label: 'Likely Fake',
      score: 0.85,
      createdAt: '2026-10-11T13:00:00Z',
    },
  ];

  it('renders table headers and row items properly', () => {
    render(
      <AnalysisHistoryTable
        analyses={mockAnalyses}
        pagination={{ page: 0, totalPages: 1, totalElements: 2 }}
        onPageChange={vi.fn()}
        onViewDetails={vi.fn()}
        onDeleteAnalysis={vi.fn()}
        isLoading={false}
      />
    );

    expect(screen.getByText('NASA Mars Discovery')).toBeInTheDocument();
    expect(screen.getByText('Likely Genuine')).toBeInTheDocument();
    expect(screen.getByText('Likely Fake')).toBeInTheDocument();
    expect(screen.getByText(/0.2200/)).toBeInTheDocument();
  });

  it('calls onViewDetails when view button is clicked', () => {
    const handleView = vi.fn();
    render(
      <AnalysisHistoryTable
        analyses={mockAnalyses}
        pagination={{ page: 0, totalPages: 1, totalElements: 2 }}
        onPageChange={vi.fn()}
        onViewDetails={handleView}
        onDeleteAnalysis={vi.fn()}
        isLoading={false}
      />
    );

    const viewBtns = screen.getAllByLabelText(/View analysis/i);
    fireEvent.click(viewBtns[0]);

    expect(handleView).toHaveBeenCalledWith(1);
  });

  it('renders empty state when analyses array is empty', () => {
    render(
      <AnalysisHistoryTable
        analyses={[]}
        pagination={{ page: 0, totalPages: 0, totalElements: 0 }}
        onPageChange={vi.fn()}
        onViewDetails={vi.fn()}
        onDeleteAnalysis={vi.fn()}
        isLoading={false}
      />
    );

    expect(screen.getByText(/No Previous Analyses/i)).toBeInTheDocument();
  });
});
