import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import Modal from '../components/ui/Modal';

describe('Modal', () => {
  const onClose = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  function renderModal() {
    return render(
      <MemoryRouter>
        <Modal title="Test Modal" onClose={onClose}>
          <p>Conteúdo do modal</p>
          <button>Botão 1</button>
          <button>Botão 2</button>
        </Modal>
      </MemoryRouter>,
    );
  }

  it('renderiza título e conteúdo', () => {
    renderModal();
    expect(screen.getByText('Test Modal')).toBeInTheDocument();
    expect(screen.getByText('Conteúdo do modal')).toBeInTheDocument();
  });

  it('possui role="dialog" e aria-modal="true"', () => {
    renderModal();
    const dialog = screen.getByRole('dialog');
    expect(dialog).toHaveAttribute('aria-modal', 'true');
  });

  it('fecha com botão X', async () => {
    const user = userEvent.setup();
    renderModal();
    await user.click(screen.getByLabelText('Fechar'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('fecha com tecla Escape', async () => {
    const user = userEvent.setup();
    renderModal();
    await user.keyboard('{Escape}');
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('fecha ao clicar no backdrop', async () => {
    const user = userEvent.setup();
    renderModal();
    const backdrop = screen.getByRole('dialog');
    await user.click(backdrop);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('não fecha ao clicar dentro do conteúdo', async () => {
    const user = userEvent.setup();
    renderModal();
    await user.click(screen.getByText('Conteúdo do modal'));
    expect(onClose).not.toHaveBeenCalled();
  });

  it('focus trap mantém foco dentro do modal', async () => {
    const user = userEvent.setup();
    renderModal();

    // Focus trap auto-focuses close button; Tab moves to next element
    expect(screen.getByLabelText('Fechar')).toHaveFocus();

    await user.tab();
    expect(screen.getByText('Botão 1')).toHaveFocus();

    await user.tab();
    expect(screen.getByText('Botão 2')).toHaveFocus();

    // Tab should cycle back to first focusable
    await user.tab();
    expect(screen.getByLabelText('Fechar')).toHaveFocus();
  });
});
