package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

/** Ajudante para escrever texto com tamanho e alinhamento (a fonte padrão é pequena). */
final class Texto {
    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private Texto() {
    }

    static void escrever(SpriteBatch batch, BitmapFont fonte, String texto, float x, float y,
                         float escala, Color cor, int alinhamento) {
        fonte.getData().setScale(escala);
        fonte.setColor(cor);
        LAYOUT.setText(fonte, texto, cor, 0, alinhamento, false);
        fonte.draw(batch, LAYOUT, x, y);
        fonte.getData().setScale(1f);
    }

    static void centro(SpriteBatch batch, BitmapFont fonte, String texto, float x, float y, float escala, Color cor) {
        escrever(batch, fonte, texto, x, y, escala, cor, Align.center);
    }
}
