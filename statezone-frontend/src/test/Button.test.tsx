import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Button from '../components/ui/Button';

describe('Button', () => {
  it('renders children text', () => {
    render(<Button>Salvar</Button>);
    expect(screen.getByRole('button', { name: 'Salvar' })).toBeInTheDocument();
  });

  it('calls onClick when clicked', async () => {
    const onClick = vi.fn();
    const user = userEvent.setup();
    render(<Button onClick={onClick}>Click me</Button>);
    await user.click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('applies variant classes', () => {
    const { container } = render(<Button variant="danger">Excluir</Button>);
    expect(container.firstChild).toHaveClass('bg-danger-bg');
  });

  it('applies size classes', () => {
    const { container } = render(<Button size="lg">Grande</Button>);
    expect(container.firstChild).toHaveClass('text-base');
  });

  it('is disabled when disabled prop is set', () => {
    render(<Button disabled>Desabilitado</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
