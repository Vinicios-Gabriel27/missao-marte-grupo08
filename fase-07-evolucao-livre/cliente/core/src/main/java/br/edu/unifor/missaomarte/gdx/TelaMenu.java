package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.online.Resposta;
import br.edu.unifor.missaomarte.online.Sessao;
import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import br.edu.unifor.missaomarte.sobrevivencia.Dificuldade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;

/** Menu principal com fundo 3D animado, botões (mouse/teclado/toque), ranking e instruções. */
public class TelaMenu extends ScreenAdapter {
    private enum Painel { PRINCIPAL, RANKING, COMO_JOGAR, CONTA }

    private static final int JOGAR = 0;
    private static final int DIFICULDADE = 1;
    private static final int CONTA = 2;
    private static final int PILOTO = 3;
    private static final int RANKING = 4;
    private static final int COMO_JOGAR = 5;
    private static final int SOM = 6;
    private static final int SAIR = 7;

    private final MissaoMarteJogo jogo;
    private final Botao[] botoes = {
        new Botao("JOGAR"), new Botao(""), new Botao(""), new Botao(""), new Botao("RANKING"),
        new Botao("COMO JOGAR"), new Botao(""), new Botao("SAIR")
    };
    // painel de conta
    private final CampoTexto campoNome = new CampoTexto("Nome do jogador", "", 16, false);
    private final CampoTexto campoSenha = new CampoTexto("Senha (mínimo 6)", "", 40, true);
    private final CampoTexto campoServidor;
    private final CampoTexto[] campos;
    private int campoFoco;
    private final Botao entrar = new Botao("ENTRAR");
    private final Botao criarConta = new Botao("CRIAR CONTA");
    private final Botao sairConta = new Botao("SAIR DA CONTA");
    private String statusConta = "";
    private Color corStatus = Ui.CINZA;
    private boolean aguardando;
    // ranking global
    private boolean abaGlobal;
    private List<RegistroRanking> rankingGlobal = new ArrayList<>();
    private String statusGlobal = "";
    private final Botao voltar = new Botao("VOLTAR");
    private Painel painel = Painel.PRINCIPAL;
    private int selecionado;
    private int dificuldade = 1;
    private boolean editandoNome;
    private float tempo;
    private final InputAdapter entradaTexto = new InputAdapter() {
        @Override
        public boolean keyTyped(char c) {
            if (painel == Painel.CONTA) {
                if (c == '\t') {
                    campoFoco = (campoFoco + 1) % campos.length;
                } else if (c != '\r' && c != '\n') {
                    campos[campoFoco].digitar(c);
                }
                return true;
            }
            if (!editandoNome) {
                return false;
            }
            String nome = jogo.piloto();
            if (c == '\b' && !nome.isEmpty()) {
                jogo.definirPiloto(nome.substring(0, nome.length() - 1));
            } else if ((Character.isLetterOrDigit(c) || c == ' ') && nome.length() < 16) {
                jogo.definirPiloto(nome + c);
            }
            return true;
        }
    };

    public TelaMenu(MissaoMarteJogo jogo) {
        this.jogo = jogo;
        dificuldade = jogo.dificuldadePreferida();
        campoServidor = new CampoTexto("Servidor", jogo.servidor(), 60, false);
        campos = new CampoTexto[] {campoNome, campoSenha, campoServidor};
        jogo.conta().nome().ifPresent(n -> campoNome.valor = n);
    }

