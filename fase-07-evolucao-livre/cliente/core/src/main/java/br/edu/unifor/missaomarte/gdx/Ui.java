package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

/**
 * Ajudantes da interface 2D em resolução virtual (altura 720, largura conforme a proporção da
 * tela). Assim o layout fica igual em qualquer janela ou celular.
 */
final class Ui {
    static final float ALTURA = 720f;
    static final Color LARANJA = new Color(1f, 0.55f, 0.18f, 1f);
    static final Color CIANO = new Color(0.35f, 0.9f, 1f, 1f);
    static final Color FUNDO_PAINEL = new Color(0.07f, 0.04f, 0.05f, 0.78f);
    static final Color BRANCO_SUAVE = new Color(1f, 0.95f, 0.9f, 1f);
    static final Color CINZA = new Color(0.72f, 0.66f, 0.64f, 1f);

    private static final GlyphLayout LAYOUT = new GlyphLayout();
    private static final Matrix4 PROJECAO = new Matrix4();
    private static float largura = 1280f;

    private Ui() {
    }

    /** Atualiza a projeção virtual; chamar no início de cada quadro de interface. */
    static Matrix4 projecao() {
        largura = ALTURA * Gdx.graphics.getWidth() / Math.max(1f, Gdx.graphics.getHeight());
        return PROJECAO.setToOrtho2D(0, 0, largura, ALTURA);
    }

    static float largura() {
        return largura;
    }

    static float mouseX() {
        return Gdx.input.getX() * largura / Math.max(1f, Gdx.graphics.getWidth());
    }

    static float mouseY() {
        return ALTURA - Gdx.input.getY() * ALTURA / Math.max(1f, Gdx.graphics.getHeight());
    }

    static boolean mouseSobre(Rectangle r) {
        return r.contains(mouseX(), mouseY());
    }

    static float texto(SpriteBatch b, BitmapFont f, String s, float x, float y, int alinhamento, Color cor) {
        LAYOUT.setText(f, s, cor, 0, alinhamento, false);
        f.draw(b, LAYOUT, x, y);
        return LAYOUT.width;
    }

    /** Texto com quebra de linha automática dentro de {@code largura}. Retorna a altura usada. */
    static float textoQuebrado(SpriteBatch b, BitmapFont f, String s, float x, float y, float largura,
                               int alinhamento, Color cor) {
        LAYOUT.setText(f, s, cor, largura, alinhamento, true);
        f.draw(b, LAYOUT, x, y);
        return LAYOUT.height;
    }

    /** Texto numa linha só; se não couber em {@code largura}, diminui a fonte até caber. */
    static void textoAjustado(SpriteBatch b, BitmapFont f, String s, float x, float y, float largura,
                              int alinhamento, Color cor) {
        float escala = f.getData().scaleX;
        LAYOUT.setText(f, s);
        if (LAYOUT.width > largura) {
            f.getData().setScale(escala * largura / LAYOUT.width);
        }
        texto(b, f, s, x, y, alinhamento, cor);
        f.getData().setScale(escala);
    }

    static float larguraTexto(BitmapFont f, String s) {
        LAYOUT.setText(f, s);
        return LAYOUT.width;
    }

    static void comecarFormas(ShapeRenderer f) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        f.setProjectionMatrix(projecao());
        f.begin(ShapeRenderer.ShapeType.Filled);
    }

    /** Retângulo com cantos arredondados (preenchido). */
    static void retanguloArredondado(ShapeRenderer f, float x, float y, float w, float h, float r, Color cor) {
        f.setColor(cor);
        r = Math.min(r, Math.min(w, h) / 2f);
        f.rect(x + r, y, w - 2 * r, h);
        f.rect(x, y + r, r, h - 2 * r);
        f.rect(x + w - r, y + r, r, h - 2 * r);
        f.arc(x + r, y + r, r, 180, 90, 8);
        f.arc(x + w - r, y + r, r, 270, 90, 8);
        f.arc(x + w - r, y + h - r, r, 0, 90, 8);
        f.arc(x + r, y + h - r, r, 90, 90, 8);
    }

    /** Painel escuro translúcido com faixa de destaque no topo. */
    static void painel(ShapeRenderer f, float x, float y, float w, float h, Color destaque) {
        retanguloArredondado(f, x - 3, y - 3, w + 6, h + 6, 16, new Color(destaque.r, destaque.g, destaque.b, 0.18f));
        retanguloArredondado(f, x, y, w, h, 14, FUNDO_PAINEL);
        f.setColor(destaque);
        f.rect(x + 18, y + h - 4, w - 36, 3);
    }

    /** Barra de progresso com fundo, preenchimento e brilho. */
    static void barra(ShapeRenderer f, float x, float y, float w, float h, float fracao, Color cor) {
        fracao = Math.max(0, Math.min(1, fracao));
        retanguloArredondado(f, x - 2, y - 2, w + 4, h + 4, h / 2 + 2, new Color(0, 0, 0, 0.55f));
        if (fracao > 0) {
            retanguloArredondado(f, x, y, Math.max(h, w * fracao), h, h / 2, cor);
            f.setColor(1, 1, 1, 0.22f);
            f.rect(x + h / 2, y + h * 0.55f, Math.max(0, w * fracao - h), h * 0.25f);
        }
    }
}
