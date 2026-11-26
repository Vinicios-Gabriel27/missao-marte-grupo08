package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Traduz teclado (WASD/setas) em direção do domínio. O joystick do Android entra na Fase 6. */
final class ControleTeclado {
    private ControleTeclado() {
    }

    static Vetor direcao() {
        float x = 0;
        float y = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= 1;
        }
        return new Vetor(x, y);
    }
}
