package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Arena;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/** Chão de Marte, rochas, crateras e muros da arena. Só decoração: não afeta o jogo. */
class Cenario {
    final Array<ModelInstance> instancias = new Array<>();

    Cenario(Modelos modelos, Arena arena, long semente) {
        float m = arena.meiaLargura();
        Random r = new Random(semente);

        ModelInstance chao = new ModelInstance(modelos.chao);
        chao.transform.setToTranslation(0, -0.5f, 0).scale(2 * m + 30, 1f, 2 * m + 30);
        instancias.add(chao);

        for (int i = 0; i < 26; i++) {
            ModelInstance c = new ModelInstance(modelos.cratera);
            float d = 1.5f + r.nextFloat() * 4f;
            c.transform.setToTranslation(sortear(r, m + 8, 5), 0.01f, sortear(r, m + 8, 5)).scale(d, 1f, d);
            instancias.add(c);
        }
        for (int i = 0; i < 90; i++) {
            ModelInstance pedra = new ModelInstance(modelos.rocha);
            float s = 0.3f + r.nextFloat() * (i < 20 ? 2.2f : 0.9f);
            pedra.transform.setToTranslation(sortear(r, m + 12, 4), s * 0.25f, sortear(r, m + 12, 4))
                    .rotate(Vector3.Y, r.nextFloat() * 360f)
                    .scale(s * (0.8f + r.nextFloat() * 0.6f), s * 0.6f, s);
            instancias.add(pedra);
        }
        float espessura = 1f;
        float altura = 1.2f;
        float comprimento = 2 * m + 2 * espessura;
        adicionarMuro(modelos, 0, m + espessura / 2, comprimento, altura, espessura);
        adicionarMuro(modelos, 0, -m - espessura / 2, comprimento, altura, espessura);
        adicionarMuro(modelos, m + espessura / 2, 0, espessura, altura, comprimento);
        adicionarMuro(modelos, -m - espessura / 2, 0, espessura, altura, comprimento);
    }

    private void adicionarMuro(Modelos modelos, float x, float z, float largura, float altura, float profundidade) {
        ModelInstance muro = new ModelInstance(modelos.muro);
        muro.transform.setToTranslation(x, altura / 2, z).scale(largura, altura, profundidade);
        instancias.add(muro);
    }

    /** Posição aleatória em [-limite, limite], evitando o centro (plataforma). */
    private static float sortear(Random r, float limite, float evitarCentro) {
        float v;
        do {
            v = (r.nextFloat() * 2 - 1) * limite;
        } while (Math.abs(v) < evitarCentro && r.nextBoolean());
        return MathUtils.clamp(v, -limite, limite);
    }
}
