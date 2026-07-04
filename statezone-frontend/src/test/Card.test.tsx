import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Card from '../components/ui/Card';

describe('Card', () => {
  it('renders children', () => {
    render(
      <Card>
        <p>Conteúdo</p>
      </Card>,
    );
    expect(screen.getByText('Conteúdo')).toBeInTheDocument();
  });

  it('calls onClick and has button role when clickable', async () => {
    const onClick = vi.fn();
    const user = userEvent.setup();
    render(
      <Card onClick={onClick}>
        <p>Click</p>
      </Card>,
    );
    expect(screen.getByRole('button')).toBeInTheDocument();
    await user.click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('supports keyboard Enter key', async () => {
    const onClick = vi.fn();
    const user = userEvent.setup();
    render(
      <Card onClick={onClick}>
        <p>Click</p>
      </Card>,
    );
    await user.tab();
    expect(screen.getByRole('button')).toHaveFocus();
    await user.keyboard('{Enter}');
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
