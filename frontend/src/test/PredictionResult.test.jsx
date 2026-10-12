import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import PredictionResult from '../components/PredictionResult';

describe('PredictionResult Component', () => {
  const mockResult = {
    label: 'Likely Fake',
    score: 0.8842,
    explanation: 'High stylistic similarity to fabricated stories.',
    topFakeFeatures: [
      { token: 'shocking', weight: 1.25, contribution: 0.45 },
      { token: 'conspiracy', weight: 0.95, contribution: 0.32 },
    ],
    topGenuineFeatures: [
      { token: 'official', weight: -0.75, contribution: -0.15 },
    ],
    limitations: [
      'Model reflects statistical patterns, not journalistic verification.',
    ],
    verificationActions: [
      'Cross-reference claims with primary sources.',
    ],
    modelVersion: '1.0.0',
    characterCount: 350,
    wordCount: 52,
  };

  it('renders verdict label, score, and explanation accurately', () => {
    render(<PredictionResult result={mockResult} onAnalyzeAnother={vi.fn()} />);

    expect(screen.getByRole('heading', { name: 'Likely Fake' })).toBeInTheDocument();
    expect(screen.getByText(/0.8842/)).toBeInTheDocument();
    expect(screen.getByText(/High stylistic similarity to fabricated stories/)).toBeInTheDocument();
  });

  it('renders top feature terms with weights', () => {
    render(<PredictionResult result={mockResult} onAnalyzeAnother={vi.fn()} />);

    expect(screen.getByText('shocking')).toBeInTheDocument();
    expect(screen.getByText('+0.4500')).toBeInTheDocument();
    expect(screen.getByText('official')).toBeInTheDocument();
  });

  it('calls onAnalyzeAnother when reset button is clicked', () => {
    const handleReset = vi.fn();
    render(<PredictionResult result={mockResult} onAnalyzeAnother={handleReset} />);

    const buttons = screen.getAllByRole('button', { name: /Analyze Another Item/i });
    fireEvent.click(buttons[0]);

    expect(handleReset).toHaveBeenCalledTimes(1);
  });
});
