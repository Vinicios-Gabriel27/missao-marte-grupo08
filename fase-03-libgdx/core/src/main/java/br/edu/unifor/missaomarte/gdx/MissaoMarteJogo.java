package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Classe principal da libGDX. Guarda os recursos 2D compartilhados entre as telas
 * e troca de tela. Ciclo de vida: create → (render/resize/pause/resume)* → dispose.
 */
public class MissaoMarteJogo extends Game {
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private BitmapFont fonte;

    @Override
    public void create() {
        batch = new SpriteBatch();
        formas = new ShapeRenderer();
        fonte = new BitmapFont();
        fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fonte.setUseIntegerPositions(false);
        setScreen(new TelaMenu(this));
    }

    /** Troca a tela e libera a anterior. */
    public void trocarTela(Screen nova) {
        Screen antiga = getScreen();
        setScreen(nova);
        if (antiga != null) {
            antiga.dispose();
        }
    }

    public SpriteBatch batch() {
        return batch;
    }

    public ShapeRenderer formas() {
        return formas;
    }

    public BitmapFont fonte() {
        return fonte;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        formas.dispose();
        fonte.dispose();
    }
}
