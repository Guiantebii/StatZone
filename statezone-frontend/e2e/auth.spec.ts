import { test, expect } from '@playwright/test';

test.describe('Login', () => {
  test('mostra formulário de login', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Senha')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
  });

  test('mostra erro com credenciais inválidas', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email').fill('invalido@teste.com');
    await page.getByLabel('Senha').fill('senha_errada');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page.getByText(/inválidas|Credenciais|erro/i)).toBeVisible({ timeout: 10000 });
  });

  test('redireciona para dashboard após login admin', async ({ page }) => {
    const email = process.env.E2E_EMAIL || 'admin@teste.com';
    const senha = process.env.E2E_SENHA || 'admin123';

    await page.goto('/login');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Senha').fill(senha);
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });
});

test.describe('Dashboard', () => {
  test('exibe cards de estatísticas', async ({ page }) => {
    const email = process.env.E2E_EMAIL || 'admin@teste.com';
    const senha = process.env.E2E_SENHA || 'admin123';

    await page.goto('/login');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Senha').fill(senha);
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    await expect(page.getByText(/campeonatos/i)).toBeVisible();
    await expect(page.getByText(/times/i)).toBeVisible();
    await expect(page.getByText(/jogadores/i)).toBeVisible();
  });

  test('navega para listagem de campeonatos', async ({ page }) => {
    const email = process.env.E2E_EMAIL || 'admin@teste.com';
    const senha = process.env.E2E_SENHA || 'admin123';

    await page.goto('/login');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Senha').fill(senha);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    await page.getByRole('link', { name: /campeonatos/i }).first().click();
    await expect(page).toHaveURL(/\/dashboard\/campeonatos/, { timeout: 10000 });
  });
});
