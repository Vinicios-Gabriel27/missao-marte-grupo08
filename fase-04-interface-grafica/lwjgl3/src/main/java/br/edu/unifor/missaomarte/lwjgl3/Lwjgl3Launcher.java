package br.edu.unifor.missaomarte.lwjgl3;

import br.edu.unifor.missaomarte.gdx.MissaoMarteJogo;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Ponto de entrada desktop: só cria a janela e entrega o jogo (que não conhece a plataforma). */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Missão Marte: Sobrevivência");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4); // 4x antialiasing
        new Lwjgl3Application(new MissaoMarteJogo(), config);
    }
}
