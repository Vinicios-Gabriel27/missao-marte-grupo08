package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Efeitos sonoros e música (sintetizados para o projeto). Limita a repetição de sons muito
 * frequentes (tiro, XP) para não virar ruído quando a horda cresce. M liga/desliga.
 */
class Sons implements Disposable {
    private static final String[] NOMES = {"tiro", "explosao", "explosao_grande", "xp", "nivel", "dano", "resgate",
        "plataforma", "chefe", "clique", "vitoria", "derrota"};

    private final ObjectMap<String, Sound> sons = new ObjectMap<>();
    private final ObjectFloatMap<String> ultimoToque = new ObjectFloatMap<>();
    private final Music musica;
    private final Preferences preferencias;
    private boolean mudo;
    private float relogio;

    Sons() {
        for (String n : NOMES) {
            sons.put(n, Gdx.audio.newSound(Gdx.files.internal("sons/" + n + ".wav")));
        }
        musica = Gdx.audio.newMusic(Gdx.files.internal("sons/musica.wav"));
        musica.setLooping(true);
        musica.setVolume(0.35f);
        preferencias = Gdx.app.getPreferences("missao-marte");
        mudo = preferencias.getBoolean("mudo", false);
    }

    void atualizar(float delta) {
        relogio += delta;
    }

    void tocar(String nome, float volume) {
        tocar(nome, volume, 0f);
    }

    /** Toca o som se ele não tocou nos últimos {@code intervalo} segundos. */
    void tocar(String nome, float volume, float intervalo) {
        if (mudo) {
            return;
        }
        if (relogio - ultimoToque.get(nome, -99f) < intervalo) {
            return;
        }
        ultimoToque.put(nome, relogio);
        Sound s = sons.get(nome);
        if (s != null) {
            s.play(volume, MathUtils.random(0.94f, 1.06f), 0f);
        }
    }

    void tocarMusica() {
        if (!mudo && !musica.isPlaying()) {
            musica.play();
        }
    }

    void alternarMudo() {
        mudo = !mudo;
        preferencias.putBoolean("mudo", mudo).flush();
        if (mudo) {
            musica.pause();
        } else {
            musica.play();
        }
    }

    boolean mudo() {
        return mudo;
    }

    @Override
    public void dispose() {
        for (Sound s : sons.values()) {
            s.dispose();
        }
        musica.dispose();
    }
}
