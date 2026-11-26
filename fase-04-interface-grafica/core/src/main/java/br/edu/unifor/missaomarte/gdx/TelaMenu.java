package br.edu.unifor.missaomarte.gdx;

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
import java.util.List;

/** Menu principal com fundo 3D animado, botões (mouse/teclado/toque), ranking e instruções. */
public class TelaMenu extends ScreenAdapter {
    private enum Painel { PRINCIPAL, RANKING, COMO_JOGAR }

    private static final int JOGAR = 0;
    private static final int DIFICULDADE = 1;
    private static final int PILOTO = 2;
    private static final int RANKING = 3;
    private static final int COMO_JOGAR = 4;
    private static final int SOM = 5;
    private static final int SAIR = 6;

    private final MissaoMarteJogo jogo;
    private final Botao[] botoes = {
        new Botao("JOGAR"), new Botao(""), new Botao(""), new Botao("RANKING"),
        new Botao("COMO JOGAR"), new Botao(""), new Botao("SAIR")
    };
    private final Botao voltar = new Botao("VOLTAR");
    private Painel painel = Painel.PRINCIPAL;
    private int selecionado;
    private int dificuldade = 1;
    private boolean editandoNome;
    private float tempo;
    private final InputAdapter entradaTexto = new InputAdapter() {
        @Override
        public boolean keyTyped(char c) {
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
    }

    @Override
    public void show() {
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
        if (MissaoMarteJogo.modoDemonstracao() && tempo > 3.5f) {
            jogo.salvarCaptura("captura-menu.png");
            iniciar();
        }
    }

    private void lerEntrada(float delta) {
        if (painel != Painel.PRINCIPAL) {
            voltar.atualizar(delta, true);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || (Gdx.input.justTouched() && voltar.mouseSobre())) {
                jogo.sons().tocar("clique", 0.6f);
                painel = Painel.PRINCIPAL;
            }
            return;
        }
        if (editandoNome) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                    || (Gdx.input.justTouched() && !botoes[PILOTO].mouseSobre())) {
                editandoNome = false;
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
            case PILOTO -> editandoNome = true;
            case RANKING -> painel = Painel.RANKING;
            case COMO_JOGAR -> painel = Painel.COMO_JOGAR;
            case SOM -> jogo.sons().alternarMudo();
            case SAIR -> Gdx.app.exit();
            default -> { }
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

        float w = Ui.largura();
        float x0 = 70;
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].em(x0, 420 - i * 56, 360, 46);
        }
        voltar.em(w / 2 - 100, 70, 200, 46);

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
        } else {
            Ui.painel(f, w / 2 - 330, 140, 660, 470, Ui.LARANJA);
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
        } else {
            desenharComoJogar(b, fontes, w);
        }
        if (painel != Painel.PRINCIPAL) {
            voltar.desenharTexto(b, fontes.texto);
        }
        b.end();
    }

    private void desenharRanking(SpriteBatch b, Fontes fontes, float w) {
        Ui.texto(b, fontes.titulo, "RANKING LOCAL", w / 2, 585, Align.center, Ui.LARANJA);
        List<RegistroRanking> top = jogo.ranking().top();
        if (top.isEmpty()) {
            Ui.texto(b, fontes.texto, "Nenhuma partida ainda. Seja o primeiro!", w / 2, 380, Align.center, Ui.CINZA);
            return;
        }
        // colunas com posição fixa: cada valor fica embaixo do seu título
        float esq = w / 2 - 300;
        float[] colunas = {w / 2 + 50, w / 2 + 135, w / 2 + 200, w / 2 + 222};
        int[] alinhar = {Align.right, Align.right, Align.right, Align.left};
        String[] titulos = {"PONTOS", "TEMPO", "NÍVEL", "DIFICULDADE"};
        float y = 525;
        Ui.texto(b, fontes.textoPequeno, "#", esq, y, Align.left, Ui.CINZA);
        Ui.texto(b, fontes.textoPequeno, "PILOTO", esq + 38, y, Align.left, Ui.CINZA);
        for (int c = 0; c < titulos.length; c++) {
            Ui.texto(b, fontes.textoPequeno, titulos[c], colunas[c], y, alinhar[c], Ui.CINZA);
        }
        for (int i = 0; i < top.size(); i++) {
            RegistroRanking r = top.get(i);
            y -= 34;
            Color cor = i == 0 ? Color.GOLD : i < 3 ? Ui.BRANCO_SUAVE : Ui.CINZA;
            Ui.texto(b, fontes.texto, (i + 1) + ".", esq, y, Align.left, cor);
            Ui.textoAjustado(b, fontes.texto, r.piloto() + (r.venceu() ? "  [V]" : ""), esq + 38, y, 220, Align.left, cor);
            String[] valores = {String.valueOf(r.pontuacao()),
                String.format("%d:%02d", r.tempoSegundos() / 60, r.tempoSegundos() % 60),
                String.valueOf(r.nivel()), r.dificuldade()};
            for (int c = 0; c < valores.length; c++) {
                Ui.texto(b, fontes.texto, valores[c], colunas[c], y, alinhar[c], cor);
            }
        }
    }

    private void desenharComoJogar(SpriteBatch b, Fontes fontes, float w) {
        Ui.texto(b, fontes.titulo, "COMO JOGAR", w / 2, 585, Align.center, Ui.LARANJA);
        String[][] linhas = {
            {"Mover", "WASD / setas  (celular: joystick)"},
            {"Atirar", "automático no inimigo mais próximo"},
            {"Subir de nível", "colete cristais verdes e escolha 1 de 3 upgrades"},
            {"Professor", "+25% de XP"},
            {"Engenheiro", "+1 de vida por segundo"},
            {"Astronauta", "+20% de dano"},
            {"Vencer", "sobreviva 5 min, resgate os 5 passageiros"},
            {"", "e pouse na plataforma vencendo o chefe e a horda"},
            {"Pausar / som", "P ou Esc  /  M"},
        };
        float y = 520;
        for (String[] l : linhas) {
            Ui.texto(b, fontes.texto, l[0], w / 2 - 290, y, Align.left, Ui.CIANO);
            Ui.texto(b, fontes.textoPequeno, l[1], w / 2 - 90, y - 2, Align.left, Ui.BRANCO_SUAVE);
            y -= 38;
        }
    }

    @Override
    public void resize(int width, int height) {
        jogo.mundo().redimensionar(width, height);
    }
}
