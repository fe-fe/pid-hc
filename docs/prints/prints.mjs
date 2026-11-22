import { chromium } from 'playwright';

const BASE = process.env.BASE;
const CODIGO = process.env.CODIGO;
const ADMIN = { nome: 'Ana Administradora', email: 'admin@pid.hc', senha: 'Senha@2026' };
const ANALISTA = { nome: 'Bruno Analista', email: 'analista@pid.hc', senha: 'Senha@2026' };

const browser = await chromium.launch({ channel: 'chromium', args: ['--lang=pt-BR'] });
const contexto = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  deviceScaleFactor: 2,
  colorScheme: 'dark',
  locale: 'pt-BR',
});
await contexto.addInitScript(() => localStorage.setItem('pid-hc-theme', 'dark'));
const page = await contexto.newPage();
const falhas = [];

const id = (valor) => page.locator(`[id="${valor}"]`);

async function abrir(caminho) {
  await page.goto(BASE + caminho);
  await page.waitForLoadState('networkidle');
}

async function foto(nome, recorte) {
  await page.waitForTimeout(400);
  await (recorte ?? page).screenshot({ path: `/telas/${nome}.png` });
  console.log(`  ${nome}.png`);
}

async function modal() {
  await page.locator('.modal.show').first().waitFor();
  await page.waitForTimeout(400);
}

async function fechar() {
  await page.keyboard.press('Escape');
  await page.locator('.modal.show').first().waitFor({ state: 'detached' }).catch(() => {});
  await page.waitForTimeout(300);
}

async function tela(nome, passos, recorte) {
  try {
    await passos();
    await foto(nome, recorte);
  } catch (erro) {
    falhas.push(nome);
    console.log(`  ${nome}: falhou (${erro.message.split('\n')[0]})`);
  }
}

async function entrar(usuario, senha = usuario.senha) {
  await abrir('/pages/auth/login.xhtml');
  await id('authForm:email').fill(usuario.email);
  await id('authForm:senha').fill(senha);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await page.waitForLoadState('networkidle');
}

async function sair() {
  await page.getByText('Sair', { exact: true }).first().click();
  await page.waitForLoadState('networkidle');
}

await tela('primeiro-acesso', () => abrir('/pages/auth/init.xhtml'));

await id('authForm:codigo').fill(CODIGO);
await id('authForm:nome').fill(ADMIN.nome);
await id('authForm:email').fill(ADMIN.email);
await id('authForm:senha').fill(ADMIN.senha);
await page.getByRole('button', { name: 'Criar administrador' }).click();
await page.waitForLoadState('networkidle');

await tela('login', () => abrir('/pages/auth/login.xhtml'));
await entrar(ADMIN, 'senha-errada');
await entrar(ADMIN);

await tela('dashboard', async () => {
  await abrir('/pages/admin/dashboard.xhtml');
  await id('formBuscaMaterial:termo').fill('luva');
  await page.getByRole('button', { name: /Consultar/ }).click();
  await page.waitForLoadState('networkidle');
  const sugestao = page.locator('.list-group-item-action').first();
  if (await sugestao.isVisible().catch(() => false)) {
    await sugestao.click();
    await page.waitForLoadState('networkidle');
  }
});
await tela('dashboard-tecnovigilancia', async () => {
  await page.getByRole('button', { name: 'Tecnovigilância' }).first().click();
  await page.waitForLoadState('networkidle');
});

await tela('usuario-modal', async () => {
  await abrir('/pages/admin/users.xhtml');
  await page.getByText('Novo usuário').click();
  await modal();
  await id('formCadastro:nome').fill(ANALISTA.nome);
  await id('formCadastro:email').fill(ANALISTA.email);
  await id('formCadastro:senha').fill(ANALISTA.senha);
  await id('formCadastro:perfil').selectOption('ANALISTA');
});
await page.getByRole('button', { name: 'Salvar' }).click().catch(() => {});
await page.waitForLoadState('networkidle');
await fechar();
await tela('usuarios', () => abrir('/pages/admin/users.xhtml'));

await tela('materiais', () => abrir('/pages/controle/materiais.xhtml'));
await tela('material-modal', async () => {
  await page.locator('a.page__button--primary').first().click();
  await modal();
});
await fechar();
await tela('fabricantes', () => abrir('/pages/controle/fabricantes.xhtml'));
await tela('marcas', () => abrir('/pages/controle/marcas.xhtml'));
await tela('marca-modal', async () => {
  await page.locator('a.page__button--primary').first().click();
  await modal();
});
await fechar();

await tela('avaliacoes', () => abrir('/pages/avaliacoes.xhtml'));
await tela('avaliacoes-vencidas', async () => {
  await page.getByRole('button', { name: /Filtrar/ }).first().click();
  await page.locator('.offcanvas.show').first().waitFor();
  await id('formFiltrosAvaliacao:somenteVencidas').check();
  await page.getByRole('button', { name: 'Aplicar filtros' }).click();
  await page.waitForLoadState('networkidle');
  await page.locator('.offcanvas.show').first().waitFor({ state: 'detached' }).catch(() => {});
});
await abrir('/pages/avaliacoes.xhtml');
await tela('avaliacao-modal', async () => {
  await page.locator('a.page__button--primary').first().click();
  await modal();
});
await tela('avaliacao-sugestao', async () => {
  await page.getByLabel('Código do material').last().fill('104210');
  await page.getByLabel('Código do material').last().blur();
  await page.waitForLoadState('networkidle');
  await page.getByLabel('Registro ANVISA').fill('80123450011');
  await page.getByLabel('Registro ANVISA').blur();
  await page.waitForLoadState('networkidle');
});
await fechar();
await tela('avaliacao-editar', async () => {
  await abrir('/pages/avaliacoes.xhtml');
  await page.locator('[title="Editar avaliação"]').first().click();
  await modal();
});
await fechar();

await tela('historico-material', async () => {
  await abrir('/pages/historicoMateriais.xhtml');
  await page.getByPlaceholder('Pesquisar material por código').fill('104210');
  await page.getByRole('button', { name: 'Pesquisar' }).click();
  await page.waitForLoadState('networkidle');
});

await tela('tecnovigilancia', () => abrir('/pages/tecnovigilancia.xhtml'));
await tela('desvio-modal', async () => {
  await page.locator('[title="Editar tecnovigilância"]').first().click();
  await modal();
});
await fechar();
await tela('importar-planilha', () => abrir('/pages/importarPlanilha.xhtml'));
await tela('anvisa', () => abrir('/pages/controle/anvisa.xhtml'));

await tela('menu-sair', () => abrir('/pages/admin/dashboard.xhtml'), page.locator('aside.sidebar'));
await sair();
await entrar(ANALISTA);
await tela('dashboard-analista', () => abrir('/pages/analista/dashboard.xhtml'));
await sair();

await entrar(ADMIN);
await tela('configuracoes', async () => {
  await abrir('/pages/admin/configuracoes.xhtml');
  await page.getByRole('button', { name: 'Salvar' }).click();
  await page.waitForLoadState('networkidle');
});
await tela('auditoria', () => abrir('/pages/admin/auditoria.xhtml'));

await browser.close();
if (falhas.length) {
  console.log(`Falharam: ${falhas.join(', ')}`);
  process.exit(1);
}
