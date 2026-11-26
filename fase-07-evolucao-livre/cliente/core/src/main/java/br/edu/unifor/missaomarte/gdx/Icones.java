package br.edu.unifor.missaomarte.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/** Ícones das cartas (assets/icones/<chave>.png), carregados uma vez e achados pela chave da carta. */
class Icones implements Disposable {
    private final ObjectMap<String, Texture> porChave = new ObjectMap<>();

    Texture de(String chave) {
        Texture t = porChave.get(chave);
        if (t == null) {
            FileHandle f = Gdx.files.internal("icones/" + chave + ".png");
            if (!f.exists()) {
                return null;
            }
            t = new Texture(f, true);
            t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            porChave.put(chave, t);
        }
        return t;
    }

    @Override
    public void dispose() {
        for (Texture t : porChave.values()) {
            t.dispose();
        }
    }
}