    @Override
    public void show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(entradaTexto);
        jogo.sons().tocarMusica();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        tempo += delta;
        jogo.sons().atualizar(delta);
        jogo.vitrine().desenhar(delta);
        lerEntrada(delta);
        desenharInterface();
        if (MissaoMarteJogo.modoDemonstracao()) {
            demonstracao();
        }
    }

    private int etapaDemo;

    /** CI: captura o menu, cria conta no servidor, mostra o ranking global e começa a partida. */
    private void demonstracao() {
        if (etapaDemo == 0 && tempo > 3f) {
            jogo.salvarCaptura("captura-menu.png");
            campoNome.valor = "Demo" + (System.currentTimeMillis() % 10000);
            campoSenha.valor = "senha123";
            painel = Painel.CONTA;
            acaoConta(true);
            etapaDemo = 1;
        } else if (etapaDemo == 1 && tempo > 5f) {
            jogo.salvarCaptura("captura-conta.png");
            abrirRanking();
            abaGlobal = true;
            carregarGlobal();
            etapaDemo = 2;
        } else if (etapaDemo == 2 && tempo > 8f) {
            jogo.salvarCaptura("captura-ranking-global.png");
            etapaDemo = 3;
            iniciar();
        }
    }

    private void lerEntrada(float delta) {
        if (painel == Painel.CONTA) {
            lerEntradaConta(delta);
            return;
        }
        if (painel == Painel.RANKING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                    || (Gdx.input.justTouched() && Ui.mouseY() > 560 && Ui.mouseY() < 610)) {
                abaGlobal = Gdx.input.justTouched() ? Ui.mouseX() > Ui.largura() / 2 : !abaGlobal;
                if (abaGlobal) {
                    carregarGlobal();
                }
                jogo.sons().tocar("clique", 0.5f);
            }
        }
        if (painel != Painel.PRINCIPAL) {
            voltar.atualizar(delta, true);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.BACK) || (Gdx.input.justTouched() && voltar.mouseSobre())) {
                jogo.sons().tocar("clique", 0.6f);
                painel = Painel.PRINCIPAL;
            }
            return;
        }
        if (editandoNome) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                    || (Gdx.input.justTouched() && !botoes[PILOTO].mouseSobre())) {
                editandoNome = false;
                Gdx.input.setOnscreenKeyboardVisible(false);
                jogo.sons().tocar("clique", 0.6f);
            }
            atualizarBotoes(delta);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selecionado = (selecionado + botoes.length - 1) % botoes.length;
            jogo.sons().tocar("clique", 0.4f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selecionado = (selecionado + 1) % botoes.length;
            jogo.sons().tocar("clique", 0.4f);
        }
        if (selecionado == DIFICULDADE && (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A))) {
            mudarDificuldade(-1);
        }
        if (selecionado == DIFICULDADE && (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D))) {
            mudarDificuldade(1);
        }
        for (int i = 0; i < botoes.length; i++) {
            if (botoes[i].mouseSobre() && (Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0)) {
                selecionado = i;
            }
        }
        boolean clicou = Gdx.input.justTouched() && botoes[selecionado].mouseSobre();
        if (clicou || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            acionar(selecionado);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            jogo.sons().alternarMudo();
        }
        atualizarBotoes(delta);
    }

    private void atualizarBotoes(float delta) {
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].atualizar(delta, i == selecionado || (i == PILOTO && editandoNome));
        }
    }

    private void acionar(int indice) {
        jogo.sons().tocar("clique", 0.7f);
        switch (indice) {
            case JOGAR -> iniciar();
            case DIFICULDADE -> mudarDificuldade(1);
            case PILOTO -> {
                editandoNome = true;
                Gdx.input.setOnscreenKeyboardVisible(true);
            }
            case RANKING -> abrirRanking();
            case CONTA -> {
                painel = Painel.CONTA;
                statusConta = jogo.conta().logado() ? "Conectado como " + jogo.conta().nome().orElse("") : "";
                corStatus = Ui.CIANO;
            }
            case COMO_JOGAR -> painel = Painel.COMO_JOGAR;
            case SOM -> jogo.sons().alternarMudo();
            case SAIR -> Gdx.app.exit();
            default -> { }
        }
    }

    private void abrirRanking() {
        painel = Painel.RANKING;
        if (abaGlobal) {
            carregarGlobal();
        }
    }

    private void carregarGlobal() {
        statusGlobal = "Carregando ranking global...";
        jogo.conta().servico().rankingGlobal(new Resposta<>() {
            @Override
            public void sucesso(List<RegistroRanking> lista) {
                rankingGlobal = lista;
                statusGlobal = lista.isEmpty() ? "Ninguém no ranking global ainda." : "";
            }

            @Override
            public void falha(String mensagem) {
                rankingGlobal = new ArrayList<>();
                statusGlobal = mensagem;
            }
        });
    }

    private void lerEntradaConta(float delta) {
        float w = Ui.largura();
        float x = w / 2 - 230;
        campoNome.area.set(x, 440, 460, 44);
        campoSenha.area.set(x, 352, 460, 44);
        campoServidor.area.set(x, 264, 460, 44);
        boolean logado = jogo.conta().logado();
        entrar.em(x, 180, 220, 46);
        criarConta.em(x + 240, 180, 220, 46);
        sairConta.em(x, 180, 460, 46);
        voltar.em(w / 2 - 100, 92, 200, 44);
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < campos.length; i++) {
                if (Ui.mouseSobre(campos[i].area)) {
                    campoFoco = i;
                    Gdx.input.setOnscreenKeyboardVisible(true);
                }
            }
        }
        boolean enter = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        if (!aguardando && !logado && ((Gdx.input.justTouched() && entrar.mouseSobre()) || (enter && campoFoco < 2))) {
            acaoConta(false);
        } else if (!aguardando && !logado && Gdx.input.justTouched() && criarConta.mouseSobre()) {
            acaoConta(true);
        } else if (logado && Gdx.input.justTouched() && sairConta.mouseSobre()) {
            jogo.conta().sair();
            statusConta = "Você está jogando como convidado.";
            corStatus = Ui.CINZA;
            jogo.sons().tocar("clique", 0.6f);
        }
        entrar.atualizar(delta, entrar.mouseSobre());
        criarConta.atualizar(delta, criarConta.mouseSobre());
        sairConta.atualizar(delta, sairConta.mouseSobre());
        voltar.atualizar(delta, voltar.mouseSobre());
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)
                || (Gdx.input.justTouched() && voltar.mouseSobre())) {
            Gdx.input.setOnscreenKeyboardVisible(false);
            jogo.definirServidor(campoServidor.valor);
            painel = Painel.PRINCIPAL;
            jogo.sons().tocar("clique", 0.6f);
        }
    }

    private void acaoConta(boolean cadastrar) {
        jogo.definirServidor(campoServidor.valor);
        aguardando = true;
        statusConta = cadastrar ? "Criando conta..." : "Entrando...";
        corStatus = Ui.CINZA;
        Resposta<Sessao> r = new Resposta<>() {
            @Override
            public void sucesso(Sessao s) {
                aguardando = false;
                campoSenha.valor = "";
                statusConta = "Conectado como " + s.nome() + ". Suas partidas vão para o ranking global!";
                corStatus = new Color(0.4f, 1f, 0.5f, 1f);
                jogo.sons().tocar("resgate", 0.6f);
            }

            @Override
            public void falha(String mensagem) {
                aguardando = false;
                statusConta = mensagem;
                corStatus = new Color(1f, 0.45f, 0.4f, 1f);
                jogo.sons().tocar("dano", 0.4f);
            }
        };
        if (cadastrar) {
            jogo.conta().cadastrarEEntrar(campoNome.valor, campoSenha.valor, r);
        } else {
            jogo.conta().entrar(campoNome.valor, campoSenha.valor, r);
        }
    }

    private void mudarDificuldade(int passo) {
        dificuldade = (dificuldade + passo + 3) % 3;
        jogo.definirDificuldadePreferida(dificuldade);
        jogo.sons().tocar("clique", 0.5f);
    }

    private void iniciar() {
        jogo.trocarTela(new TelaJogo(jogo, Dificuldade.values()[dificuldade]));
    }

    private void desenharInterface() {
        Fontes fontes = jogo.fontes();
        Dificuldade d = Dificuldade.values()[dificuldade];
        String cursor = editandoNome && (int) (tempo * 2.5f) % 2 == 0 ? "_" : "";
        botoes[DIFICULDADE].rotulo = "DIFICULDADE:  < " + d.toString().toUpperCase() + " >";
        botoes[PILOTO].rotulo = "PILOTO:  " + jogo.piloto() + cursor;
        botoes[SOM].rotulo = "SOM:  " + (jogo.sons().mudo() ? "DESLIGADO" : "LIGADO");
        botoes[CONTA].rotulo = "CONTA:  " + jogo.conta().nome().orElse("convidado (entrar)");
        botoes[PILOTO].rotulo = jogo.conta().logado() ? "APELIDO LOCAL:  " + jogo.piloto() : botoes[PILOTO].rotulo;

        float w = Ui.largura();
        float x0 = 70;
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].em(x0, 440 - i * 50, 380, 42);
        }
        if (painel != Painel.CONTA) {
            voltar.em(w / 2 - 100, 70, 200, 46);
        }

        ShapeRenderer f = jogo.formas();
        Ui.comecarFormas(f);
        // faixa escura lateral para legibilidade
        for (int i = 0; i < 24; i++) {
            f.setColor(0, 0, 0, 0.55f * (1 - i / 24f));
            f.rect(i * 22, 0, 22, Ui.ALTURA);
        }
        if (painel == Painel.PRINCIPAL) {
            for (Botao b : botoes) {
                b.desenharFundo(f);
            }
        } else if (painel == Painel.CONTA) {
            Ui.painel(f, w / 2 - 280, 80, 560, 540, Ui.CIANO);
            boolean logado = jogo.conta().logado();
            for (int i = 0; i < campos.length; i++) {
                campos[i].desenharFundo(f, i == campoFoco);
            }
            if (logado) {
                sairConta.desenharFundo(f);
            } else {
                entrar.desenharFundo(f);
                criarConta.desenharFundo(f);
            }
            voltar.desenharFundo(f);
        } else {
            Ui.painel(f, w / 2 - 330, 140, 660, 470, Ui.LARANJA);
            if (painel == Painel.RANKING) {
                Color ativa = new Color(1f, 0.55f, 0.18f, 0.9f);
                Color inativa = new Color(0.2f, 0.12f, 0.1f, 0.8f);
                Ui.retanguloArredondado(f, w / 2 - 250, 560, 240, 40, 8, abaGlobal ? inativa : ativa);
                Ui.retanguloArredondado(f, w / 2 + 10, 560, 240, 40, 8, abaGlobal ? ativa : inativa);
            }
            voltar.desenharFundo(f);
        }
        f.end();

        SpriteBatch b = jogo.batch();
        b.setProjectionMatrix(Ui.projecao());
        b.begin();
        if (painel == Painel.PRINCIPAL) {
            float brilho = 0.85f + 0.15f * MathUtils.sin(tempo * 2f);
            Ui.texto(b, fontes.tituloGigante, "MISSÃO", x0, 655, Align.left, new Color(1f, 0.6f * brilho, 0.2f, 1f));
            Ui.texto(b, fontes.tituloGigante, "MARTE", x0, 575, Align.left, Ui.BRANCO_SUAVE);
            Ui.texto(b, fontes.subtitulo, "S O B R E V I V Ê N C I A", x0 + 4, 497, Align.left, Ui.CIANO);
            for (Botao bt : botoes) {
                bt.desenharTexto(b, fontes.texto);
            }
            int recorde = jogo.ranking().recorde();
            if (recorde > 0) {
                Ui.texto(b, fontes.textoPequeno, "Recorde: " + recorde + " pts", x0 + 4, 26, Align.left, Ui.CINZA);
            }
            Ui.texto(b, fontes.textoPequeno, "UNIFOR  •  T200  •  Grupo 08", w - 24, 26, Align.right, Ui.CINZA);
        } else if (painel == Painel.RANKING) {
            desenharRanking(b, fontes, w);
        } else if (painel == Painel.CONTA) {
            Ui.texto(b, fontes.titulo, "CONTA ONLINE", w / 2, 590, Align.center, Ui.CIANO);
            Ui.texto(b, fontes.textoPequeno, "Sem conta você joga como convidado (ranking local).", w / 2, 540,
                    Align.center, Ui.CINZA);
            boolean piscar = (int) (tempo * 2.5f) % 2 == 0;
            for (int i = 0; i < campos.length; i++) {
                campos[i].desenharTexto(b, fontes, i == campoFoco, piscar);
            }
            if (jogo.conta().logado()) {
                sairConta.desenharTexto(b, fontes.texto);
            } else {
                entrar.desenharTexto(b, fontes.texto);
                criarConta.desenharTexto(b, fontes.texto);
            }
            Ui.texto(b, fontes.textoPequeno, statusConta, w / 2, 168, Align.center, corStatus);
        } else {
            desenharComoJogar(b, fontes, w);
        }
        if (painel != Painel.PRINCIPAL) {
            voltar.desenharTexto(b, fontes.texto);
        }
        b.end();
    }

    private void desenharRanking(SpriteBatch b, Fontes fontes, float w) {
        Ui.texto(b, fontes.texto, "LOCAL", w / 2 - 130, 589, Align.center, abaGlobal ? Ui.CINZA : Color.WHITE);
        Ui.texto(b, fontes.texto, "GLOBAL", w / 2 + 130, 589, Align.center, abaGlobal ? Color.WHITE : Ui.CINZA);
        List<RegistroRanking> top = abaGlobal ? rankingGlobal : jogo.ranking().top();
        if (abaGlobal && !statusGlobal.isEmpty()) {
            Ui.texto(b, fontes.texto, statusGlobal, w / 2, 380, Align.center, Ui.CINZA);
            return;
        }
        if (top.isEmpty()) {
            Ui.texto(b, fontes.texto, "Nenhuma partida ainda. Seja o primeiro!", w / 2, 380, Align.center, Ui.CINZA);
            return;
        }
        // colunas com posição fixa: cada valor fica embaixo do seu título
        float esq = w / 2 - 300;
        float[] colunas = {w / 2 + 70, w / 2 + 150, w / 2 + 215, w / 2 + 300};
        String[] titulos = {"PONTOS", "TEMPO", "NÍVEL", "DIFICULDADE"};
        float y = 525;
        Ui.texto(b, fontes.textoPequeno, "#", esq, y, Align.left, Ui.CINZA);
        Ui.texto(b, fontes.textoPequeno, "PILOTO", esq + 38, y, Align.left, Ui.CINZA);
        for (int c = 0; c < titulos.length; c++) {
            Ui.texto(b, fontes.textoPequeno, titulos[c], colunas[c], y, Align.right, Ui.CINZA);
        }
        for (int i = 0; i < top.size(); i++) {
            RegistroRanking r = top.get(i);
            y -= 34;
            Color cor = i == 0 ? Color.GOLD : i < 3 ? Ui.BRANCO_SUAVE : Ui.CINZA;
            Ui.texto(b, fontes.texto, (i + 1) + ".", esq, y, Align.left, cor);
            Ui.textoAjustado(b, fontes.texto, r.piloto() + (r.venceu() ? "  [V]" : ""), esq + 38, y, 250, Align.left, cor);
            String[] valores = {String.valueOf(r.pontuacao()),
                String.format("%d:%02d", r.tempoSegundos() / 60, r.tempoSegundos() % 60),
                String.valueOf(r.nivel()), r.dificuldade()};
            for (int c = 0; c < valores.length; c++) {
                Ui.texto(b, fontes.texto, valores[c], colunas[c], y, Align.right, cor);
            }
        }
    }

    private void desenharComoJogar(SpriteBatch b, Fontes fontes, float w) {
        Ui.texto(b, fontes.titulo, "COMO JOGAR", w / 2, 585, Align.center, Ui.LARANJA);
        String[][] linhas = {
            {"Mover", "WASD / setas  (celular: joystick)"},
            {"Atirar", "automático no inimigo mais próximo"},
            {"Investida", "Espaço/Shift (celular: botão DASH) — atravessa a horda"},
            {"Subir de nível", "colete cristais verdes e escolha 1 de 3 upgrades"},
            {"Professor", "+25% de XP"},
            {"Engenheiro", "+1 de vida por segundo"},
            {"Astronauta", "+20% de dano"},
            {"Vencer", "sobreviva 5 min, resgate os 5 passageiros"},
            {"", "e pouse na plataforma vencendo o chefe e a horda"},
        };
        float y = 525;
        for (String[] l : linhas) {
            Ui.texto(b, fontes.texto, l[0], w / 2 - 290, y, Align.left, Ui.CIANO);
            Ui.texto(b, fontes.textoPequeno, l[1], w / 2 - 90, y - 2, Align.left, Ui.BRANCO_SUAVE);
            y -= 37;
        }
    }

    @Override
    public void resize(int width, int height) {
        jogo.mundo().redimensionar(width, height);
    }
}
