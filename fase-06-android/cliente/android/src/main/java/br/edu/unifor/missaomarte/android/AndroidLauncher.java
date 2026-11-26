package br.edu.unifor.missaomarte.android;

import android.os.Bundle;
import br.edu.unifor.missaomarte.gdx.MissaoMarteJogo;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/** Ponto de entrada no Android. O jogo (core) é o mesmo do desktop. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;          // PBR do gdx-gltf
        config.useImmersiveMode = true; // tela cheia, sem barras do sistema
        config.useAccelerometer = false;
        config.useCompass = false;
        config.numSamples = 2;
        initialize(new MissaoMarteJogo(), config);
    }
}
