package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

/** Campo de texto simples (rótulo, valor, foco, máscara de senha). */
class CampoTexto {
    final Rectangle area = new Rectangle();
    final String rotulo;
    final int limite;
    final boolean senha;
    String valor;

    CampoTexto(String rotulo, String valor, int limite, boolean senha) {
        this.rotulo = rotulo;
        this.valor = valor;
        this.limite = limite;
        this.senha = senha;
    }

    void digitar(char c) {
        if (c == '\b') {
            if (!valor.isEmpty()) {
                valor = valor.substring(0, valor.length() - 1);
            }
        } else if (c >= 32 && c != 127 && valor.length() < limite) {
            valor += c;
        }
    }

    void desenharFundo(ShapeRenderer f, boolean foco) {
        Ui.retanguloArredondado(f, area.x, area.y, area.width, area.height, 8,
                foco ? new Color(0.18f, 0.1f, 0.08f, 0.95f) : new Color(0.05f, 0.03f, 0.03f, 0.85f));
        f.setColor(foco ? Ui.LARANJA : new Color(0.4f, 0.3f, 0.28f, 1f));
        f.rect(area.x + 8, area.y, area.width - 16, 2);
    }

    void desenharTexto(SpriteBatch b, Fontes fo, boolean foco, boolean cursorVisivel) {
        BitmapFont f = fo.texto;
        Ui.texto(b, fo.textoPequeno, rotulo, area.x, area.y + area.height + 22, Align.left, Ui.CINZA);
        String mostrado = senha ? "•".repeat(valor.length()) : valor;
        Ui.texto(b, f, mostrado + (foco && cursorVisivel ? "_" : ""), area.x + 14,
                area.y + area.height / 2 + f.getCapHeight() / 2, Align.left, Ui.BRANCO_SUAVE);
    }
}
