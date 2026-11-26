package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.sobrevivencia.Arena;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/** Chão de Marte, rochas, crateras e muros da arena. Só decoração: não afeta o jogo. */
class Cenario {
    /** Modelos simples (chão, montanhas, muros) — sombreamento clássico. */
    final Array<ModelInstance> instancias = new Array<>();
    /** Modelos Kenney (rochas, crateras, estruturas) — sombreamento PBR. */
    final Array<ModelInstance> instanciasPbr = new Array<>();

    Cenario(Modelos modelos, ModelosKenney kenney, Arena arena, long semente) {
        float m = arena.meiaLargura();
        Random r = new Random(semente);

        ModelInstance chao = new ModelInstance(modelos.chao);
        chao.transform.setToTranslation(0, 0f, 0).scale(2 * m + 140, 1f, 2 * m + 140);
        instancias.add(chao);

        for (int i = 0; i < 22; i++) {
            Model cratera = kenney.crateras.get(i % kenney.crateras.size);
            float d = 3f + r.nextFloat() * 5f;
            ModelInstance c = new ModelInstance(cratera);
            c.transform.setToTranslation(sortear(r, m, 6), -0.05f, sortear(r, m, 6))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(d, d * 0.6f, d);
            instanciasPbr.add(c);
        }
        for (int i = 0; i < 85; i++) {
            Model rocha = kenney.rochas.get(r.nextInt(kenney.rochas.size));
            float s = (i < 18 ? 2.5f : 1f) + r.nextFloat() * (i < 18 ? 3f : 1.6f);
            ModelInstance pedra = new ModelInstance(rocha);
            pedra.transform.setToTranslation(sortear(r, m + 14, 5), -0.05f, sortear(r, m + 14, 5))
                    .rotate(Vector3.Y, r.nextFloat() * 360f).scale(s, s, s);
            instanciasPbr.add(pedra);
        }
        // Base marciana: estruturas perto da plataforma e nos cantos da arena
        float[][] base = {
            {-10f, -8f, 1.8f, 40}, {10f, -9f, 2.6f, -30}, {11f, 9f, 2.2f, 200}, {-10f, 10f, 2.5f, 120},
            {-m + 4, -m + 4, 5f, 45}, {m - 4, m - 4, 5f, 225}, {m - 5, -m + 5, 4f, 135}, {-m + 5, m - 5, 4f, 315},
        };
        for (int i = 0; i < base.length; i++) {
            ModelInstance e = new ModelInstance(kenney.estruturas.get(i % kenney.estruturas.size));
            e.transform.setToTranslation(base[i][0], 0f, base[i][1]).rotate(Vector3.Y, base[i][3])
                    .scale(base[i][2], base[i][2], base[i][2]);
            instanciasPbr.add(e);
        }
        for (int i = 0; i < 40; i++) {
            float ang = i / 40f * MathUtils.PI2 + r.nextFloat() * 0.1f;
            float dist = m + 18 + r.nextFloat() * 25;
            float alt = 6 + r.nextFloat() * 14;
            float larg = 10 + r.nextFloat() * 14;
            ModelInstance mont = new ModelInstance(modelos.montanha);
            mont.transform.setToTranslation(MathUtils.cos(ang) * dist, alt / 2 - 0.5f, MathUtils.sin(ang) * dist)
                    .rotate(Vector3.Y, r.nextFloat() * 360).scale(larg, alt, larg * (0.7f + r.nextFloat() * 0.6f));
            instancias.add(mont);
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
