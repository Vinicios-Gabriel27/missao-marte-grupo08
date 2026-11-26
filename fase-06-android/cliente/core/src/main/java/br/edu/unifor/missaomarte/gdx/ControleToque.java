package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;

/**
 * Joystick virtual: o dedo que toca na metade esquerda da tela vira o centro do joystick e
 * arrastar define a direção (com zona morta e intensidade proporcional). Suporta vários
 * dedos: um no joystick e outro tocando nos cartões de upgrade ou no botão de pausa.
 */
class ControleToque extends InputAdapter {
    static final float RAIO = 80f;
    private static final float ZONA_MORTA = 0.18f;

    final Rectangle botaoPausa = new Rectangle();
    private int dedo = -1;
    private float centroX;
    private float centroY;
    private float atualX;
    private float atualY;
    private boolean pausaTocada;

    static boolean telaDeToque() {
        return Gdx.app.getType() == Application.ApplicationType.Android
                || Gdx.app.getType() == Application.ApplicationType.iOS;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        float vx = virtualX(x);
        float vy = virtualY(y);
        if (botaoPausa.contains(vx, vy)) {
            pausaTocada = true;
            return true;
        }
        if (dedo == -1 && vx < Ui.largura() * 0.55f && vy < Ui.ALTURA * 0.8f) {
            dedo = pointer;
            centroX = atualX = vx;
            centroY = atualY = vy;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (pointer == dedo) {
            atualX = virtualX(x);
            atualY = virtualY(y);
            // o centro "segue" o dedo se ele passar muito do raio
            float dx = atualX - centroX;
            float dy = atualY - centroY;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d > RAIO * 1.4f) {
                centroX = atualX - dx / d * RAIO * 1.4f;
                centroY = atualY - dy / d * RAIO * 1.4f;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (pointer == dedo) {
            dedo = -1;
            return true;
        }
        return false;
    }

    /** Direção atual (zero se não há dedo no joystick). */
    Vetor direcao() {
        if (dedo == -1) {
            return Vetor.ZERO;
        }
        Vetor v = new Vetor(atualX - centroX, atualY - centroY).vezes(1f / RAIO);
        float c = v.comprimento();
        if (c < ZONA_MORTA) {
            return Vetor.ZERO;
        }
        return c > 1f ? v.normalizado() : v;
    }

    /** Verdadeiro uma vez por toque no botão de pausa. */
    boolean consumirPausa() {
        boolean p = pausaTocada;
        pausaTocada = false;
        return p;
    }

    boolean ativo() {
        return dedo != -1;
    }

    float centroX() {
        return centroX;
    }

    float centroY() {
        return centroY;
    }

    float atualX() {
        return atualX;
    }

    float atualY() {
        return atualY;
    }

    private static float virtualX(int x) {
        return x * Ui.largura() / Math.max(1f, Gdx.graphics.getWidth());
    }

    private static float virtualY(int y) {
        return Ui.ALTURA - y * Ui.ALTURA / Math.max(1f, Gdx.graphics.getHeight());
    }
}
