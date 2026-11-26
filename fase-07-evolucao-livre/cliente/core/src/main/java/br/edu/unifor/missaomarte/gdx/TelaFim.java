package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.online.Resposta;
import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;

/** Resultado da partida, com o fundo 3D, registro no ranking local e opções de continuar. */
public class TelaFim extends ScreenAdapter {
    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final int posicaoRanking;
    private final boolean venceu;
    private final Botao[] botoes = {new Botao("JOGAR NOVAMENTE"), new Botao("MENU PRINCIPAL")};
    private int selecionado;
    private String statusGlobal;
    private Color corGlobal = Ui.CINZA;
    private float tempo;

    public TelaFim(MissaoMarteJogo jogo, PartidaSobrevivencia partida) {
        this.jogo = jogo;
        this.partida = partida;
        this.venceu = partida.estado() == EstadoPartida.VITORIA;
        this.posicaoRanking = jogo.ranking().registrar(jogo.piloto(), partida);
        jogo.sons().tocar(venceu ? "vitoria" : "derrota", 0.8f);
        enviarAoRankingGlobal();
    }

    /** Logado: envia em segundo plano. Convidado ou servidor fora do ar: fica só no ranking local. */
    private void enviarAoRankingGlobal() {
        RegistroRanking r = new RegistroRanking(jogo.piloto(), partida.pontuacao(), partida.dificuldade().toString(),
                (int) partida.tempo(), partida.nivel(), partida.inimigosDerrotados(), partida.passageirosResgatados(),
                venceu, "");
        boolean enviando = jogo.conta().enviar(r, new Resposta<>() {
            @Override
            public void sucesso(Long posicao) {
                statusGlobal = "Ranking global: " + posicao + "º lugar!";
                corGlobal = Color.GOLD;
            }

            @Override
            public void falha(String mensagem) {
                statusGlobal = mensagem + " — salvo só no ranking local";
                corGlobal = new Color(1f, 0.5f, 0.45f, 1f);
            }
        });
        statusGlobal = enviando ? "Enviando ao ranking global..." : "Convidado: entre numa conta para o ranking global";
    }

    @Override
    public void render(float delta) {
        tempo += delta;
        jogo.sons().atualizar(delta);
        jogo.vitrine().desenhar(delta);
        float w = Ui.largura();
        botoes[0].em(w / 2 - 290, 70, 280, 48);
        botoes[1].em(w / 2 + 10, 70, 280, 48);
        lerEntrada(delta);

        ShapeRenderer f = jogo.formas();
        Ui.comecarFormas(f);
        f.setColor(0, 0, 0, 0.45f);
        f.rect(0, 0, w, Ui.ALTURA);
        Ui.painel(f, w / 2 - 320, 150, 640, 420, venceu ? new Color(0.3f, 1f, 0.45f, 1f) : new Color(1f, 0.25f, 0.2f, 1f));
        for (Botao b : botoes) {
            b.desenharFundo(f);
        }
        f.end();

        Fontes fontes = jogo.fontes();
        SpriteBatch b = jogo.batch();
        b.setProjectionMatrix(Ui.projecao());
        b.begin();
        Ui.texto(b, fontes.titulo, venceu ? "MISSÃO CUMPRIDA!" : "NAVE DESTRUÍDA", w / 2, 540, Align.center,
                venceu ? new Color(0.4f, 1f, 0.5f, 1f) : new Color(1f, 0.35f, 0.3f, 1f));
        String[][] linhas = {
            {"Piloto", jogo.piloto()},
            {"Dificuldade", partida.dificuldade().toString()},
            {"Tempo", String.format("%d:%02d", (int) partida.tempo() / 60, (int) partida.tempo() % 60)},
            {"Nível", String.valueOf(partida.nivel())},
            {"Inimigos derrotados", String.valueOf(partida.inimigosDerrotados())},
            {"Passageiros", partida.passageirosResgatados() + " / " + partida.totalPassageiros()},
        };
        float y = 480;
        for (String[] l : linhas) {
            Ui.texto(b, fontes.texto, l[0], w / 2 - 240, y, Align.left, Ui.CINZA);
            Ui.texto(b, fontes.texto, l[1], w / 2 + 240, y, Align.right, Ui.BRANCO_SUAVE);
            y -= 36;
        }
        float pulso = 1f + 0.04f * MathUtils.sin(tempo * 4f);
        fontes.titulo.getData().setScale(0.5f * pulso);
        Ui.texto(b, fontes.titulo, partida.pontuacao() + " PTS", w / 2, 235, Align.center, Color.GOLD);
        fontes.titulo.getData().setScale(0.5f);
        String ranking = posicaoRanking == 1 ? "NOVO RECORDE!" : posicaoRanking > 0 ? posicaoRanking + "º lugar no ranking local" : "";
        Ui.texto(b, fontes.texto, ranking, w / 2, 192, Align.center, posicaoRanking == 1 ? Color.GOLD : Ui.CIANO);
        Ui.texto(b, fontes.textoPequeno, statusGlobal, w / 2, 164, Align.center, corGlobal);
        for (Botao bt : botoes) {
            bt.desenharTexto(b, fontes.texto);
        }
        b.end();
    }

    private void lerEntrada(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selecionado = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selecionado = 1;
        }
        for (int i = 0; i < botoes.length; i++) {
            if (botoes[i].mouseSobre()) {
                selecionado = i;
            }
            botoes[i].atualizar(delta, i == selecionado);
        }
        boolean confirmar = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || (Gdx.input.justTouched() && botoes[selecionado].mouseSobre());
        if (tempo > 0.8f && confirmar) {
            jogo.sons().tocar("clique", 0.7f);
            if (selecionado == 0) {
                jogo.trocarTela(new TelaJogo(jogo, partida.dificuldade()));
            } else {
                jogo.trocarTela(new TelaMenu(jogo));
            }
        }
    }
}
