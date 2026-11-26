package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

/** Botão da interface: realça com mouse ou teclado e anima suavemente. */
class Botao {
    final Rectangle area = new Rectangle();
    String rotulo;
    private float realce;

    Botao(String rotulo) {
        this.rotulo = rotulo;
    }

    Botao em(float x, float y, float w, float h) {
        area.set(x, y, w, h);
        return this;
    }

    void atualizar(float delta, boolean selecionado) {
        realce += ((selecionado ? 1f : 0f) - realce) * Math.min(1f, delta * 12f);
    }

    boolean mouseSobre() {
        return Ui.mouseSobre(area);
    }

    void desenharFundo(ShapeRenderer f) {
        float r = Interpolation.smooth.apply(realce);
        float desloc = 6 * r;
        Color base = new Color(0.1f, 0.06f, 0.07f, 0.72f).lerp(new Color(0.95f, 0.45f, 0.12f, 0.92f), r);
        if (r > 0.02f) {
            Ui.retanguloArredondado(f, area.x - 4 + desloc, area.y - 4, area.width + 8, area.height + 8, 14,
                    new Color(1f, 0.55f, 0.18f, 0.25f * r));
        }
        Ui.retanguloArredondado(f, area.x + desloc, area.y, area.width, area.height, 10, base);
        f.setColor(1f, 0.7f, 0.35f, 0.6f + 0.4f * r);
        f.rect(area.x + desloc, area.y + 8, 4, area.height - 16);
    }

    void desenharTexto(SpriteBatch b, BitmapFont fonte) {
        float desloc = 6 * Interpolation.smooth.apply(realce);
        Color cor = new Color(Ui.CINZA).lerp(Color.WHITE, realce);
        Ui.texto(b, fonte, rotulo, area.x + 26 + desloc, area.y + area.height / 2 + fonte.getCapHeight() / 2, Align.left, cor);
    }
}
