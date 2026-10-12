import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import NewsInputForm from '../components/NewsInputForm';

describe('NewsInputForm Component', () => {
  it('validates empty submission and shows error message', () => {
    const handleSubmit = vi.fn();
    render(<NewsInputForm onSubmit={handleSubmit} isLoading={false} apiError={null} />);

    const submitBtn = screen.getByRole('button', { name: /Analyze Content/i });
    fireEvent.click(submitBtn);

    expect(handleSubmit).not.toHaveBeenCalled();
    expect(screen.getByText(/Article text or social-media post is required/i)).toBeInTheDocument();
  });

  it('populates fields when sample button is clicked', () => {
    const handleSubmit = vi.fn();
    render(<NewsInputForm onSubmit={handleSubmit} isLoading={false} apiError={null} />);

    const sampleBtn = screen.getByRole('button', { name: /Sample Verified/i });
    fireEvent.click(sampleBtn);

    const textArea = screen.getByPlaceholderText(/Paste complete news article/i);
    const headlineInput = screen.getByPlaceholderText(/e\.g\., Congress Approves/i);
    expect(headlineInput.value).toContain('NASA Mars Rover');
    expect(textArea.value).toContain('Scientists at the Jet Propulsion Laboratory');
  });

  it('submits valid text and calls onSubmit callback', () => {
    const handleSubmit = vi.fn();
    render(<NewsInputForm onSubmit={handleSubmit} isLoading={false} apiError={null} />);

    const textArea = screen.getByPlaceholderText(/Paste complete news article/i);
    fireEvent.change(textArea, {
      target: { value: 'This is a sufficiently long news article text for testing purposes.' }
    });

    const submitBtn = screen.getByRole('button', { name: /Analyze Content/i });
    fireEvent.click(submitBtn);

    expect(handleSubmit).toHaveBeenCalledTimes(1);
    expect(handleSubmit).toHaveBeenCalledWith(expect.objectContaining({
      text: 'This is a sufficiently long news article text for testing purposes.',
    }));
  });

  it('renders loading state and disables submit button during analysis', () => {
    render(<NewsInputForm onSubmit={vi.fn()} isLoading={true} apiError={null} />);

    const loadingBtn = screen.getByRole('button', { name: /Running Classifier/i });
    expect(loadingBtn).toBeDisabled();
  });

  it('displays API error alert when apiError prop is passed', () => {
    render(<NewsInputForm onSubmit={vi.fn()} isLoading={false} apiError="Model inference timed out" />);

    expect(screen.getByText(/Model inference timed out/i)).toBeInTheDocument();
  });
});
