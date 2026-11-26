package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

/**
 * Fontes TTF geradas em tempo de execução (gdx-freetype): Orbitron (títulos, estilo sci-fi)
 * e Rajdhani (textos e HUD). Licença OFL. Geradas no dobro do tamanho e desenhadas em escala
 * 0,5 para ficarem nítidas em telas grandes.
 */
class Fontes implements Disposable {
    private static final String CARACTERES = FreeTypeFontGenerator.DEFAULT_CHARS
            + "ãáâàéêíóôõúüçÃÁÂÀÉÊÍÓÔÕÚÜÇºª•";

    final BitmapFont tituloGigante;
    final BitmapFont titulo;
    final BitmapFont subtitulo;
    final BitmapFont texto;
    final BitmapFont textoPequeno;
    final BitmapFont numero;

    Fontes() {
        FreeTypeFontGenerator orbitron = new FreeTypeFontGenerator(Gdx.files.internal("fontes/Orbitron-ExtraBold.ttf"));
        FreeTypeFontGenerator rajdhani = new FreeTypeFontGenerator(Gdx.files.internal("fontes/Rajdhani-Bold.ttf"));
        FreeTypeFontGenerator rajdhaniSemi = new FreeTypeFontGenerator(Gdx.files.internal("fontes/Rajdhani-SemiBold.ttf"));
        tituloGigante = gerar(orbitron, 88, 3, true);
        titulo = gerar(orbitron, 40, 2, true);
        subtitulo = gerar(orbitron, 22, 0, false);
        texto = gerar(rajdhani, 28, 2, true);
        textoPequeno = gerar(rajdhaniSemi, 21, 1, true);
        numero = gerar(rajdhani, 34, 3, true);
        orbitron.dispose();
        rajdhani.dispose();
        rajdhaniSemi.dispose();
    }

    private static BitmapFont gerar(FreeTypeFontGenerator gerador, int tamanho, int borda, boolean sombra) {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = tamanho * 2;
        p.characters = CARACTERES;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        p.borderWidth = borda * 2;
        p.borderColor = new Color(0.08f, 0.03f, 0.03f, 0.85f);
        if (sombra) {
            p.shadowOffsetX = 2;
            p.shadowOffsetY = 3;
            p.shadowColor = new Color(0, 0, 0, 0.45f);
        }
        BitmapFont f = gerador.generateFont(p);
        f.getData().setScale(0.5f);
        f.setUseIntegerPositions(false);
        return f;
    }

    @Override
    public void dispose() {
        for (BitmapFont f : new BitmapFont[] {tituloGigante, titulo, subtitulo, texto, textoPequeno, numero}) {
            f.dispose();
        }
    }
}
