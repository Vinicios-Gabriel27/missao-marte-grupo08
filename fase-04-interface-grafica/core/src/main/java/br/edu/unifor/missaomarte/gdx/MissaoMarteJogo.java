package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.ranking.ServicoRanking;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Classe principal da libGDX. Cria uma vez os recursos caros (mundo 3D, fontes, sons) e os
 * compartilha entre as telas. Ciclo de vida: create → (render/resize/pause/resume)* → dispose.
 */
public class MissaoMarteJogo extends Game {
    private SpriteBatch batch;
    private ShapeRenderer formas;
    private Fontes fontes;
    private Sons sons;
    private Mundo3D mundo;
    private CenaVitrine vitrine;
    private ServicoRanking ranking;
    private Preferences preferencias;

    @Override
    public void create() {
        batch = new SpriteBatch();
        formas = new ShapeRenderer();
        fontes = new Fontes();
        sons = new Sons();
        mundo = new Mundo3D();
        vitrine = new CenaVitrine(mundo);
        ranking = new ServicoRanking(new RepositorioRankingLocal());
        preferencias = Gdx.app.getPreferences("missao-marte");
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

    /** MISSAOMARTE_DEMO=1: menu por alguns segundos, partida com piloto automático e capturas (CI). */
    static boolean modoDemonstracao() {
        return "1".equals(System.getenv("MISSAOMARTE_DEMO"));
    }

    void salvarCaptura(String nome) {
        Pixmap p = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(Gdx.files.local(nome), p, 6, true);
        p.dispose();
        Gdx.app.log("DEMO", nome + " salva");
    }

    String piloto() {
        return preferencias.getString("piloto", "Piloto");
    }

    void definirPiloto(String nome) {
        preferencias.putString("piloto", nome).flush();
    }

    int dificuldadePreferida() {
        return preferencias.getInteger("dificuldade", 1);
    }

    void definirDificuldadePreferida(int indice) {
        preferencias.putInteger("dificuldade", indice).flush();
    }

    SpriteBatch batch() {
        return batch;
    }

    ShapeRenderer formas() {
        return formas;
    }

    Fontes fontes() {
        return fontes;
    }

    Sons sons() {
        return sons;
    }

    Mundo3D mundo() {
        return mundo;
    }

    CenaVitrine vitrine() {
        return vitrine;
    }

    ServicoRanking ranking() {
        return ranking;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        mundo.redimensionar(width, height);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        formas.dispose();
        fontes.dispose();
        sons.dispose();
        mundo.dispose();
    }
}
