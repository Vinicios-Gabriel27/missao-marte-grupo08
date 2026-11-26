package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

/** Resultado da partida. */
public class TelaFim extends ScreenAdapter {
    private final MissaoMarteJogo jogo;
    private final PartidaSobrevivencia partida;
    private final Matrix4 projecao = new Matrix4();
    private float tempo;

    public TelaFim(MissaoMarteJogo jogo, PartidaSobrevivencia partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    @Override
    public void render(float delta) {
        tempo += delta;
        boolean venceu = partida.estado() == EstadoPartida.VITORIA;
        ScreenUtils.clear(venceu ? new Color(0.03f, 0.12f, 0.06f, 1f) : new Color(0.15f, 0.02f, 0.02f, 1f));
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        projecao.setToOrtho2D(0, 0, w, h);
        jogo.batch().setProjectionMatrix(projecao);
        jogo.batch().begin();
        Texto.centro(jogo.batch(), jogo.fonte(), venceu ? "MISSÃO CUMPRIDA!" : "NAVE DESTRUÍDA",
                w / 2, h * 0.8f, 3.5f, venceu ? Color.GREEN : Color.RED);
        String[] linhas = {
            "Dificuldade: " + partida.dificuldade(),
            String.format("Tempo: %d:%02d", (int) partida.tempo() / 60, (int) partida.tempo() % 60),
            "Nível alcançado: " + partida.nivel(),
            "Inimigos derrotados: " + partida.inimigosDerrotados(),
            "Passageiros resgatados: " + partida.passageirosResgatados() + "/" + partida.totalPassageiros(),
        };
        for (int i = 0; i < linhas.length; i++) {
            Texto.centro(jogo.batch(), jogo.fonte(), linhas[i], w / 2, h * 0.6f - i * 34, 1.6f, Color.WHITE);
        }
        Texto.centro(jogo.batch(), jogo.fonte(), "PONTUAÇÃO: " + partida.pontuacao(), w / 2, h * 0.25f, 2.6f, Color.YELLOW);
        if (tempo > 1f && (int) (tempo * 2) % 2 == 0) {
            Texto.centro(jogo.batch(), jogo.fonte(), "ENTER para voltar ao menu", w / 2, h * 0.1f, 1.4f, Color.LIGHT_GRAY);
        }
        jogo.batch().end();
        if (tempo > 1f && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched())) {
            jogo.trocarTela(new TelaMenu(jogo));
        }
    }
}
