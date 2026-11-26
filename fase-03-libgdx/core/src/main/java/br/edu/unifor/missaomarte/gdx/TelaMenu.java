package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Dificuldade;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

/** Menu inicial: escolhe a dificuldade e começa a partida. */
public class TelaMenu extends ScreenAdapter {
    private static final Color FUNDO = new Color(0.16f, 0.05f, 0.04f, 1f);

    private final MissaoMarteJogo jogo;
    private final Matrix4 projecao = new Matrix4();
    private int selecionada = 1;
    private float tempo;

    public TelaMenu(MissaoMarteJogo jogo) {
        this.jogo = jogo;
    }

    @Override
    public void render(float delta) {
        tempo += delta;
        lerEntrada();
        ScreenUtils.clear(FUNDO);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        projecao.setToOrtho2D(0, 0, w, h);
        jogo.batch().setProjectionMatrix(projecao);
        jogo.batch().begin();
        Texto.centro(jogo.batch(), jogo.fonte(), "MISSÃO MARTE", w / 2, h * 0.8f, 4f, Color.ORANGE);
        Texto.centro(jogo.batch(), jogo.fonte(), "SOBREVIVÊNCIA", w / 2, h * 0.8f - 60, 2.2f, Color.WHITE);
        Texto.centro(jogo.batch(), jogo.fonte(), "Resgate os passageiros, sobreviva 5 minutos e volte para a plataforma",
                w / 2, h * 0.6f, 1.3f, Color.LIGHT_GRAY);
        Dificuldade[] niveis = Dificuldade.values();
        for (int i = 0; i < niveis.length; i++) {
            boolean ativo = i == selecionada;
            String rotulo = (ativo ? "> " : "  ") + (i + 1) + ". " + niveis[i] + (ativo ? " <" : "  ");
            Texto.centro(jogo.batch(), jogo.fonte(), rotulo, w / 2, h * 0.45f - i * 40, 1.8f,
                    ativo ? Color.YELLOW : Color.GRAY);
        }
        if ((int) (tempo * 2) % 2 == 0) {
            Texto.centro(jogo.batch(), jogo.fonte(), "ENTER para decolar", w / 2, h * 0.18f, 1.6f, Color.WHITE);
        }
        Texto.centro(jogo.batch(), jogo.fonte(), "WASD/setas: mover   |   tiro automático   |   1/2/3: escolher upgrade   |   P: pausar",
                w / 2, 40, 1.1f, Color.GRAY);
        jogo.batch().end();
    }

    private void lerEntrada() {
        Dificuldade[] niveis = Dificuldade.values();
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selecionada = (selecionada + niveis.length - 1) % niveis.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selecionada = (selecionada + 1) % niveis.length;
        }
        for (int i = 0; i < niveis.length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                selecionada = i;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.justTouched()) {
            jogo.trocarTela(new TelaJogo(jogo, niveis[selecionada]));
        }
    }
}
